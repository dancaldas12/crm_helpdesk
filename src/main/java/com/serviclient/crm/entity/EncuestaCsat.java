package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.EstadoEncuestaCsat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "encuestas_csat", uniqueConstraints = {
    @UniqueConstraint(name = "uq_encuesta_ticket", columnNames = {"ticket_id"})
}, indexes = {
    @Index(name = "idx_encuesta_cliente", columnList = "cliente_id"),
    @Index(name = "idx_encuesta_estado", columnList = "estado"),
    @Index(name = "idx_encuesta_empresa_estado", columnList = "empresa_id, estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncuestaCsat {

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
    @JoinColumn(name = "contacto_id")
    private Contacto contacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private EstadoEncuestaCsat estado = EstadoEncuestaCsat.PENDIENTE;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToOne(mappedBy = "encuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private RespuestaCsat respuesta;

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
