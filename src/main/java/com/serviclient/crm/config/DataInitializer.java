package com.serviclient.crm.config;

import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.*;
import com.serviclient.crm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final ContactoRepository contactoRepository;
    private final ClienteServicioRepository clienteServicioRepository;
    private final CategoriaTicketRepository categoriaTicketRepository;
    private final ConfiguracionSlaRepository configuracionSlaRepository;
    private final ConfiguracionCsatRepository configuracionCsatRepository;
    private final ConfiguracionHealthScoreRepository configuracionHealthScoreRepository;
    private final TicketRepository ticketRepository;
    private final TicketMensajeRepository ticketMensajeRepository;
    private final TicketHistorialRepository ticketHistorialRepository;
    private final TicketAdjuntoRepository ticketAdjuntoRepository;
    private final RenovacionRepository renovacionRepository;
    private final RenovacionSeguimientoRepository renovacionSeguimientoRepository;
    private final EncuestaCsatRepository encuestaCsatRepository;
    private final RespuestaCsatRepository respuestaCsatRepository;
    private final EncuestaSatisfaccionRepository encuestaRepository;
    private final HealthScoreHistorialRepository healthScoreHistorialRepository;
    private final InteraccionRepository interaccionRepository;
    private final NotificacionRepository notificacionRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (empresaRepository.count() > 0) {
            log.info("Base de datos ya contiene datos inicializados.");
            return;
        }

        log.info("Inicializando datos acordes a la estructura de base de datos MySQL (CRM_MESA_AYUDA)...");

        // 1. Empresa Principal
        Empresa empresa = Empresa.builder()
                .nombreComercial("ServiClient")
                .razonSocial("ServiClient Cloud Solutions S.A.C.")
                .ruc("20601234567")
                .industria("Tecnología & SaaS")
                .tamanoEmpresa("50-200")
                .pais("Perú")
                .ciudad("Lima")
                .direccion("Av. Javier Prado Este 4200, San Isidro")
                .sitioWeb("https://serviclient.com")
                .zonaHoraria("America/Lima")
                .estado("ACTIVA")
                .build();
        empresa = empresaRepository.save(empresa);

        // 2. Permisos
        String[] modulos = {"CLIENTES", "TICKETS", "RENOVACIONES", "CSAT", "CONFIGURACION"};
        String[] acciones = {"VER", "CREAR", "EDITAR", "ELIMINAR"};
        Set<Permiso> todosPermisos = new HashSet<>();
        for (String m : modulos) {
            for (String a : acciones) {
                Permiso p = Permiso.builder()
                        .nombre(m + "_" + a)
                        .descripcion("Permite " + a.toLowerCase() + " en el módulo " + m.toLowerCase())
                        .modulo(m)
                        .accion(a)
                        .build();
                todosPermisos.add(permisoRepository.save(p));
            }
        }

        // 3. Roles
        RolEntity rolAdmin = RolEntity.builder()
                .empresa(empresa)
                .nombre("Administrador")
                .descripcion("Acceso total y configuración del sistema")
                .estado("ACTIVO")
                .permisos(todosPermisos)
                .build();
        rolAdmin = rolRepository.save(rolAdmin);

        RolEntity rolAgente = RolEntity.builder()
                .empresa(empresa)
                .nombre("Agente de Soporte")
                .descripcion("Atención y resolución de tickets e incidencias")
                .estado("ACTIVO")
                .permisos(todosPermisos)
                .build();
        rolAgente = rolRepository.save(rolAgente);

        RolEntity rolSupervisor = RolEntity.builder()
                .empresa(empresa)
                .nombre("Supervisor Customer Success")
                .descripcion("Gestión de salud de clientes, CSAT y renovaciones")
                .estado("ACTIVO")
                .permisos(todosPermisos)
                .build();
        rolSupervisor = rolRepository.save(rolSupervisor);

        // 4. Usuarios
        Usuario admin = Usuario.builder()
                .empresa(empresa)
                .rolEntity(rolAdmin)
                .nombre("Juan")
                .apellido("Pérez")
                .email("admin@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .telefono("+51 987 654 321")
                .estado("ACTIVO")
                .ultimoAcceso(LocalDateTime.now())
                .build();
        admin = usuarioRepository.save(admin);

        Usuario carlos = Usuario.builder()
                .empresa(empresa)
                .rolEntity(rolAgente)
                .nombre("Carlos")
                .apellido("Ruiz")
                .email("carlos.ruiz@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .telefono("+51 981 111 222")
                .estado("ACTIVO")
                .ultimoAcceso(LocalDateTime.now().minusHours(2))
                .build();
        carlos = usuarioRepository.save(carlos);

        Usuario ana = Usuario.builder()
                .empresa(empresa)
                .rolEntity(rolAgente)
                .nombre("Ana")
                .apellido("García")
                .email("ana.garcia@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .telefono("+51 982 333 444")
                .estado("ACTIVO")
                .ultimoAcceso(LocalDateTime.now().minusHours(1))
                .build();
        ana = usuarioRepository.save(ana);

        Usuario laura = Usuario.builder()
                .empresa(empresa)
                .rolEntity(rolSupervisor)
                .nombre("Laura")
                .apellido("Mendoza")
                .email("laura.mendoza@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .telefono("+51 983 555 666")
                .estado("ACTIVO")
                .ultimoAcceso(LocalDateTime.now().minusMinutes(30))
                .build();
        laura = usuarioRepository.save(laura);

        // 5. Servicios ofrecidos
        Servicio srvErp = servicioRepository.save(Servicio.builder()
                .empresa(empresa)
                .nombre("ERP Cloud Enterprise")
                .descripcion("Sistema integral de planificación de recursos empresariales en la nube")
                .codigo("SRV-ERP")
                .estado("ACTIVO")
                .build());

        Servicio srvHelpdesk = servicioRepository.save(Servicio.builder()
                .empresa(empresa)
                .nombre("Helpdesk Omni-channel")
                .descripcion("Plataforma de soporte y atención a usuarios multicanal")
                .codigo("SRV-HD")
                .estado("ACTIVO")
                .build());

        Servicio srvCrm = servicioRepository.save(Servicio.builder()
                .empresa(empresa)
                .nombre("CRM Ventas Pro")
                .descripcion("Gestión de relaciones comerciales y pipeline de ventas")
                .codigo("SRV-CRM")
                .estado("ACTIVO")
                .build());

        Servicio srvAnalytics = servicioRepository.save(Servicio.builder()
                .empresa(empresa)
                .nombre("Analítica Avanzada & IA")
                .descripcion("Módulo de analítica predictiva y dashboards ejecutivos")
                .codigo("SRV-AI")
                .estado("ACTIVO")
                .build());

        // 6. Categorías de Tickets
        CategoriaTicketEntity catSoporte = categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                .empresa(empresa)
                .nombre("Soporte Técnico")
                .descripcion("Problemas y consultas de operación técnica")
                .estado("ACTIVO")
                .build());

        CategoriaTicketEntity catFacturacion = categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                .empresa(empresa)
                .nombre("Facturación & Cobranzas")
                .descripcion("Consultas de pagos, recibos y contratos")
                .estado("ACTIVO")
                .build());

        CategoriaTicketEntity catOnboarding = categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                .empresa(empresa)
                .nombre("Capacitación & Onboarding")
                .descripcion("Entrenamiento e inducción para nuevos usuarios")
                .estado("ACTIVO")
                .build());

        CategoriaTicketEntity catFeature = categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                .empresa(empresa)
                .nombre("Solicitud de Feature")
                .descripcion("Requerimientos y mejoras solicitadas")
                .estado("ACTIVO")
                .build());

        CategoriaTicketEntity catCritica = categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                .empresa(empresa)
                .nombre("Incidencia Crítica")
                .descripcion("Interrupción de servicios críticos")
                .estado("ACTIVO")
                .build());

        // 7. Configuración SLA
        configuracionSlaRepository.save(ConfiguracionSla.builder()
                .empresa(empresa)
                .prioridad(PrioridadTicket.CRITICA)
                .tiempoRespuestaMinutos(30)
                .tiempoResolucionMinutos(240) // 4 horas
                .estado("ACTIVO")
                .build());

        configuracionSlaRepository.save(ConfiguracionSla.builder()
                .empresa(empresa)
                .prioridad(PrioridadTicket.ALTA)
                .tiempoRespuestaMinutos(60)
                .tiempoResolucionMinutos(480) // 8 horas
                .estado("ACTIVO")
                .build());

        configuracionSlaRepository.save(ConfiguracionSla.builder()
                .empresa(empresa)
                .prioridad(PrioridadTicket.MEDIA)
                .tiempoRespuestaMinutos(120)
                .tiempoResolucionMinutos(1440) // 24 horas
                .estado("ACTIVO")
                .build());

        configuracionSlaRepository.save(ConfiguracionSla.builder()
                .empresa(empresa)
                .prioridad(PrioridadTicket.BAJA)
                .tiempoRespuestaMinutos(240)
                .tiempoResolucionMinutos(2880) // 48 horas
                .estado("ACTIVO")
                .build());

        // 8. Configuración CSAT
        configuracionCsatRepository.save(ConfiguracionCsat.builder()
                .empresa(empresa)
                .activo(true)
                .escalaMin(1)
                .escalaMax(5)
                .pregunta("¿Qué tan satisfecho estás con la atención recibida?")
                .mensajeAgradecimiento("Gracias por compartir tu opinión con ServiClient.")
                .build());

        // 9. Configuración Health Score
        configuracionHealthScoreRepository.save(ConfiguracionHealthScore.builder()
                .empresa(empresa)
                .pesoTickets(new BigDecimal("30.00"))
                .pesoCsat(new BigDecimal("30.00"))
                .pesoActividad(new BigDecimal("15.00"))
                .pesoRenovacion(new BigDecimal("25.00"))
                .rangoSaludableMin(80)
                .rangoObservacionMin(50)
                .estado("ACTIVO")
                .build());

        // 10. Clientes
        // Cliente 1: TechCorp Solutions (Saludable)
        Cliente c1 = clienteRepository.save(Cliente.builder()
                .empresa(empresa)
                .responsable(laura)
                .nombreComercial("TechCorp Solutions")
                .razonSocial("TechCorp Solutions S.A.C.")
                .ruc("20554433221")
                .industria("Fintech")
                .tamanoEmpresa("200-500")
                .pais("Perú")
                .ciudad("Lima")
                .direccion("Av. Canaval y Moreyra 480, San Isidro")
                .sitioWeb("https://techcorp.pe")
                .estado("ACTIVO")
                .build());

        Contacto cont1 = contactoRepository.save(Contacto.builder()
                .cliente(c1)
                .nombre("Roberto")
                .apellido("Gómez")
                .cargo("CTO")
                .email("roberto.gomez@techcorp.pe")
                .telefono("+51 999 111 222")
                .esContactoPrincipal(true)
                .estado("ACTIVO")
                .build());

        clienteServicioRepository.save(ClienteServicio.builder()
                .cliente(c1)
                .servicio(srvErp)
                .fechaInicio(LocalDate.now().minusMonths(11))
                .fechaFin(LocalDate.now().plusMonths(1))
                .estado(EstadoServicioCliente.ACTIVO)
                .build());

        healthScoreHistorialRepository.save(HealthScoreHistorial.builder()
                .cliente(c1)
                .puntaje(new BigDecimal("92.00"))
                .estado(EstadoSaludCliente.SALUDABLE)
                .puntajeTickets(new BigDecimal("30.00"))
                .puntajeCsat(new BigDecimal("28.00"))
                .puntajeActividad(new BigDecimal("14.00"))
                .puntajeRenovacion(new BigDecimal("20.00"))
                .build());

        // Cliente 2: Retail Express (En Observación)
        Cliente c2 = clienteRepository.save(Cliente.builder()
                .empresa(empresa)
                .responsable(carlos)
                .nombreComercial("Retail Express Perú")
                .razonSocial("Retail Express del Perú S.A.")
                .ruc("20443322110")
                .industria("Retail & E-commerce")
                .tamanoEmpresa("100-250")
                .pais("Perú")
                .ciudad("Lima")
                .direccion("Av. Larco 743, Miraflores")
                .sitioWeb("https://retailexpress.pe")
                .estado("ACTIVO")
                .build());

        Contacto cont2 = contactoRepository.save(Contacto.builder()
                .cliente(c2)
                .nombre("Mariana")
                .apellido("Vargas")
                .cargo("Líder de Soporte y Operaciones")
                .email("m.vargas@retailexpress.pe")
                .telefono("+51 988 222 333")
                .esContactoPrincipal(true)
                .estado("ACTIVO")
                .build());

        clienteServicioRepository.save(ClienteServicio.builder()
                .cliente(c2)
                .servicio(srvHelpdesk)
                .fechaInicio(LocalDate.now().minusMonths(8))
                .fechaFin(LocalDate.now().plusDays(24))
                .estado(EstadoServicioCliente.ACTIVO)
                .build());

        healthScoreHistorialRepository.save(HealthScoreHistorial.builder()
                .cliente(c2)
                .puntaje(new BigDecimal("64.00"))
                .estado(EstadoSaludCliente.EN_OBSERVACION)
                .puntajeTickets(new BigDecimal("15.00"))
                .puntajeCsat(new BigDecimal("20.00"))
                .puntajeActividad(new BigDecimal("10.00"))
                .puntajeRenovacion(new BigDecimal("19.00"))
                .build());

        // Cliente 3: Banco Metropolitano (En Riesgo)
        Cliente c3 = clienteRepository.save(Cliente.builder()
                .empresa(empresa)
                .responsable(laura)
                .nombreComercial("Banco Metropolitano")
                .razonSocial("Banco Metropolitano S.A.")
                .ruc("20112233445")
                .industria("Banca & Seguros")
                .tamanoEmpresa("1000+")
                .pais("Perú")
                .ciudad("Lima")
                .direccion("Av. República de Panamá 3055, San Isidro")
                .sitioWeb("https://bancometropolitano.pe")
                .estado("ACTIVO")
                .build());

        Contacto cont3 = contactoRepository.save(Contacto.builder()
                .cliente(c3)
                .nombre("Fernando")
                .apellido("Castro")
                .cargo("Gerente de Infraestructura TI")
                .email("f.castro@bancometropolitano.pe")
                .telefono("+51 977 333 444")
                .esContactoPrincipal(true)
                .estado("ACTIVO")
                .build());

        clienteServicioRepository.save(ClienteServicio.builder()
                .cliente(c3)
                .servicio(srvCrm)
                .fechaInicio(LocalDate.now().minusMonths(11))
                .fechaFin(LocalDate.now().plusDays(12))
                .estado(EstadoServicioCliente.ACTIVO)
                .build());

        healthScoreHistorialRepository.save(HealthScoreHistorial.builder()
                .cliente(c3)
                .puntaje(new BigDecimal("42.00"))
                .estado(EstadoSaludCliente.EN_RIESGO)
                .puntajeTickets(new BigDecimal("5.00"))
                .puntajeCsat(new BigDecimal("12.00"))
                .puntajeActividad(new BigDecimal("10.00"))
                .puntajeRenovacion(new BigDecimal("15.00"))
                .build());

        // Cliente 4: Minera Los Andes (Saludable)
        Cliente c4 = clienteRepository.save(Cliente.builder()
                .empresa(empresa)
                .responsable(ana)
                .nombreComercial("Minera Los Andes")
                .razonSocial("Compañía Minera Los Andes S.A.C.")
                .ruc("20998877665")
                .industria("Minería & Energía")
                .tamanoEmpresa("500-1000")
                .pais("Perú")
                .ciudad("Arequipa")
                .direccion("Av. Cayma 600, Arequipa")
                .sitioWeb("https://minerala.pe")
                .estado("ACTIVO")
                .build());

        Contacto cont4 = contactoRepository.save(Contacto.builder()
                .cliente(c4)
                .nombre("Lucía")
                .apellido("Salazar")
                .cargo("Jefa de Sistemas")
                .email("lucia.salazar@minerala.pe")
                .telefono("+51 966 444 555")
                .esContactoPrincipal(true)
                .estado("ACTIVO")
                .build());

        clienteServicioRepository.save(ClienteServicio.builder()
                .cliente(c4)
                .servicio(srvAnalytics)
                .fechaInicio(LocalDate.now().minusMonths(4))
                .fechaFin(LocalDate.now().plusMonths(8))
                .estado(EstadoServicioCliente.ACTIVO)
                .build());

        healthScoreHistorialRepository.save(HealthScoreHistorial.builder()
                .cliente(c4)
                .puntaje(new BigDecimal("88.00"))
                .estado(EstadoSaludCliente.SALUDABLE)
                .puntajeTickets(new BigDecimal("28.00"))
                .puntajeCsat(new BigDecimal("28.00"))
                .puntajeActividad(new BigDecimal("12.00"))
                .puntajeRenovacion(new BigDecimal("20.00"))
                .build());

        // 11. Interacciones Cliente 360°
        interaccionRepository.save(Interaccion.builder()
                .cliente(c1)
                .usuario(laura)
                .tipo(TipoInteraccion.REUNION)
                .titulo("Revisión Trimestral Q3 (QBR)")
                .descripcion("Reunión ejecutiva con el CTO Roberto Gómez. Muy satisfechos con el uptime del ERP.")
                .fechaInteraccion(LocalDateTime.now().minusDays(5))
                .build());

        interaccionRepository.save(Interaccion.builder()
                .cliente(c2)
                .usuario(carlos)
                .tipo(TipoInteraccion.LLAMADA)
                .titulo("Seguimiento de Incidencia en Integración")
                .descripcion("Se coordinó soporte extendido para resolver latencias en las consultas API de ventas.")
                .fechaInteraccion(LocalDateTime.now().minusDays(2))
                .build());

        interaccionRepository.save(Interaccion.builder()
                .cliente(c3)
                .usuario(laura)
                .tipo(TipoInteraccion.SEGUIMIENTO)
                .titulo("Comité de Crisis por Caída de Módulo")
                .descripcion("Reunión urgente con el Gerente TI Fernando Castro. Se estableció plan de contingencia.")
                .fechaInteraccion(LocalDateTime.now().minusDays(1))
                .build());

        // 12. Tickets de Mesa de Ayuda
        // Ticket 1 (Abierto / Crítico)
        Ticket t1 = ticketRepository.save(Ticket.builder()
                .numeroTicket("TK-4029")
                .empresa(empresa)
                .cliente(c3)
                .contacto(cont3)
                .categoriaEntity(catCritica)
                .agente(carlos)
                .asunto("Falla en módulo de Facturación Electrónica SUNAT")
                .descripcion("No se pueden emitir comprobantes desde las 08:30 AM. Error HTTP 500 al comunicarse con el web service del proveedor.")
                .prioridad(PrioridadTicket.CRITICA)
                .estado(EstadoTicket.ABIERTO)
                .slaRespuestaLimite(LocalDateTime.now().plusMinutes(25))
                .slaResolucionLimite(LocalDateTime.now().plusHours(3))
                .createdAt(LocalDateTime.now().minusMinutes(35))
                .build());

        ticketMensajeRepository.save(TicketMensaje.builder()
                .ticket(t1)
                .contacto(cont3)
                .tipo(TipoMensajeTicket.CLIENTE)
                .mensaje("No se pueden emitir comprobantes desde las 08:30 AM. Error HTTP 500 al comunicarse con el web service del proveedor.")
                .createdAt(LocalDateTime.now().minusMinutes(35))
                .build());

        ticketHistorialRepository.save(TicketHistorial.builder()
                .ticket(t1)
                .usuario(admin)
                .tipoEvento("CREACION")
                .descripcion("Ticket creado desde Portal Web")
                .createdAt(LocalDateTime.now().minusMinutes(35))
                .build());

        // Ticket 2 (En Proceso / Alta)
        Ticket t2 = ticketRepository.save(Ticket.builder()
                .numeroTicket("TK-4028")
                .empresa(empresa)
                .cliente(c2)
                .contacto(cont2)
                .categoriaEntity(catSoporte)
                .agente(ana)
                .asunto("Lentitud en la generación de reportes consolidados")
                .descripcion("Los reportes mensuales de inventario tardan más de 10 minutos en exportar a Excel.")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.EN_PROCESO)
                .slaRespuestaLimite(LocalDateTime.now().minusHours(1))
                .fechaPrimeraRespuesta(LocalDateTime.now().minusHours(2))
                .slaResolucionLimite(LocalDateTime.now().plusHours(5))
                .createdAt(LocalDateTime.now().minusHours(3))
                .build());

        ticketMensajeRepository.save(TicketMensaje.builder()
                .ticket(t2)
                .contacto(cont2)
                .tipo(TipoMensajeTicket.CLIENTE)
                .mensaje("Los reportes mensuales de inventario tardan más de 10 minutos en exportar a Excel.")
                .createdAt(LocalDateTime.now().minusHours(3))
                .build());

        ticketMensajeRepository.save(TicketMensaje.builder()
                .ticket(t2)
                .usuario(ana)
                .tipo(TipoMensajeTicket.AGENTE)
                .mensaje("Hola Mariana, estamos analizando las consultas a la base de datos para optimizar los índices de exportación.")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build());

        ticketHistorialRepository.save(TicketHistorial.builder()
                .ticket(t2)
                .usuario(ana)
                .tipoEvento("RESPUESTA")
                .descripcion("Respuesta enviada al cliente")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build());

        // Ticket 3 (Resuelto / Media con Encuesta CSAT)
        Ticket t3 = ticketRepository.save(Ticket.builder()
                .numeroTicket("TK-4025")
                .empresa(empresa)
                .cliente(c1)
                .contacto(cont1)
                .categoriaEntity(catOnboarding)
                .agente(ana)
                .asunto("Configuración de nuevo usuario administrador")
                .descripcion("Requerimos habilitar credenciales de acceso para el nuevo analista financiero.")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.RESUELTO)
                .slaRespuestaLimite(LocalDateTime.now().minusDays(1))
                .fechaPrimeraRespuesta(LocalDateTime.now().minusDays(1).plusMinutes(15))
                .slaResolucionLimite(LocalDateTime.now().minusHours(10))
                .fechaResolucion(LocalDateTime.now().minusHours(12))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build());

        EncuestaCsat encCsat = encuestaCsatRepository.save(EncuestaCsat.builder()
                .empresa(empresa)
                .cliente(c1)
                .contacto(cont1)
                .ticket(t3)
                .estado(EstadoEncuestaCsat.RESPONDIDA)
                .fechaEnvio(LocalDateTime.now().minusHours(12))
                .fechaRespuesta(LocalDateTime.now().minusHours(11))
                .build());

        respuestaCsatRepository.save(RespuestaCsat.builder()
                .encuesta(encCsat)
                .puntuacion(5)
                .comentario("Excelente soporte, todo quedó habilitado en minutos.")
                .build());

        encuestaRepository.save(EncuestaSatisfaccion.builder()
                .ticket(t3)
                .cliente(c1)
                .contacto(cont1)
                .puntuacion(5)
                .comentario("Excelente soporte, todo quedó habilitado en minutos.")
                .fechaCreacion(LocalDateTime.now().minusHours(11))
                .build());

        // 13. Renovaciones
        Renovacion ren1 = renovacionRepository.save(Renovacion.builder()
                .empresa(empresa)
                .cliente(c3)
                .clienteServicio(clienteServicioRepository.findByClienteId(c3.getId()).get(0))
                .responsable(laura)
                .fechaVencimiento(LocalDate.now().plusDays(12))
                .diasAlerta(30)
                .estado(EstadoRenovacion.EN_NEGOCIACION)
                .notas("Negociación crítica: Cliente insatisfecho con recientes caídas. Se ofreció SLA 99.9% y 10% descuento.")
                .build());

        renovacionSeguimientoRepository.save(RenovacionSeguimiento.builder()
                .renovacion(ren1)
                .usuario(laura)
                .fecha(LocalDateTime.now().minusDays(3))
                .estado(EstadoRenovacion.EN_NEGOCIACION)
                .comentario("Se envió propuesta comercial actualizada con addendum de soporte prioritario 24/7.")
                .build());

        Renovacion ren2 = renovacionRepository.save(Renovacion.builder()
                .empresa(empresa)
                .cliente(c2)
                .clienteServicio(clienteServicioRepository.findByClienteId(c2.getId()).get(0))
                .responsable(carlos)
                .fechaVencimiento(LocalDate.now().plusDays(24))
                .diasAlerta(30)
                .estado(EstadoRenovacion.PENDIENTE)
                .notas("Próxima a vencer en 24 días. Contactar a Mariana Vargas.")
                .build());

        Renovacion ren3 = renovacionRepository.save(Renovacion.builder()
                .empresa(empresa)
                .cliente(c1)
                .clienteServicio(clienteServicioRepository.findByClienteId(c1.getId()).get(0))
                .responsable(laura)
                .fechaVencimiento(LocalDate.now().plusMonths(1))
                .diasAlerta(30)
                .estado(EstadoRenovacion.RENOVADO)
                .fechaRenovacion(LocalDate.now().minusDays(2))
                .notas("Renovado por 12 meses adicionales con expansión de 50 licencias.")
                .build());

        // 14. Notificaciones
        notificacionRepository.save(Notificacion.builder()
                .empresa(empresa)
                .usuario(admin)
                .tipo(TipoNotificacion.CLIENTE_EN_RIESGO)
                .titulo("Alerta: Banco Metropolitano en Riesgo")
                .mensaje("El Health Score de Banco Metropolitano cayó a 42 debido a tickets críticos abiertos.")
                .entidadTipo("CLIENTE")
                .entidadId(c3.getId())
                .leida(false)
                .build());

        notificacionRepository.save(Notificacion.builder()
                .empresa(empresa)
                .usuario(carlos)
                .tipo(TipoNotificacion.TICKET_ASIGNADO)
                .titulo("Nuevo Ticket Asignado: TK-4029")
                .mensaje("Se te ha asignado el ticket crítico 'Falla en módulo de Facturación Electrónica SUNAT'.")
                .entidadTipo("TICKET")
                .entidadId(t1.getId())
                .leida(false)
                .build());

        // 15. Auditoría
        auditoriaRepository.save(Auditoria.builder()
                .empresa(empresa)
                .usuario(admin)
                .accion(AccionAuditoria.LOGIN)
                .entidad("USUARIOS")
                .registroId(admin.getId())
                .direccionIp("127.0.0.1")
                .build());

        log.info("[✓] Datos de demostración inicializados con éxito en las 25 tablas de MySQL.");
    }
}
