package com.serviclient.crm.entity.enums;

public enum EstadoServicioCliente {
    ACTIVO("Activo"),
    SUSPENDIDO("Suspendido"),
    FINALIZADO("Finalizado");

    private final String descripcion;

    EstadoServicioCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
