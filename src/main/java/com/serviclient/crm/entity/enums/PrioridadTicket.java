package com.serviclient.crm.entity.enums;

public enum PrioridadTicket {
    BAJA("Baja", "badge-priority-low"),
    MEDIA("Media", "badge-priority-medium"),
    ALTA("Alta", "badge-priority-high"),
    CRITICA("Crítica", "badge-priority-critical");

    private final String etiqueta;
    private final String cssClass;

    PrioridadTicket(String etiqueta, String cssClass) {
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
