package com.serviclient.crm.entity.enums;

public enum EstadoRenovacion {
    PENDIENTE("Pendiente", "badge-renovacion-pendiente"),
    EN_NEGOCIACION("En negociación", "badge-renovacion-negociacion"),
    RENOVADO("Renovado", "badge-renovacion-renovado"),
    NO_RENOVADO("No renovado", "badge-renovacion-norealizada"),
    NO_REALIZADO("No realizada", "badge-renovacion-norealizada");

    private final String etiqueta;
    private final String cssClass;

    EstadoRenovacion(String etiqueta, String cssClass) {
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
