package com.serviclient.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_csat", uniqueConstraints = {
    @UniqueConstraint(name = "uq_csat_empresa", columnNames = {"empresa_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionCsat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @Column(name = "escala_min", nullable = false)
    private Integer escalaMin = 1;

    @Builder.Default
    @Column(name = "escala_max", nullable = false)
    private Integer escalaMax = 5;

    @Builder.Default
    @Column(nullable = false, length = 255)
    private String pregunta = "¿Qué tan satisfecho estás con la atención recibida?";

    @Builder.Default
    @Column(name = "mensaje_agradecimiento", length = 255)
    private String mensajeAgradecimiento = "Gracias por compartir tu opinión.";

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
