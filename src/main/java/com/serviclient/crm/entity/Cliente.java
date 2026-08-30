package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.EstadoCliente;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 150)
    private String nombreComercial;

    @Column(length = 200)
    private String razonSocial;

    @Column(length = 20)
    private String ruc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(nullable = false, length = 100)
    private String servicioContratado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoCliente estado = EstadoCliente.SALUDABLE;

    @Builder.Default
    @Column(nullable = false)
    private Integer healthScore = 80;

    @Builder.Default
    @Column(nullable = false)
    private Double csatPromedio = 4.5;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaRenovacion;

    @Column(length = 500)
    private String logoUrl;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Contacto> contactos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Renovacion> renovaciones = new ArrayList<>();

    public long getDiasParaRenovacion() {
        if (fechaRenovacion == null) return 0;
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaRenovacion);
        return dias;
    }

    public int getCsatPorcentaje() {
        if (csatPromedio == null || csatPromedio <= 0) return 0;
        return (int) Math.round((csatPromedio / 5.0) * 100);
    }

    public String getHealthScoreColorClass() {
        if (healthScore == null) return "bg-slate-400";
        if (healthScore >= 70) return "bg-emerald-500";
        if (healthScore >= 50) return "bg-amber-500";
        return "bg-rose-500";
    }

    public String getHealthScoreTextColorClass() {
        if (healthScore == null) return "text-slate-600";
        if (healthScore >= 70) return "text-emerald-600";
        if (healthScore >= 50) return "text-amber-600";
        return "text-rose-600";
    }

    public Contacto getContactoPrincipal() {
        if (contactos == null || contactos.isEmpty()) return null;
        return contactos.stream()
                .filter(c -> Boolean.TRUE.equals(c.getEsPrincipal()))
                .findFirst()
                .orElse(contactos.get(0));
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
