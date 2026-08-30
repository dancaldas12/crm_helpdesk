package com.serviclient.crm.entity.enums;

public enum CanalOrigen {
    PORTAL_WEB("Portal Web"),
    EMAIL("Correo Electrónico"),
    TELEFONO("Teléfono"),
    CHAT("Chat en Vivo");

    private final String etiqueta;

    CanalOrigen(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
