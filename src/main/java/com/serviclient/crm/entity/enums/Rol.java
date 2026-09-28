package com.serviclient.crm.entity.enums;

public enum Rol {
    ADMIN("Administrador"),
    AGENTE("Agente de Soporte"),
    SUPERVISOR("Supervisor"),
    CLIENTE("Cliente");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
