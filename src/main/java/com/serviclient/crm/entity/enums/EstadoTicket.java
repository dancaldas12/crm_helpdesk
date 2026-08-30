package com.serviclient.crm.entity.enums;

public enum EstadoTicket {
    ABIERTO("Abierto", "badge-status-open"),
    EN_PROCESO("En proceso", "badge-status-in-progress"),
    PENDIENTE("Pendiente", "badge-status-pending"),
    RESUELTO("Resuelto", "badge-status-resolved"),
    CERRADO("Cerrado", "badge-status-closed");

    private final String etiqueta;
    private final String cssClass;

    EstadoTicket(String etiqueta, String cssClass) {
        this.etiqueta = etiqueta;
        this.cssClass = cssClass;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getCssClass() {
        return cssClass;
    }
}
