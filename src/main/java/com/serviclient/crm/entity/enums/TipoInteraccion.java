package com.serviclient.crm.entity.enums;

public enum TipoInteraccion {
    LLAMADA("Llamada Telefónica"),
    REUNION("Reunión / Videollamada"),
    CORREO("Correo Electrónico"),
    SEGUIMIENTO("Seguimiento"),
    NOTA("Nota Interna"),
    OTRO("Otro");

    private final String descripcion;

    TipoInteraccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
