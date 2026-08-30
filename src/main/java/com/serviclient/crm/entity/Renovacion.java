package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.EstadoRenovacion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "renovaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renovacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(nullable = false, length = 120)
    private String servicio;

    private BigDecimal montoEstimado;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoRenovacion estado = EstadoRenovacion.PENDIENTE;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "renovacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha DESC")
    @Builder.Default
    private List<RenovacionSeguimiento> seguimientos = new ArrayList<>();

    public long getDiasRestantes() {
        if (fechaVencimiento == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
