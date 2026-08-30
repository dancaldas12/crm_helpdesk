package com.serviclient.crm.service;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.ClienteRepository;
import com.serviclient.crm.repository.EncuestaSatisfaccionRepository;
import com.serviclient.crm.repository.TicketRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthScoreService {

    private final TicketRepository ticketRepository;
    private final EncuestaSatisfaccionRepository encuestaRepository;
    private final ClienteRepository clienteRepository;

    @Getter
    @Builder
    public static class FactorSalud {
        private String titulo;
        private String descripcion;
        private String tipo; // "CRITICO", "WARNING", "INFO"
        private String iconoClass;
    }

    @Transactional
    public int recalcularYActualizarHealthScore(Cliente cliente) {
        Empresa empresa = cliente.getEmpresa();
        
        // Base score = 100
        double score = 100.0;

        // Factor 1: Tickets críticos abiertos
        long ticketsCriticos = ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(
                cliente.getId(), PrioridadTicket.CRITICA, EstadoTicket.CERRADO
        );
        if (ticketsCriticos > 0 && (empresa == null || Boolean.TRUE.equals(empresa.getFactorTicketsCriticos()))) {
            score -= Math.min(40.0, ticketsCriticos * 20.0);
        }

        // Factor 2: CSAT
        Double csatPromedio = encuestaRepository.obtenerPromedioCsatPorCliente(cliente.getId());
        if (csatPromedio != null && (empresa == null || Boolean.TRUE.equals(empresa.getFactorCsat()))) {
            cliente.setCsatPromedio(csatPromedio);
            if (csatPromedio < 3.0) {
                score -= 30.0;
            } else if (csatPromedio < 4.0) {
                score -= 15.0;
            }
        }

        // Factor 3: Días para renovación
        long diasRenovacion = cliente.getDiasParaRenovacion();
        if (diasRenovacion <= 30 && (empresa == null || Boolean.TRUE.equals(empresa.getFactorRenovacionProxima()))) {
            score -= 15.0;
        }

        int finalScore = (int) Math.max(0, Math.min(100, Math.round(score)));
        cliente.setHealthScore(finalScore);

        if (finalScore >= 70) {
            cliente.setEstado(EstadoCliente.SALUDABLE);
        } else if (finalScore >= 50) {
            cliente.setEstado(EstadoCliente.OBSERVACION);
        } else {
            cliente.setEstado(EstadoCliente.EN_RIESGO);
        }

        clienteRepository.save(cliente);
        return finalScore;
    }

    public List<FactorSalud> obtenerFactoresRiesgo(Cliente cliente) {
        List<FactorSalud> factores = new ArrayList<>();

        // Verificar tickets críticos
        long ticketsCriticos = ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(
                cliente.getId(), PrioridadTicket.CRITICA, EstadoTicket.CERRADO
        );
        if (ticketsCriticos > 0) {
            factores.add(FactorSalud.builder()
                    .titulo("Ticket crítico pendiente")
                    .descripcion("SLA en riesgo de incumplimiento (" + ticketsCriticos + " ticket(s) crítico(s))")
                    .tipo("CRITICO")
                    .iconoClass("text-rose-500 bg-rose-100")
                    .build());
        }

        // Verificar CSAT
        Double csat = cliente.getCsatPromedio();
        if (csat != null && csat < 4.0) {
            factores.add(FactorSalud.builder()
                    .titulo("CSAT bajo")
                    .descripcion("Última encuesta calificada con " + String.format("%.1f", csat) + " / 5.0")
                    .tipo("WARNING")
                    .iconoClass("text-amber-500 bg-amber-100")
                    .build());
        }

        // Verificar renovación
        long dias = cliente.getDiasParaRenovacion();
        if (dias >= 0 && dias <= 30) {
            factores.add(FactorSalud.builder()
                    .titulo("Renovación próxima")
                    .descripcion("Faltan " + dias + " días, sin confirmación final")
                    .tipo("INFO")
                    .iconoClass("text-indigo-500 bg-indigo-100")
                    .build());
        }

        return factores;
    }
}
