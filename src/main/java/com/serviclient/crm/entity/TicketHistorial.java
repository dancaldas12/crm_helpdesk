package com.serviclient.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_historial", indexes = {
    @Index(name = "idx_historial_ticket_fecha", columnList = "ticket_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "tipo_evento", nullable = false, length = 80)
    private String tipoEvento;

    @Column(name = "valor_anterior", length = 255)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 255)
    private String valorNuevo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Compatibility helpers
    public String getAutorNombre() {
        return (usuario != null) ? usuario.getNombreCompleto() : "Sistema";
    }

    public String getAccion() {
        return (tipoEvento != null) ? tipoEvento : "Actualización";
    }

    public String getDetalle() {
        return descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return createdAt;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.createdAt = fechaCreacion;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.tipoEvento == null) {
            this.tipoEvento = "CAMBIO";
        }
    }
}
