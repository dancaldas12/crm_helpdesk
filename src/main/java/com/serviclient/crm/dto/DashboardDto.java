package com.serviclient.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object con todos los indicadores, métricas y datos
 * calculados para renderizar el Dashboard principal de ServiClient.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    private String periodoSeleccionado;

    // --- 1. Top 4 KPIs ---
    private long totalClientes;
    private long clientesNuevosEsteMes;

    private long totalTickets;
    private long ticketsCriticos;
    private long ticketsFueraDeSla;

    private double satisfaccionPromedio;
    private int satisfaccionPorcentaje;

    private long renovacionesProximas30;

    // --- 2. Salud de Clientes ---
    private long clientesSaludablesCount;
    private double clientesSaludablesPorcentaje;

    private long clientesObservacionCount;
    private double clientesObservacionPorcentaje;

    private long clientesRiesgoCount;
    private double clientesRiesgoPorcentaje;

    private long totalClientesActivos;

    // --- 3. Estado de Tickets ---
    private long ticketsAbiertos;
    private long ticketsEnProceso;
    private long ticketsPendientes;
    private long ticketsCriticosCount;
    private long ticketsFueraSlaCount;
    private long ticketsResueltos;
    private String ultimaActualizacionTickets;

    // --- 4. Renovaciones ---
    private long countMenor30Dias;
    private long countEntre31Y90Dias;
    private long countMayor90Dias;
    private long countCompletadas;
    private long countNoRealizadas;
    private String tabRenovacionActiva;

    @Builder.Default
    private List<RenovacionItemDto> renovacionesTabla = new ArrayList<>();

    // --- 5. Satisfacción CSAT ---
    private double csatPuntuacion;
    private int estrellasEnteras;
    private boolean tieneMediaEstrella;
    private String calificacionNivelTexto;
    private long encuestasRespondidas;
    private int csatSatisfaccionGeneralPorcentaje;
    private long clientesBajaSatisfaccion;
    private long encuestasPendientes;
    private double csatObjetivo;
    private boolean cumpleMetaCsat;

    // --- 6. Alertas y Pendientes ---
    private int totalAlertasActivas;
    @Builder.Default
    private List<AlertaItemDto> alertas = new ArrayList<>();

    // --- 7. Actividad Reciente ---
    @Builder.Default
    private List<ActividadItemDto> actividadReciente = new ArrayList<>();

    /**
     * Elemento para la tabla de renovaciones en el dashboard.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RenovacionItemDto {
        private Long id;
        private Long clienteId;
        private String clienteNombre;
        private String servicioNombre;
        private long diasRestantes;
        private String diasRestantesTexto;
        private String diasRestantesBadgeClass; // badge-danger, badge-warning, badge-neutral
        private String responsableNombre;
        private String estado;
        private String estadoBadgeClass;
        private int healthScore;
        private String healthScoreEstado;
        private String healthScoreBadgeClass; // hs-saludable, hs-observacion, hs-riesgo
    }

    /**
     * Elemento para la lista de alertas del dashboard.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertaItemDto {
        private String titulo;
        private String descripcion;
        private String tipoColor; // danger (rojo), warning (naranja/amarillo), neutral (gris)
        private String enlaceUrl;
    }

    /**
     * Elemento para la lista de actividad reciente del dashboard.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActividadItemDto {
        private String titulo;
        private String detalle;
        private String tipoIcono; // ticket-critico, csat-recibido, cliente-nuevo, renovacion-cambio, ticket-resuelto
        private String badgeTexto;
        private String badgeColorClass; // badge-danger, badge-success, badge-info, badge-purple
        private String tiempoFormateado;
    }
}
