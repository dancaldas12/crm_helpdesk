package com.serviclient.crm.entity.enums;

public enum EstadoCliente {
    SALUDABLE("Saludable", "bg-emerald-50 text-emerald-700 border-emerald-200"),
    OBSERVACION("Observación", "bg-amber-50 text-amber-700 border-amber-200"),
    EN_RIESGO("En riesgo", "bg-rose-50 text-rose-700 border-rose-200");

    private final String etiqueta;
    private final String estiloBadge;

    EstadoCliente(String etiqueta, String estiloBadge) {
        this.etiqueta = etiqueta;
        this.estiloBadge = estiloBadge;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getEstiloBadge() {
        return estiloBadge;
    }
}
