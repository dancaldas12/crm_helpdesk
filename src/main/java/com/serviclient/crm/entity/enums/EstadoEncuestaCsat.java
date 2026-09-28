package com.serviclient.crm.entity.enums;

public enum EstadoEncuestaCsat {
    PENDIENTE("Pendiente"),
    RESPONDIDA("Respondida"),
    EXPIRADA("Expirada");

    private final String descripcion;

    EstadoEncuestaCsat(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
