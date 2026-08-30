package com.serviclient.crm;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.ClienteRepository;
import com.serviclient.crm.repository.EncuestaSatisfaccionRepository;
import com.serviclient.crm.repository.TicketRepository;
import com.serviclient.crm.service.HealthScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthScoreServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EncuestaSatisfaccionRepository encuestaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private HealthScoreService healthScoreService;

    private Empresa empresa;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nombreComercial("ServiClient")
                .factorTicketsCriticos(true)
                .factorCsat(true)
                .factorRenovacionProxima(true)
                .build();

        cliente = Cliente.builder()
                .id(10L)
                .empresa(empresa)
                .nombreComercial("NovaTech Solutions")
                .fechaInicio(LocalDate.now().minusMonths(6))
                .fechaRenovacion(LocalDate.now().plusDays(15))
                .healthScore(80)
                .build();
    }

    @Test
    void testRecalcularHealthScoreEnRiesgo() {
        // Mock: 2 tickets críticos abiertos
        when(ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(eq(10L), eq(PrioridadTicket.CRITICA), any()))
                .thenReturn(2L);
        // Mock: CSAT bajo de 2.5
        when(encuestaRepository.obtenerPromedioCsatPorCliente(10L))
                .thenReturn(2.5);

        int score = healthScoreService.recalcularYActualizarHealthScore(cliente);

        // 100 - (2*20=40) - 30 (CSAT < 3.0) - 15 (Renovacion <= 30 dias) = 15
        assertEquals(15, score);
        assertEquals(EstadoCliente.EN_RIESGO, cliente.getEstado());
    }

    @Test
    void testFactoresDeRiesgoDetectados() {
        when(ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(eq(10L), eq(PrioridadTicket.CRITICA), any()))
                .thenReturn(1L);
        cliente.setCsatPromedio(3.5);

        List<HealthScoreService.FactorSalud> factores = healthScoreService.obtenerFactoresRiesgo(cliente);

        assertFalse(factores.isEmpty());
        assertTrue(factores.stream().anyMatch(f -> f.getTitulo().equals("Ticket crítico pendiente")));
        assertTrue(factores.stream().anyMatch(f -> f.getTitulo().equals("CSAT bajo")));
        assertTrue(factores.stream().anyMatch(f -> f.getTitulo().equals("Renovación próxima")));
    }
}
