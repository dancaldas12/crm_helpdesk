package com.serviclient.crm.entity.enums;

public enum TipoNotificacion {
    CLIENTE_EN_RIESGO("Cliente en Riesgo"),
    RENOVACION_PROXIMA("Renovación Próxima"),
    SLA_PROXIMO_VENCER("SLA Próximo a Vencer"),
    SLA_INCUMPLIDO("SLA Incumplido"),
    CSAT_BAJO("CSAT Bajo"),
    TICKET_ASIGNADO("Ticket Asignado"),
    TICKET_ACTUALIZADO("Ticket Actualizado");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
