package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.EstadoSaludCliente;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_score_historial", indexes = {
    @Index(name = "idx_health_cliente_fecha", columnList = "cliente_id, fecha_calculo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthScoreHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal puntaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSaludCliente estado;

    @Column(name = "puntaje_tickets", precision = 5, scale = 2)
    private BigDecimal puntajeTickets;

    @Column(name = "puntaje_csat", precision = 5, scale = 2)
    private BigDecimal puntajeCsat;

    @Column(name = "puntaje_actividad", precision = 5, scale = 2)
    private BigDecimal puntajeActividad;

    @Column(name = "puntaje_renovacion", precision = 5, scale = 2)
    private BigDecimal puntajeRenovacion;

    @Builder.Default
    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo = LocalDateTime.now();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.fechaCalculo == null) {
            this.fechaCalculo = LocalDateTime.now();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
