package com.serviclient.crm.service;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.ClienteServicio;
import com.serviclient.crm.entity.HealthScoreHistorial;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import com.serviclient.crm.entity.enums.EstadoSaludCliente;
import com.serviclient.crm.entity.enums.EstadoServicioCliente;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.ClienteRepository;
import com.serviclient.crm.repository.ClienteServicioRepository;
import com.serviclient.crm.repository.EncuestaCsatRepository;
import com.serviclient.crm.repository.HealthScoreHistorialRepository;
import com.serviclient.crm.repository.RenovacionRepository;
import com.serviclient.crm.repository.TicketRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de cálculo algorítmico y evaluación del Health Score del cliente.
 *
 * <p>Modela la salud del cliente en una escala normalizada de 0 a 100 puntos evaluando variables
 * operativas como tickets abiertos, incidencias críticas sin resolver y satisfacción promedio
 * (CSAT). Además, persiste el registro histórico de salud y clasifica al cliente en categorías:
 * SALUDABLE (80-100), EN_OBSERVACION (50-79) o EN_RIESGO (0-49).
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see IHealthScoreService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthScoreService implements IHealthScoreService {

  private final ClienteRepository clienteRepository;
  private final TicketRepository ticketRepository;
  private final EncuestaCsatRepository encuestaCsatRepository;
  private final HealthScoreHistorialRepository healthScoreHistorialRepository;
  private final NotificacionService notificacionService;
  private final RenovacionRepository renovacionRepository;
  private final ClienteServicioRepository clienteServicioRepository;

  /**
   * Representa un factor cualitativo y cuantitativo que influye en la calificación del Health
   * Score.
   */
  @Getter
  @Builder
  public static class FactorSalud {
    /** Título descriptivo del factor */
    private String titulo;

    /** Concepto o explicación de la causa */
    private String concepto;

    /** Tipo de impacto en la salud: "NEGATIVO", "POSITIVO" o "NEUTRO" */
    private String impacto;

    /** Representación textual de la penalización o bonificación aplicada */
    private String penalizacionTexto;

    /** Recomendación o plan de acción sugerido para mitigar el riesgo */
    private String recomendacion;
  }

  /**
   * Recalcula el puntaje de salud del cliente en base a sus métricas operativas vigentes, registra
   * una entrada en el historial de Health Score y actualiza la entidad cliente.
   *
   * @param cliente cliente cuyo Health Score será recalculado
   * @return puntaje final normalizado entre 0 y 100
   */
  @Transactional
  public int recalcularYActualizarHealthScore(Cliente cliente) {
    int puntaje = 100;

    // 1. Descuento por tickets abiertos
    long ticketsAbiertos =
        ticketRepository.countByClienteIdAndEstadoNot(cliente.getId(), EstadoTicket.CERRADO);
    if (ticketsAbiertos > 0) {
      puntaje -= (int) (ticketsAbiertos * 5);
    }

    // 2. Descuento por tickets críticos
    long ticketsCriticos =
        ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(
            cliente.getId(), PrioridadTicket.CRITICA, EstadoTicket.CERRADO);
    if (ticketsCriticos > 0) {
      puntaje -= (int) (ticketsCriticos * 15);
    }

    // 3. Ajuste por CSAT: utilizar respuestas reales del Portal Cliente
    Double csatPromedio = encuestaCsatRepository.findAverageScoreByClienteId(cliente.getId());

    // Sin encuestas respondidas, no se aplica penalización ni bonificación.
    if (csatPromedio != null && csatPromedio > 0.0) {
      if (csatPromedio < 3.0) {
        puntaje -= 20;
      } else if (csatPromedio >= 4.5) {
        puntaje += 5;
      }
    }

    // 4. Penalización por renovaciones pendientes o vencidas.
    int penalizacionRenovacion = calcularPenalizacionRenovacion(cliente);

    puntaje -= penalizacionRenovacion;

    // Normalizar entre 0 y 100
    puntaje = Math.max(0, Math.min(100, puntaje));

    // Guardar historial
    EstadoSaludCliente estadoSalud =
        puntaje >= 80
            ? EstadoSaludCliente.SALUDABLE
            : (puntaje >= 50 ? EstadoSaludCliente.EN_OBSERVACION : EstadoSaludCliente.EN_RIESGO);

    HealthScoreHistorial historial =
        HealthScoreHistorial.builder()
            .cliente(cliente)
            .puntaje(new BigDecimal(puntaje))
            .estado(estadoSalud)
            .fechaCalculo(LocalDateTime.now())
            .build();

    final int puntajeFinal = puntaje;

    boolean puntajeCambio =
        healthScoreHistorialRepository
            .findFirstByClienteIdOrderByFechaCalculoDesc(cliente.getId())
            .map(anterior -> anterior.getPuntaje().intValue() != puntajeFinal)
            .orElse(true);

    if (puntajeCambio) {
      healthScoreHistorialRepository.save(historial);
    }

    cliente.setHealthScore(puntaje);
    clienteRepository.save(cliente);

    // Sincronizar las alertas de riesgo con el puntaje real.
    notificacionService.sincronizarRiesgoCliente(cliente, puntaje);

    return puntaje;
  }

  /**
   * Obtiene la lista de factores explicativos de riesgo o bonificación que justifican el puntaje
   * actual del cliente.
   *
   * @param cliente cliente a evaluar
   * @return lista de factores de salud identificados
   */
  @Override
  @Transactional(readOnly = true)
  public List<FactorSalud> obtenerFactoresSalud(Cliente cliente) {
    List<FactorSalud> factores = new ArrayList<>();

    long ticketsCriticos =
        ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(
            cliente.getId(), PrioridadTicket.CRITICA, EstadoTicket.CERRADO);

    if (ticketsCriticos > 0) {
      factores.add(
          FactorSalud.builder()
              .titulo("Tickets Críticos Abiertos")
              .concepto("Tickets Críticos Abiertos")
              .impacto("NEGATIVO")
              .penalizacionTexto("-" + (ticketsCriticos * 15) + " pts")
              .recomendacion(
                  "Asignar recursos de nivel 2 para resolver incidencias críticas de inmediato.")
              .build());
    }

    return factores;
  }

  private int calcularPenalizacionRenovacion(Cliente cliente) {

    LocalDate hoy = LocalDate.now();

    // Primera fuente: renovaciones formalmente registradas.
    List<Renovacion> renovaciones = renovacionRepository.findByClienteId(cliente.getId());

    if (renovaciones != null && !renovaciones.isEmpty()) {

      // Primero evaluar renovaciones pendientes o en negociación.
      boolean tieneRenovacionActiva =
          renovaciones.stream()
              .anyMatch(r ->
                  r.getEstado() == EstadoRenovacion.PENDIENTE
                      || r.getEstado() == EstadoRenovacion.EN_NEGOCIACION);

      if (tieneRenovacionActiva) {
        return renovaciones.stream()
            .filter(r ->
                r.getEstado() == EstadoRenovacion.PENDIENTE
                    || r.getEstado() == EstadoRenovacion.EN_NEGOCIACION)
            .map(Renovacion::getFechaVencimiento)
            .filter(Objects::nonNull)
            .mapToInt(fecha -> penalizacionPorFecha(fecha, hoy))
            .max()
            .orElse(0);
      }

      // Si el cliente decidió no renovar, mantenerlo como riesgo.
      boolean noRenovo =
          renovaciones.stream()
              .anyMatch(r ->
                  r.getEstado() == EstadoRenovacion.NO_RENOVADO
                      || r.getEstado() == EstadoRenovacion.NO_REALIZADO);

      return noRenovo ? 40 : 0;
    }

    // Segunda fuente: servicios que aún no tienen renovación.
    List<ClienteServicio> servicios = clienteServicioRepository.findByClienteId(cliente.getId());

    if (servicios == null) {
      return 0;
    }

    return servicios.stream()
        .filter(s -> s.getEstado() == EstadoServicioCliente.ACTIVO)
        .map(ClienteServicio::getFechaFin)
        .filter(Objects::nonNull)
        .mapToInt(fecha -> penalizacionPorFecha(fecha, hoy))
        .max()
        .orElse(0);
  }

  private int penalizacionPorFecha(LocalDate vencimiento, LocalDate hoy) {

    long dias = ChronoUnit.DAYS.between(hoy, vencimiento);

    if (dias < 0) {
      return 40;
    }

    if (dias <= 7) {
      return 15;
    }

    if (dias <= 30) {
      return 5;
    }

    return 0;
  }
}
