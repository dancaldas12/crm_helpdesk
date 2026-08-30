package com.serviclient.crm.service;

import com.serviclient.crm.dto.TicketDto;
import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMensajeRepository ticketMensajeRepository;
    private final TicketHistorialRepository ticketHistorialRepository;
    private final EncuestaSatisfaccionRepository encuestaRepository;
    private final ClienteRepository clienteRepository;
    private final ContactoRepository contactoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaService empresaService;
    private final HealthScoreService healthScoreService;

    @Getter
    @Builder
    public static class MetricasTickets {
        private long ticketsAbiertos;
        private long enProceso;
        private long criticos;
        private long pendientes;
        private long fueraDeSla;
    }

    @Transactional(readOnly = true)
    public MetricasTickets obtenerMetricas(Long empresaId) {
        long abiertos = ticketRepository.countByEmpresaIdAndEstado(empresaId, EstadoTicket.ABIERTO);
        long enProceso = ticketRepository.countByEmpresaIdAndEstado(empresaId, EstadoTicket.EN_PROCESO);
        long criticos = ticketRepository.countByEmpresaIdAndPrioridad(empresaId, PrioridadTicket.CRITICA);
        long pendientes = ticketRepository.countByEmpresaIdAndEstado(empresaId, EstadoTicket.PENDIENTE);
        long fueraDeSla = ticketRepository.countTicketsFueraDeSla(empresaId, LocalDateTime.now());

        return MetricasTickets.builder()
                .ticketsAbiertos(abiertos)
                .enProceso(enProceso)
                .criticos(criticos)
                .pendientes(pendientes)
                .fueraDeSla(fueraDeSla)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<Ticket> listarTickets(Long empresaId, String busqueda, EstadoTicket estado,
                                     PrioridadTicket prioridad, CategoriaTicket categoria, Pageable pageable) {
        return ticketRepository.buscarConFiltros(empresaId, busqueda, estado, prioridad, categoria, pageable);
    }

    @Transactional(readOnly = true)
    public Ticket obtenerPorId(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Ticket obtenerPorCodigo(String codigo) {
        return ticketRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con código: " + codigo));
    }

    @Transactional
    public Ticket crearTicket(Long empresaId, TicketDto dto, Usuario autor) {
        Empresa empresa = empresaService.obtenerPorId(empresaId);
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        Contacto contacto = null;
        if (dto.getContactoId() != null) {
            contacto = contactoRepository.findById(dto.getContactoId()).orElse(null);
        } else {
            contacto = cliente.getContactoPrincipal();
        }

        Usuario agente = null;
        if (dto.getAgenteId() != null) {
            agente = usuarioRepository.findById(dto.getAgenteId()).orElse(null);
        }

        // Generar código TK-xxxx
        String codigo = "TK-" + (4000 + new Random().nextInt(5000));

        LocalDateTime ahora = LocalDateTime.now();
        int horasSla1ra = empresa.getSlaPrimerRespuestaHoras() != null ? empresa.getSlaPrimerRespuestaHoras() : 2;
        int horasSlaRes = empresa.getSlaResolucionHoras() != null ? empresa.getSlaResolucionHoras() : 24;

        Ticket ticket = Ticket.builder()
                .codigo(codigo)
                .empresa(empresa)
                .cliente(cliente)
                .contacto(contacto)
                .agente(agente)
                .asunto(dto.getAsunto())
                .descripcion(dto.getDescripcion())
                .categoria(dto.getCategoria() != null ? dto.getCategoria() : CategoriaTicket.SOPORTE_TECNICO)
                .prioridad(dto.getPrioridad() != null ? dto.getPrioridad() : PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .canalOrigen(dto.getCanalOrigen())
                .fechaCreacion(ahora)
                .fechaLimitePrimeraRespuesta(ahora.plusHours(horasSla1ra))
                .fechaLimiteResolucion(ahora.plusHours(horasSlaRes))
                .build();

        Ticket guardado = ticketRepository.save(ticket);

        // Mensaje inicial en la conversación
        String remitenteNombre = (contacto != null) ? contacto.getNombre() : (autor != null ? autor.getNombreCompleto() : "Cliente");
        TicketMensaje mensajeInicial = TicketMensaje.builder()
                .ticket(guardado)
                .remitente(autor)
                .remitenteNombre(remitenteNombre)
                .esAgente(autor != null && autor.getRol() != null && autor.getRol().name().equals("AGENTE"))
                .esNotaInterna(false)
                .contenido(dto.getDescripcion())
                .fechaCreacion(ahora)
                .build();
        ticketMensajeRepository.save(mensajeInicial);

        // Registro de auditoría
        TicketHistorial hist = TicketHistorial.builder()
                .ticket(guardado)
                .usuario(autor)
                .autorNombre(remitenteNombre)
                .accion("Ticket creado vía " + dto.getCanalOrigen().getEtiqueta())
                .detalle("Asunto: " + dto.getAsunto())
                .fechaCreacion(ahora)
                .build();
        ticketHistorialRepository.save(hist);

        healthScoreService.recalcularYActualizarHealthScore(cliente);
        return guardado;
    }

    @Transactional
    public TicketMensaje enviarMensaje(Long ticketId, Usuario autor, String contenido, boolean esNotaInterna) {
        Ticket ticket = obtenerPorId(ticketId);
        LocalDateTime ahora = LocalDateTime.now();

        // Si es la primera respuesta del agente, marcar SLA de 1ra respuesta
        if (!esNotaInterna && ticket.getFechaPrimeraRespuesta() == null) {
            ticket.setFechaPrimeraRespuesta(ahora);
            ticket.setEstado(EstadoTicket.EN_PROCESO);
            ticketRepository.save(ticket);
        }

        String autorNombre = (autor != null) ? autor.getNombreCompleto() : "Agente";

        TicketMensaje mensaje = TicketMensaje.builder()
                .ticket(ticket)
                .remitente(autor)
                .remitenteNombre(autorNombre)
                .remitenteAvatar(autor != null ? autor.getAvatarUrl() : null)
                .esAgente(true)
                .esNotaInterna(esNotaInterna)
                .contenido(contenido)
                .fechaCreacion(ahora)
                .build();

        TicketMensaje guardado = ticketMensajeRepository.save(mensaje);

        // Registro de auditoría si es mensaje público
        if (!esNotaInterna) {
            TicketHistorial hist = TicketHistorial.builder()
                    .ticket(ticket)
                    .usuario(autor)
                    .autorNombre(autorNombre)
                    .accion("Respuesta enviada al cliente")
                    .detalle(contenido.length() > 60 ? contenido.substring(0, 57) + "..." : contenido)
                    .fechaCreacion(ahora)
                    .build();
            ticketHistorialRepository.save(hist);
        }

        return guardado;
    }

    @Transactional
    public void asignarAgente(Long ticketId, Long nuevoAgenteId, Usuario autor) {
        Ticket ticket = obtenerPorId(ticketId);
        Usuario nuevoAgente = usuarioRepository.findById(nuevoAgenteId)
                .orElseThrow(() -> new IllegalArgumentException("Agente no encontrado"));

        ticket.setAgente(nuevoAgente);
        ticketRepository.save(ticket);

        TicketHistorial hist = TicketHistorial.builder()
                .ticket(ticket)
                .usuario(autor)
                .autorNombre(autor != null ? autor.getNombreCompleto() : "Sistema")
                .accion("Ticket asignado a " + nuevoAgente.getNombreCompleto())
                .detalle("Reasignación de agente responsable")
                .fechaCreacion(LocalDateTime.now())
                .build();
        ticketHistorialRepository.save(hist);
    }

    @Transactional
    public void cambiarEstadoYPrioridad(Long ticketId, EstadoTicket nuevoEstado, PrioridadTicket nuevaPrioridad, String comentario, Usuario autor) {
        Ticket ticket = obtenerPorId(ticketId);
        LocalDateTime ahora = LocalDateTime.now();

        if (nuevoEstado != null && nuevoEstado != ticket.getEstado()) {
            ticket.setEstado(nuevoEstado);
            if (nuevoEstado == EstadoTicket.RESUELTO && ticket.getFechaResolucion() == null) {
                ticket.setFechaResolucion(ahora);
            }
            TicketHistorial hist = TicketHistorial.builder()
                    .ticket(ticket)
                    .usuario(autor)
                    .autorNombre(autor != null ? autor.getNombreCompleto() : "Sistema")
                    .accion("Estado cambiado a: " + nuevoEstado.getEtiqueta())
                    .detalle(comentario != null && !comentario.isBlank() ? comentario : "Actualización manual de estado")
                    .fechaCreacion(ahora)
                    .build();
            ticketHistorialRepository.save(hist);
        }

        if (nuevaPrioridad != null && nuevaPrioridad != ticket.getPrioridad()) {
            ticket.setPrioridad(nuevaPrioridad);
            TicketHistorial hist = TicketHistorial.builder()
                    .ticket(ticket)
                    .usuario(autor)
                    .autorNombre(autor != null ? autor.getNombreCompleto() : "Sistema")
                    .accion("Prioridad cambiada a: " + nuevaPrioridad.getEtiqueta())
                    .detalle("Ajuste de severidad")
                    .fechaCreacion(ahora)
                    .build();
            ticketHistorialRepository.save(hist);
        }

        ticketRepository.save(ticket);
        healthScoreService.recalcularYActualizarHealthScore(ticket.getCliente());
    }

    @Transactional
    public void cerrarTicket(Long ticketId, Boolean enviarCsat, String comentarioCierre, Usuario autor) {
        Ticket ticket = obtenerPorId(ticketId);
        LocalDateTime ahora = LocalDateTime.now();

        ticket.setEstado(EstadoTicket.CERRADO);
        ticket.setFechaCierre(ahora);
        if (ticket.getFechaResolucion() == null) {
            ticket.setFechaResolucion(ahora);
        }
        ticketRepository.save(ticket);

        TicketHistorial hist = TicketHistorial.builder()
                .ticket(ticket)
                .usuario(autor)
                .autorNombre(autor != null ? autor.getNombreCompleto() : "Sistema")
                .accion("Ticket Cerrado")
                .detalle(Boolean.TRUE.equals(enviarCsat) ? "Cierre de ticket con envío de encuesta CSAT" : "Cierre de ticket")
                .fechaCreacion(ahora)
                .build();
        ticketHistorialRepository.save(hist);

        if (Boolean.TRUE.equals(enviarCsat)) {
            // Generar registro inicial de encuesta CSAT
            EncuestaSatisfaccion encuesta = EncuestaSatisfaccion.builder()
                    .ticket(ticket)
                    .cliente(ticket.getCliente())
                    .contacto(ticket.getContacto())
                    .puntuacion(5) // Default positivo inicial para demo
                    .comentario("Excelente atención y resolución rápida.")
                    .fechaCreacion(ahora)
                    .build();
            encuestaRepository.save(encuesta);
        }

        healthScoreService.recalcularYActualizarHealthScore(ticket.getCliente());
    }
}
