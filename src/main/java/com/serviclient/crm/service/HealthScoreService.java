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

/**
 * Servicio que implementa el motor de cálculo del <b>Health Score</b> del cliente.
 *
 * <p>El Health Score es un indicador numérico de 0 a 100 que refleja el nivel de salud
 * de la relación con un cliente B2B. Se calcula a partir de tres factores ponderados:</p>
 * <ol>
 *   <li><b>Tickets críticos abiertos</b> — descuenta hasta 40 puntos (20 por cada ticket crítico).</li>
 *   <li><b>CSAT promedio</b> — descuenta 30 puntos si &lt; 3.0, o 15 puntos si &lt; 4.0.</li>
 *   <li><b>Renovación próxima</b> — descuenta 15 puntos si vence en &lt;= 30 días.</li>
 * </ol>
 *
 * <p><b>Umbrales de estado:</b></p>
 * <ul>
 *   <li>Score &ge; 70 &rarr; {@code SALUDABLE}</li>
 *   <li>Score &ge; 50 &rarr; {@code OBSERVACION}</li>
 *   <li>Score &lt; 50 &rarr; {@code EN_RIESGO}</li>
 * </ul>
 *
 * <p>Cada factor puede activarse o desactivarse mediante la configuración de la
 * {@link com.serviclient.crm.entity.Empresa} (atributos {@code factorTicketsCriticos},
 * {@code factorCsat}, {@code factorRenovacionProxima}).</p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.entity.HealthScoreHistorial
 * @see com.serviclient.crm.entity.ConfiguracionHealthScore
 */
@Service
@RequiredArgsConstructor
public class HealthScoreService {

    private final TicketRepository ticketRepository;
    private final EncuestaSatisfaccionRepository encuestaRepository;
    private final ClienteRepository clienteRepository;

    /**
     * Representa un factor de riesgo identificado durante el análisis del Health Score.
     * Utilizado en la Vista 360° del cliente para mostrar alertas visuales contextualizadas.
     */
    @Getter
    @Builder
    public static class FactorSalud {
        /** Título breve del factor de riesgo (p.ej. "Ticket crítico pendiente"). */
        private String titulo;
        /** Descripción detallada del factor con valores concretos. */
        private String descripcion;
        /** Nivel de severidad: {@code "CRITICO"}, {@code "WARNING"} o {@code "INFO"}. */
        private String tipo;
        /** Clases CSS de Tailwind para el icono de la alerta (color + fondo). */
        private String iconoClass;
    }

    /**
     * Recalcula el Health Score del cliente, actualiza su estado de salud
     * y persiste los cambios en la base de datos.
     *
     * <p>Se llama automáticamente tras cerrar un ticket, registrar un seguimiento
     * de renovación o cualquier evento que pueda afectar la salud del cliente.</p>
     *
     * @param cliente entidad cliente sobre la que se calcula el score
     * @return el nuevo puntaje calculado (0-100)
     */
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

    /**
     * Identifica y retorna los factores de riesgo activos para el cliente dado,
     * listos para ser mostrados en la Vista 360° como alertas visuales.
     *
     * <p>Un factor se incluye solo si su condición de riesgo es verdadera:
     * tickets críticos &gt; 0, CSAT &lt; 4.0, o renovación en &le; 30 días.</p>
     *
     * @param cliente entidad cliente a analizar
     * @return lista de {@link FactorSalud} con los riesgos detectados (puede estar vacía)
     */
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
