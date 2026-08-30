package com.serviclient.crm.config;

import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.*;
import com.serviclient.crm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ContactoRepository contactoRepository;
    private final TicketRepository ticketRepository;
    private final TicketMensajeRepository ticketMensajeRepository;
    private final TicketHistorialRepository ticketHistorialRepository;
    private final RenovacionRepository renovacionRepository;
    private final RenovacionSeguimientoRepository renovacionSeguimientoRepository;
    private final EncuestaSatisfaccionRepository encuestaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (empresaRepository.count() > 0) {
            log.info("Datos ya inicializados en la base de datos.");
            return;
        }

        log.info("Sembrando datos iniciales de demostración acordes a los prototipos...");

        // 1. Empresa Principal
        Empresa empresa = Empresa.builder()
                .nombreComercial("ServiClient")
                .razonSocial("ServiClient Cloud Solutions S.A.C.")
                .ruc("20601234567")
                .industria("Tecnología & SaaS")
                .pais("Perú")
                .ciudad("Lima")
                .direccion("Av. Javier Prado Este 4200, San Isidro")
                .slaPrimerRespuestaHoras(2)
                .slaResolucionHoras(24)
                .activarCsat(true)
                .escalaCsat("1 a 5")
                .factorTicketsCriticos(true)
                .factorCsat(true)
                .factorRenovacionProxima(true)
                .build();
        empresa = empresaRepository.save(empresa);

        // 2. Usuarios
        Usuario admin = Usuario.builder()
                .empresa(empresa)
                .nombre("Juan")
                .apellido("Pérez")
                .email("admin@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
        admin = usuarioRepository.save(admin);

        Usuario carlos = Usuario.builder()
                .empresa(empresa)
                .nombre("Carlos")
                .apellido("Ruiz")
                .email("carlos.ruiz@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.AGENTE)
                .activo(true)
                .build();
        carlos = usuarioRepository.save(carlos);

        Usuario ana = Usuario.builder()
                .empresa(empresa)
                .nombre("Ana")
                .apellido("García")
                .email("ana.garcia@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.AGENTE)
                .activo(true)
                .build();
        ana = usuarioRepository.save(ana);

        Usuario laura = Usuario.builder()
                .empresa(empresa)
                .nombre("Laura")
                .apellido("Gómez")
                .email("laura.gomez@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.AGENTE)
                .activo(true)
                .build();
        laura = usuarioRepository.save(laura);

        Usuario elena = Usuario.builder()
                .empresa(empresa)
                .nombre("Elena")
                .apellido("Montes")
                .email("elena.montes@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.AGENTE)
                .activo(true)
                .build();
        elena = usuarioRepository.save(elena);

        Usuario anaSilva = Usuario.builder()
                .empresa(empresa)
                .nombre("Ana")
                .apellido("Silva")
                .email("ana.silva@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.AGENTE)
                .activo(true)
                .build();
        anaSilva = usuarioRepository.save(anaSilva);

        // 3. Clientes
        LocalDate hoy = LocalDate.now();

        Cliente novaTech = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("NovaTech Solutions")
                .razonSocial("NovaTech Solutions S.A.")
                .ruc("20554433221")
                .responsable(carlos)
                .servicioContratado("SaaS Enterprise")
                .estado(EstadoCliente.EN_RIESGO)
                .healthScore(43)
                .csatPromedio(3.8)
                .fechaInicio(LocalDate.of(2026, 1, 15))
                .fechaRenovacion(hoy.plusDays(15))
                .build();
        novaTech = clienteRepository.save(novaTech);

        Contacto contactLaura = Contacto.builder()
                .cliente(novaTech)
                .nombre("Laura M.")
                .cargo("Directora de Operaciones")
                .email("laura.m@novatech.com")
                .telefono("+51 987 654 321")
                .esPrincipal(true)
                .build();
        contactoRepository.save(contactLaura);

        Contacto contactCarlosM = Contacto.builder()
                .cliente(novaTech)
                .nombre("Carlos Méndez")
                .cargo("Gerente de TI")
                .email("carlos.m@novatech.com")
                .telefono("+51 981 123 456")
                .esPrincipal(false)
                .build();
        contactoRepository.save(contactCarlosM);

        Cliente globalCorp = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("GlobalCorp Inc.")
                .razonSocial("GlobalCorp International S.A.C.")
                .ruc("20112233445")
                .responsable(carlos)
                .servicioContratado("SaaS Standard")
                .estado(EstadoCliente.SALUDABLE)
                .healthScore(92)
                .csatPromedio(4.25) // 85%
                .fechaInicio(LocalDate.of(2025, 6, 1))
                .fechaRenovacion(hoy.plusDays(98))
                .build();
        globalCorp = clienteRepository.save(globalCorp);

        Contacto contactMaria = Contacto.builder()
                .cliente(globalCorp)
                .nombre("María López")
                .cargo("Jefa de Sistemas")
                .email("maria.lopez@globalcorp.com")
                .telefono("+51 999 888 777")
                .esPrincipal(true)
                .build();
        contactoRepository.save(contactMaria);

        Cliente dataFlow = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("DataFlow Systems")
                .razonSocial("DataFlow Systems Perú S.R.L.")
                .ruc("20443322119")
                .responsable(elena)
                .servicioContratado("SaaS Premium")
                .estado(EstadoCliente.OBSERVACION)
                .healthScore(68)
                .csatPromedio(3.9) // 78%
                .fechaInicio(LocalDate.of(2025, 11, 1))
                .fechaRenovacion(hoy.plusDays(45))
                .build();
        dataFlow = clienteRepository.save(dataFlow);

        Contacto contactPedro = Contacto.builder()
                .cliente(dataFlow)
                .nombre("Pedro Ramírez")
                .cargo("Analista de Datos")
                .email("pedro.ramirez@dataflow.com")
                .telefono("+51 944 556 677")
                .esPrincipal(true)
                .build();
        contactoRepository.save(contactPedro);

        Cliente techCorp = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("TechCorp Industries")
                .razonSocial("TechCorp Industries del Perú S.A.")
                .ruc("20887766554")
                .responsable(anaSilva)
                .servicioContratado("Licencia Enterprise")
                .estado(EstadoCliente.EN_RIESGO)
                .healthScore(45)
                .csatPromedio(3.5)
                .fechaInicio(LocalDate.of(2025, 11, 15))
                .fechaRenovacion(hoy.plusDays(12))
                .build();
        techCorp = clienteRepository.save(techCorp);

        Cliente globalLogistics = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("Global Logistics SA")
                .razonSocial("Global Logistics and Supply S.A.")
                .ruc("20776655443")
                .responsable(carlos)
                .servicioContratado("Soporte Premium 24/7")
                .estado(EstadoCliente.SALUDABLE)
                .healthScore(85)
                .csatPromedio(4.8)
                .fechaInicio(LocalDate.of(2025, 11, 28))
                .fechaRenovacion(hoy.plusDays(25))
                .build();
        globalLogistics = clienteRepository.save(globalLogistics);

        Cliente grupoFinanciero = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("Grupo Financiero Sur")
                .razonSocial("Grupo Financiero del Sur S.A.C.")
                .ruc("20998877661")
                .responsable(laura)
                .servicioContratado("Módulo Analytics")
                .estado(EstadoCliente.SALUDABLE)
                .healthScore(95)
                .csatPromedio(4.9)
                .fechaInicio(LocalDate.of(2025, 12, 5))
                .fechaRenovacion(hoy.plusDays(32))
                .build();
        grupoFinanciero = clienteRepository.save(grupoFinanciero);

        Cliente retailSolutions = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("Retail Solutions Inc")
                .razonSocial("Retail Solutions Latin America S.A.")
                .ruc("20334455667")
                .responsable(anaSilva)
                .servicioContratado("Licencia Standard")
                .estado(EstadoCliente.OBSERVACION)
                .healthScore(65)
                .csatPromedio(4.0)
                .fechaInicio(LocalDate.of(2026, 1, 10))
                .fechaRenovacion(hoy.plusDays(68))
                .build();
        retailSolutions = clienteRepository.save(retailSolutions);

        // 4. Tickets
        LocalDateTime ahora = LocalDateTime.now();

        // TK-4029 (Principal del prototipo)
        Ticket tk4029 = Ticket.builder()
                .codigo("TK-4029")
                .empresa(empresa)
                .cliente(novaTech)
                .contacto(contactLaura)
                .agente(carlos)
                .asunto("Error al acceder al sistema")
                .descripcion("Hola, desde esta mañana no podemos acceder al panel principal del sistema. Nos lanza un error 500 constante. ¿Pueden revisarlo urgente?")
                .categoria(CategoriaTicket.SOPORTE_TECNICO)
                .prioridad(PrioridadTicket.CRITICA)
                .estado(EstadoTicket.EN_PROCESO)
                .canalOrigen(CanalOrigen.PORTAL_WEB)
                .fechaCreacion(ahora.minusHours(3))
                .fechaLimitePrimeraRespuesta(ahora.minusHours(1))
                .fechaPrimeraRespuesta(ahora.minusHours(2).minusMinutes(45))
                .fechaLimiteResolucion(ahora.plusHours(21))
                .build();
        tk4029 = ticketRepository.save(tk4029);

        TicketMensaje msg1 = TicketMensaje.builder()
                .ticket(tk4029)
                .remitente(null)
                .remitenteNombre("Laura M.")
                .esAgente(false)
                .esNotaInterna(false)
                .contenido("Hola, desde esta mañana no podemos acceder al panel principal del sistema. Nos lanza un error 500 constante. ¿Pueden revisarlo urgente?")
                .fechaCreacion(ahora.minusHours(3))
                .build();
        ticketMensajeRepository.save(msg1);

        TicketMensaje msg2 = TicketMensaje.builder()
                .ticket(tk4029)
                .remitente(carlos)
                .remitenteNombre("Carlos R. (Tú)")
                .esAgente(true)
                .esNotaInterna(false)
                .contenido("Hola Laura, lamento el inconveniente. Ya he escalado el caso al equipo de infraestructura. Parece ser un problema de sincronización en el clúster de autenticación.")
                .fechaCreacion(ahora.minusHours(2).minusMinutes(45))
                .build();
        ticketMensajeRepository.save(msg2);

        TicketMensaje nota1 = TicketMensaje.builder()
                .ticket(tk4029)
                .remitente(carlos)
                .remitenteNombre("Carlos R.")
                .esAgente(true)
                .esNotaInterna(true)
                .contenido("Se reinició el servicio en el pod secundario. Se está monitoreando la latencia con DevOps.")
                .fechaCreacion(ahora.minusHours(1))
                .build();
        ticketMensajeRepository.save(nota1);

        // Historial TK-4029
        ticketHistorialRepository.save(TicketHistorial.builder()
                .ticket(tk4029)
                .autorNombre("Carlos R.")
                .accion("Estado cambiado a: En Progreso")
                .detalle("Iniciada la investigación técnica")
                .fechaCreacion(ahora.minusHours(2).minusMinutes(45))
                .build());

        ticketHistorialRepository.save(TicketHistorial.builder()
                .ticket(tk4029)
                .autorNombre("Sistema")
                .accion("Ticket asignado a Carlos R.")
                .detalle("Asignación automática según reglas de balanceo")
                .fechaCreacion(ahora.minusHours(2).minusMinutes(48))
                .build());

        ticketHistorialRepository.save(TicketHistorial.builder()
                .ticket(tk4029)
                .autorNombre("Laura M.")
                .accion("Ticket creado vía Portal Web")
                .detalle("Asunto: Error al acceder al sistema")
                .fechaCreacion(ahora.minusHours(3))
                .build());

        // TK-4028
        Ticket tk4028 = Ticket.builder()
                .codigo("TK-4028")
                .empresa(empresa)
                .cliente(globalCorp)
                .contacto(contactMaria)
                .agente(carlos)
                .asunto("Error al exportar reportes mensuales")
                .descripcion("El botón de exportar a Excel genera un archivo corrupto al superar 1000 filas.")
                .categoria(CategoriaTicket.SOFTWARE)
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .canalOrigen(CanalOrigen.EMAIL)
                .fechaCreacion(ahora.minusHours(5))
                .fechaLimitePrimeraRespuesta(ahora.minusHours(3))
                .fechaLimiteResolucion(ahora.plusHours(19))
                .build();
        ticketRepository.save(tk4028);

        // TK-4027
        Ticket tk4027 = Ticket.builder()
                .codigo("TK-4027")
                .empresa(empresa)
                .cliente(dataFlow)
                .contacto(contactPedro)
                .agente(laura)
                .asunto("Consulta sobre facturación de licencias extra")
                .descripcion("Solicitud de desglose del cobro adicional de este período.")
                .categoria(CategoriaTicket.FACTURACION)
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.RESUELTO)
                .canalOrigen(CanalOrigen.PORTAL_WEB)
                .fechaCreacion(ahora.minusDays(1))
                .fechaLimitePrimeraRespuesta(ahora.minusDays(1).plusHours(2))
                .fechaPrimeraRespuesta(ahora.minusDays(1).plusMinutes(30))
                .fechaLimiteResolucion(ahora.plusHours(1))
                .fechaResolucion(ahora.minusHours(10))
                .build();
        ticketRepository.save(tk4027);

        // TK-4026
        Ticket tk4026 = Ticket.builder()
                .codigo("TK-4026")
                .empresa(empresa)
                .cliente(techCorp)
                .agente(null)
                .asunto("Actualización de datos de la razón social")
                .descripcion("Requerimos cambiar la dirección fiscal registrada.")
                .categoria(CategoriaTicket.ADMINISTRACION)
                .prioridad(PrioridadTicket.BAJA)
                .estado(EstadoTicket.ABIERTO)
                .canalOrigen(CanalOrigen.PORTAL_WEB)
                .fechaCreacion(ahora.minusHours(6))
                .fechaLimitePrimeraRespuesta(ahora.minusHours(4))
                .fechaLimiteResolucion(ahora.plusHours(18))
                .build();
        ticketRepository.save(tk4026);

        // 5. Renovaciones
        Renovacion ren1 = Renovacion.builder()
                .empresa(empresa)
                .cliente(techCorp)
                .responsable(anaSilva)
                .servicio("Licencia Enterprise")
                .montoEstimado(new BigDecimal("12500.00"))
                .fechaVencimiento(hoy.plusDays(12))
                .estado(EstadoRenovacion.EN_NEGOCIACION)
                .build();
        ren1 = renovacionRepository.save(ren1);

        renovacionSeguimientoRepository.save(RenovacionSeguimiento.builder()
                .renovacion(ren1)
                .responsable(anaSilva)
                .fecha(hoy.minusDays(2))
                .estado(EstadoRenovacion.EN_NEGOCIACION)
                .comentario("Reunión sostenida con el Director de TI. Se presentó propuesta con 10% de descuento por contrato bianual.")
                .build());

        Renovacion ren2 = Renovacion.builder()
                .empresa(empresa)
                .cliente(globalLogistics)
                .responsable(carlos)
                .servicio("Soporte Premium 24/7")
                .montoEstimado(new BigDecimal("8400.00"))
                .fechaVencimiento(hoy.plusDays(25))
                .estado(EstadoRenovacion.PENDIENTE)
                .build();
        renovacionRepository.save(ren2);

        Renovacion ren3 = Renovacion.builder()
                .empresa(empresa)
                .cliente(grupoFinanciero)
                .responsable(laura)
                .servicio("Módulo Analytics")
                .montoEstimado(new BigDecimal("18000.00"))
                .fechaVencimiento(hoy.plusDays(32))
                .estado(EstadoRenovacion.RENOVADO)
                .build();
        renovacionRepository.save(ren3);

        Renovacion ren4 = Renovacion.builder()
                .empresa(empresa)
                .cliente(retailSolutions)
                .responsable(anaSilva)
                .servicio("Licencia Standard")
                .montoEstimado(new BigDecimal("6000.00"))
                .fechaVencimiento(hoy.plusDays(68))
                .estado(EstadoRenovacion.PENDIENTE)
                .build();
        renovacionRepository.save(ren4);

        log.info("Siembra de datos de demostración completada exitosamente.");
    }
}
