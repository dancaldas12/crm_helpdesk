package com.serviclient.crm.repository;

import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("dev")
class JpaRepositoriesTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EncuestaSatisfaccionRepository encuestaRepository;

    @Autowired
    private RenovacionRepository renovacionRepository;

    private Empresa empresa;
    private Usuario admin;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .nombreComercial("Test Corp")
                .razonSocial("Test Corp S.A.C.")
                .ruc("20123456789")
                .industria("Tecnología")
                .pais("Perú")
                .ciudad("Lima")
                .slaPrimerRespuestaHoras(2)
                .slaResolucionHoras(24)
                .activarCsat(true)
                .factorTicketsCriticos(true)
                .factorCsat(true)
                .factorRenovacionProxima(true)
                .build();
        empresa = entityManager.persistAndFlush(empresa);

        admin = Usuario.builder()
                .empresa(empresa)
                .nombre("Admin")
                .apellido("Test")
                .email("admin.test@testcorp.com")
                .password("$2a$10$abcdefghijklmnopqrstuv")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
        admin = entityManager.persistAndFlush(admin);

        cliente = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("Cliente Alpha")
                .razonSocial("Alpha S.A.")
                .ruc("20987654321")
                .responsable(admin)
                .servicioContratado("SaaS Premium")
                .estado(EstadoCliente.SALUDABLE)
                .healthScore(90)
                .csatPromedio(4.5)
                .fechaInicio(LocalDate.now().minusMonths(3))
                .fechaRenovacion(LocalDate.now().plusMonths(9))
                .build();
        cliente = entityManager.persistAndFlush(cliente);
    }

    @Test
    @DisplayName("Debe persistir y buscar empresa por ID y usuario por email")
    void testEmpresaYUsuarioRepository() {
        Optional<Empresa> empresaEncontrada = empresaRepository.findById(empresa.getId());
        assertTrue(empresaEncontrada.isPresent());
        assertEquals("Test Corp", empresaEncontrada.get().getNombreComercial());

        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail("admin.test@testcorp.com");
        assertTrue(usuarioEncontrado.isPresent());
        assertEquals(Rol.ADMIN, usuarioEncontrado.get().getRol());
        assertEquals(empresa.getId(), usuarioEncontrado.get().getEmpresa().getId());
    }

    @Test
    @DisplayName("Debe buscar clientes por empresa y aplicar filtros de búsqueda")
    void testClienteRepositoryFiltros() {
        Page<Cliente> resultado = clienteRepository.buscarConFiltros(
                empresa.getId(),
                "Alpha",
                EstadoCliente.SALUDABLE,
                PageRequest.of(0, 10)
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Cliente Alpha", resultado.getContent().get(0).getNombreComercial());

        long totalSaludables = clienteRepository.countByEmpresaIdAndEstado(empresa.getId(), EstadoCliente.SALUDABLE);
        assertEquals(1, totalSaludables);
    }

    @Test
    @DisplayName("Debe registrar y consultar tickets con SLA y filtros por estado y prioridad")
    void testTicketRepository() {
        Ticket ticket = Ticket.builder()
                .codigo("TK-9999")
                .empresa(empresa)
                .cliente(cliente)
                .agente(admin)
                .asunto("Incidencia crítica de servidor")
                .descripcion("El servidor no responde peticiones")
                .categoria(CategoriaTicket.INFRAESTRUCTURA)
                .prioridad(PrioridadTicket.CRITICA)
                .estado(EstadoTicket.ABIERTO)
                .fechaCreacion(LocalDateTime.now())
                .fechaLimitePrimeraRespuesta(LocalDateTime.now().plusHours(2))
                .fechaLimiteResolucion(LocalDateTime.now().plusHours(24))
                .build();
        entityManager.persistAndFlush(ticket);

        long criticosAbiertos = ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(
                cliente.getId(),
                PrioridadTicket.CRITICA,
                EstadoTicket.CERRADO
        );
        assertEquals(1, criticosAbiertos);

        long countPorEstado = ticketRepository.countByEmpresaIdAndEstado(empresa.getId(), EstadoTicket.ABIERTO);
        assertEquals(1, countPorEstado);
    }

    @Test
    @DisplayName("Debe calcular promedio de CSAT correctamente por cliente")
    void testEncuestaSatisfaccionPromedio() {
        Ticket ticket1 = Ticket.builder()
                .codigo("TK-1001")
                .empresa(empresa)
                .cliente(cliente)
                .asunto("Consulta 1")
                .descripcion("Detalle")
                .categoria(CategoriaTicket.SOPORTE_TECNICO)
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.CERRADO)
                .fechaCreacion(LocalDateTime.now().minusDays(2))
                .build();
        ticket1 = entityManager.persistAndFlush(ticket1);

        EncuestaSatisfaccion encuesta1 = EncuestaSatisfaccion.builder()
                .ticket(ticket1)
                .cliente(cliente)
                .puntuacion(5)
                .comentario("Excelente atención")
                .fechaCreacion(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(encuesta1);

        Double promedio = encuestaRepository.obtenerPromedioCsatPorCliente(cliente.getId());
        assertNotNull(promedio);
        assertEquals(5.0, promedio);
    }

    @Test
    @DisplayName("Debe listar renovaciones ordenadas por fecha de vencimiento")
    void testRenovacionRepository() {
        Renovacion renovacion = Renovacion.builder()
                .empresa(empresa)
                .cliente(cliente)
                .responsable(admin)
                .servicio("SaaS Anual")
                .fechaVencimiento(LocalDate.now().plusDays(20))
                .estado(EstadoRenovacion.EN_NEGOCIACION)
                .montoEstimado(BigDecimal.valueOf(12000.0))
                .build();
        entityManager.persistAndFlush(renovacion);

        List<Renovacion> lista = renovacionRepository.findByEmpresaIdOrderByFechaVencimientoAsc(empresa.getId());
        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        assertEquals(EstadoRenovacion.EN_NEGOCIACION, lista.get(0).getEstado());
    }
}
