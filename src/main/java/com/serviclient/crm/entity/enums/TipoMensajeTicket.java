package com.serviclient.crm.entity.enums;

public enum TipoMensajeTicket {
    AGENTE("Agente"),
    CLIENTE("Cliente"),
    SISTEMA("Sistema");

    private final String descripcion;

    TipoMensajeTicket(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
