package com.serviclient.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_health_score", uniqueConstraints = {
    @UniqueConstraint(name = "uq_health_empresa", columnNames = {"empresa_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionHealthScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Builder.Default
    @Column(name = "peso_tickets", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoTickets = new BigDecimal("30.00");

    @Builder.Default
    @Column(name = "peso_csat", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoCsat = new BigDecimal("30.00");

    @Builder.Default
    @Column(name = "peso_actividad", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoActividad = new BigDecimal("15.00");

    @Builder.Default
    @Column(name = "peso_renovacion", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoRenovacion = new BigDecimal("25.00");

    @Builder.Default
    @Column(name = "rango_saludable_min", nullable = false)
    private Integer rangoSaludableMin = 80;

    @Builder.Default
    @Column(name = "rango_observacion_min", nullable = false)
    private Integer rangoObservacionMin = 50;

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
