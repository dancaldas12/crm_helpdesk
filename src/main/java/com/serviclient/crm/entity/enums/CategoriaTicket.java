package com.serviclient.crm.entity.enums;

public enum CategoriaTicket {
    INFRAESTRUCTURA("Infraestructura"),
    SOFTWARE("Software"),
    FACTURACION("Facturación"),
    ADMINISTRACION("Administración"),
    SOPORTE_TECNICO("Soporte Técnico");

    private final String etiqueta;

    CategoriaTicket(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
