package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.EstadoRenovacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "renovacion_seguimientos")
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
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoRenovacion estado;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.fecha == null) {
            this.fecha = LocalDate.now();
        }
    }
}
