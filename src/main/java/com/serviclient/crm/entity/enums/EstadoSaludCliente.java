package com.serviclient.crm.entity.enums;

public enum EstadoSaludCliente {
    SALUDABLE("Saludable"),
    EN_OBSERVACION("En Observación"),
    EN_RIESGO("En Riesgo");

    private final String descripcion;

    EstadoSaludCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
