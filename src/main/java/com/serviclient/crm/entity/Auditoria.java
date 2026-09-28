package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.AccionAuditoria;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria", indexes = {
    @Index(name = "idx_auditoria_empresa_fecha", columnList = "empresa_id, created_at"),
    @Index(name = "idx_auditoria_usuario_fecha", columnList = "usuario_id, created_at"),
    @Index(name = "idx_auditoria_entidad_registro", columnList = "entidad, registro_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionAuditoria accion;

    @Column(nullable = false, length = 100)
    private String entidad;

    @Column(name = "registro_id")
    private Long registroId;

    @Column(name = "datos_anteriores", columnDefinition = "JSON")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "JSON")
    private String datosNuevos;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

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
