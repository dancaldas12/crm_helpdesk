package com.serviclient.crm.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.EncuestaCsat;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.TicketHistorial;
import com.serviclient.crm.entity.TicketMensaje;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoEncuestaCsat;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.entity.enums.TipoMensajeTicket;
import com.serviclient.crm.repository.ClienteRepository;
import com.serviclient.crm.repository.EncuestaCsatRepository;
import com.serviclient.crm.repository.TicketHistorialRepository;
import com.serviclient.crm.repository.TicketMensajeRepository;
import com.serviclient.crm.repository.TicketRepository;
import com.serviclient.crm.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.method.HandlerMethod;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ActiveProfiles("dev")
// Descomenta la siguiente linea si deseas mostrar en vivo las sentencias SQL reales de Hibernate:
@org.springframework.test.context.TestPropertySource(properties = "spring.jpa.show-sql=true")
@DisplayName("Pruebas de Integración - Ciclo de Vida de Mesa de Ayuda (Tickets)")
class TicketIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private TicketMensajeRepository ticketMensajeRepository;
    @Autowired private TicketHistorialRepository ticketHistorialRepository;
    @Autowired private EncuestaCsatRepository encuestaCsatRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("Debe crear ticket, calcular SLA, responder, cerrar y generar encuesta CSAT en BD")
    @WithUserDetails(
            value = "admin@serviclient.com",
            userDetailsServiceBeanName = "customUserDetailsService")
    void debeEjecutarCicloCompletoDeTicketConSlaMensajesYCierreCsat() throws Exception {
        System.out.println("\n======================================================================");
        System.out.println(" PRUEBA DE INTEGRACION 1: CICLO DE VIDA COMPLETO DE TICKET (E2E)");
        System.out.println("======================================================================");

        // 0. Obtener cliente semilla real desde H2
        Long clienteId = transactionTemplate.execute(status -> {
            Usuario admin = usuarioRepository.findByEmail("admin@serviclient.com")
                    .orElseThrow(() -> new IllegalStateException("No existe admin@serviclient.com en H2"));
            Long empresaId = admin.getEmpresa().getId();
            List<Cliente> clientes = clienteRepository.findByEmpresaId(empresaId);
            assertFalse(clientes.isEmpty(), "Debe existir al menos un cliente semilla para la empresa");
            Cliente cliente = clientes.get(0);
            System.out.println("[SETUP] Usuario autenticado: " + admin.getEmail() + " (Empresa ID=" + empresaId + ")");
            System.out.println("[SETUP] Cliente seleccionado en H2: " + cliente.getNombreComercial() + " (ID=" + cliente.getId() + ")");
            return cliente.getId();
        });

        Long ticketId = null;
        try {
            // 1. CREAR TICKET (POST /tickets)
            String asunto = "Falla de integracion en facturacion electronica";
            String descripcion = "Error 500 al emitir comprobantes desde el modulo principal.";

            System.out.println("\n[PASO 1] Enviando POST /tickets (con token CSRF y prioridad ALTA)...");
            MvcResult createResult = mockMvc.perform(post("/tickets")
                            .with(csrf())
                            .param("clienteId", String.valueOf(clienteId))
                            .param("asunto", asunto)
                            .param("descripcion", descripcion)
                            .param("categoria", "SOPORTE_TECNICO")
                            .param("prioridad", "ALTA"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/tickets/*"))
                    .andExpect(flash().attributeExists("successMessage"))
                    .andReturn();

            imprimirHandler(createResult);
            String redirectedUrl = createResult.getResponse().getRedirectedUrl();
            assertNotNull(redirectedUrl, "Debe devolver URL de redirección al detalle del ticket");
            ticketId = Long.parseLong(redirectedUrl.substring(redirectedUrl.lastIndexOf('/') + 1));

            Ticket ticketCreado = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new AssertionError("El ticket no se persistió en la BD"));

            assertTrue(ticketCreado.getNumeroTicket().startsWith("TK-"), "El código debe iniciar con TK-");
            assertEquals(asunto, ticketCreado.getAsunto());
            assertEquals(EstadoTicket.ABIERTO, ticketCreado.getEstado());
            assertEquals(PrioridadTicket.ALTA, ticketCreado.getPrioridad());
            assertNotNull(ticketCreado.getSlaRespuestaLimite(), "Debe calcular SLA de primera respuesta");
            assertNotNull(ticketCreado.getSlaResolucionLimite(), "Debe calcular SLA de resolución");
            assertTrue(ticketCreado.getSlaResolucionLimite().isAfter(ticketCreado.getCreatedAt()),
                    "El SLA de resolución debe ser posterior a la fecha de creación");
            assertNull(ticketCreado.getFechaPrimeraRespuesta(), "Aún no debe tener primera respuesta");

            List<TicketMensaje> mensajesIniciales =
                    ticketMensajeRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
            assertEquals(1, mensajesIniciales.size(), "Debe existir el mensaje inicial");
            assertEquals(descripcion, mensajesIniciales.get(0).getMensaje());
            assertFalse(Boolean.TRUE.equals(mensajesIniciales.get(0).getEsInterno()));

            List<TicketHistorial> historialInicial =
                    ticketHistorialRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
            assertTrue(
                    historialInicial.stream().anyMatch(h -> "CREACION".equals(h.getTipoEvento())),
                    "Debe existir evento CREACION en el historial");

            System.out.println("  [OK] HTTP " + createResult.getResponse().getStatus() + " -> Redireccion a " + redirectedUrl);
            System.out.println("  [BD] Ticket guardado: ID=" + ticketCreado.getId()
                    + " | Codigo=" + ticketCreado.getNumeroTicket()
                    + " | Estado=" + ticketCreado.getEstado()
                    + " | Prioridad=" + ticketCreado.getPrioridad());
            System.out.println("  [BD] SLA 1ra Respuesta: " + ticketCreado.getSlaRespuestaLimite()
                    + " | SLA Resolucion: " + ticketCreado.getSlaResolucionLimite());
            System.out.println("  [BD] Mensajes: " + mensajesIniciales.size()
                    + " | Eventos en historial: " + historialInicial.stream().map(TicketHistorial::getTipoEvento).toList());

            // 2. CONSULTAR DETALLE DEL TICKET (GET /tickets/{id})
            System.out.println("\n[PASO 2] Enviando GET /tickets/" + ticketId + "...");
            MvcResult detalleResult = mockMvc.perform(get("/tickets/{id}", ticketId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("helpdesk/detalle"))
                    .andExpect(model().attributeExists("ticket", "agentes", "estados", "prioridades"))
                    .andReturn();

            imprimirHandler(detalleResult);
            System.out.println("  [OK] HTTP " + detalleResult.getResponse().getStatus()
                    + " | Vista renderizada: " + detalleResult.getModelAndView().getViewName());

            // 3. ENVIAR PRIMERA RESPUESTA PÚBLICA (POST /tickets/{id}/mensajes)
            String respuestaAgente = "Estamos revisando los logs del servicio de facturacion.";
            System.out.println("\n[PASO 3] Enviando POST /tickets/" + ticketId + "/mensajes (respuesta publica)...");
            MvcResult mensajeResult = mockMvc.perform(post("/tickets/{id}/mensajes", ticketId)
                            .with(csrf())
                            .param("contenido", respuestaAgente)
                            .param("esNotaInterna", "false"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/tickets/" + ticketId))
                    .andExpect(flash().attributeExists("successMessage"))
                    .andReturn();

            imprimirHandler(mensajeResult);
            Ticket ticketRespondido = ticketRepository.findById(ticketId).orElseThrow();
            assertEquals(EstadoTicket.EN_PROCESO, ticketRespondido.getEstado(),
                    "La primera respuesta pública debe pasar el ticket a EN_PROCESO");
            assertNotNull(ticketRespondido.getFechaPrimeraRespuesta(),
                    "Debe registrarse la fecha de primera respuesta");

            List<TicketMensaje> mensajesTrasRespuesta =
                    ticketMensajeRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
            assertEquals(2, mensajesTrasRespuesta.size(), "Deben existir 2 mensajes en el hilo");
            assertEquals(respuestaAgente, mensajesTrasRespuesta.get(1).getMensaje());
            assertEquals(TipoMensajeTicket.AGENTE, mensajesTrasRespuesta.get(1).getTipo());
            assertFalse(Boolean.TRUE.equals(mensajesTrasRespuesta.get(1).getEsInterno()));

            List<TicketHistorial> historialTrasRespuesta =
                    ticketHistorialRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
            assertTrue(
                    historialTrasRespuesta.stream().anyMatch(h -> "RESPUESTA".equals(h.getTipoEvento())),
                    "Debe existir evento RESPUESTA en el historial");

            System.out.println("  [OK] Transicion automatica de estado: ABIERTO -> " + ticketRespondido.getEstado());
            System.out.println("  [BD] Fecha primera respuesta registrada: " + ticketRespondido.getFechaPrimeraRespuesta());
            System.out.println("  [BD] Total mensajes en conversacion: " + mensajesTrasRespuesta.size()
                    + " | Historial: " + historialTrasRespuesta.stream().map(TicketHistorial::getTipoEvento).toList());

            // 4. CERRAR TICKET CON ENCUESTA CSAT (POST /tickets/{id}/cerrar)
            System.out.println("\n[PASO 4] Enviando POST /tickets/" + ticketId + "/cerrar (enviarCsat=true)...");
            MvcResult cierreResult = mockMvc.perform(post("/tickets/{id}/cerrar", ticketId)
                            .with(csrf())
                            .param("enviarCsat", "true")
                            .param("comentarioCierre", "Incidencia resuelta y validada con el cliente."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/tickets/" + ticketId))
                    .andExpect(flash().attributeExists("successMessage"))
                    .andReturn();

            imprimirHandler(cierreResult);
            Ticket ticketCerrado = ticketRepository.findById(ticketId).orElseThrow();
            assertEquals(EstadoTicket.CERRADO, ticketCerrado.getEstado());
            assertNotNull(ticketCerrado.getFechaResolucion(), "Debe registrar fecha de resolución");
            assertNotNull(ticketCerrado.getFechaCierre(), "Debe registrar fecha de cierre");

            List<TicketHistorial> historialFinal =
                    ticketHistorialRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
            assertTrue(
                    historialFinal.stream().anyMatch(h -> "CIERRE".equals(h.getTipoEvento())),
                    "Debe existir evento CIERRE en el historial");

            Optional<EncuestaCsat> encuestaOpt = encuestaCsatRepository.findByTicketId(ticketId);
            assertTrue(encuestaOpt.isPresent(), "Debe haberse generado una encuesta CSAT para el ticket");
            assertEquals(EstadoEncuestaCsat.PENDIENTE, encuestaOpt.get().getEstado());
            assertNotNull(encuestaOpt.get().getFechaEnvio());
            assertTrue(encuestaOpt.get().isActiva());

            System.out.println("  [OK] Ticket cerrado: Estado=" + ticketCerrado.getEstado()
                    + " | FechaCierre=" + ticketCerrado.getFechaCierre());
            System.out.println("  [BD] Encuesta CSAT creada automaticamente: ID=" + encuestaOpt.get().getId()
                    + " | Estado=" + encuestaOpt.get().getEstado()
                    + " | Activa=" + encuestaOpt.get().isActiva());
            System.out.println("  [BD] Auditoria completa en historial: "
                    + historialFinal.stream().map(TicketHistorial::getTipoEvento).toList());

        } finally {
            if (ticketId != null) {
                final Long idAEliminar = ticketId;
                transactionTemplate.executeWithoutResult(status -> {
                    encuestaCsatRepository.findByTicketId(idAEliminar)
                            .ifPresent(encuestaCsatRepository::delete);
                    ticketRepository.findById(idAEliminar)
                            .ifPresent(ticketRepository::delete);
                });
                System.out.println("\n[CLEANUP] Ticket ID=" + ticketId + " y su encuesta CSAT eliminados de H2.");
            }
            System.out.println("======================================================================\n");
        }
    }

    @Test
    @DisplayName("Debe rechazar creación de ticket cuando faltan campos obligatorios")
    @WithUserDetails(
            value = "admin@serviclient.com",
            userDetailsServiceBeanName = "customUserDetailsService")
    void debeRechazarCreacionDeTicketConDatosInvalidos() throws Exception {
        System.out.println("\n======================================================================");
        System.out.println(" PRUEBA DE INTEGRACION 2: VALIDACION DE FORMULARIO (@Valid)");
        System.out.println("======================================================================");

        long totalAntes = ticketRepository.count();
        System.out.println("[BD] Tickets existentes antes de la peticion: " + totalAntes);
        System.out.println("[ENVIO] POST /tickets con clienteId=null, asunto='', descripcion=''...");

        MvcResult result = mockMvc.perform(post("/tickets")
                        .with(csrf())
                        .param("asunto", "")
                        .param("descripcion", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("helpdesk/form"))
                .andExpect(model().attributeHasFieldErrors("ticketDto", "clienteId", "asunto", "descripcion"))
                .andReturn();

        imprimirHandler(result);
        long totalDespues = ticketRepository.count();
        assertEquals(totalAntes, totalDespues,
                "No debe insertarse ningún ticket cuando fallan las validaciones");

        System.out.println("  [OK] HTTP " + result.getResponse().getStatus()
                + " | Vista devuelta: " + result.getModelAndView().getViewName()
                + " (con errores en clienteId, asunto, descripcion)");
        System.out.println("  [BD] Tickets existentes despues: " + totalDespues + " (sin inserciones sucias)");
        System.out.println("======================================================================\n");
    }

    @Test
    @DisplayName("Debe denegar acceso a /tickets para un usuario con rol SUPERVISOR")
    @WithMockUser(username = "supervisor@serviclient.com", roles = "SUPERVISOR")
    void debeDenegarAccesoATicketsParaRolSupervisor() throws Exception {
        System.out.println("\n======================================================================");
        System.out.println(" PRUEBA DE INTEGRACION 3: SEGURIDAD POR ROLES (SPRING SECURITY)");
        System.out.println("======================================================================");
        System.out.println("[ENVIO] GET /tickets autenticado como rol SUPERVISOR (ruta solo ADMIN/AGENTE)...");

        MvcResult result = mockMvc.perform(get("/tickets"))
                .andExpect(status().isForbidden())
                .andReturn();

        System.out.println("  [OK] Acceso bloqueado por SecurityConfig -> HTTP "
                + result.getResponse().getStatus() + " (Forbidden)");
        System.out.println("======================================================================\n");
    }

    private void imprimirHandler(MvcResult resultado) {
        if (resultado.getHandler() instanceof HandlerMethod handler) {
            System.out.println("  [MVC] Controlador: " + handler.getBeanType().getSimpleName()
                    + "." + handler.getMethod().getName() + "()");
        }
    }
}