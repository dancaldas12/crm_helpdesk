package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.TipoNotificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones", indexes = {
    @Index(name = "idx_notificacion_usuario_leida", columnList = "usuario_id, leida"),
    @Index(name = "idx_notificacion_empresa", columnList = "empresa_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacion tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(name = "entidad_tipo", length = 50)
    private String entidadTipo;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean leida = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
