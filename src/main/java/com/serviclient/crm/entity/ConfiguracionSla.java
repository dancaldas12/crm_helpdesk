package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.PrioridadTicket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_sla", uniqueConstraints = {
    @UniqueConstraint(name = "uq_sla_empresa_prioridad", columnNames = {"empresa_id", "prioridad"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrioridadTicket prioridad;

    @Column(name = "tiempo_respuesta_minutos", nullable = false)
    private Integer tiempoRespuestaMinutos;

    @Column(name = "tiempo_resolucion_minutos", nullable = false)
    private Integer tiempoResolucionMinutos;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";

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
