package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.EstadoRenovacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "renovacion_seguimientos", indexes = {
    @Index(name = "idx_seguimiento_renovacion_fecha", columnList = "renovacion_id, fecha")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenovacionSeguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renovacion_id", nullable = false)
    private Renovacion renovacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoRenovacion estado;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Usuario getResponsable() {
        return usuario;
    }

    public void setResponsable(Usuario responsable) {
        this.usuario = responsable;
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
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
