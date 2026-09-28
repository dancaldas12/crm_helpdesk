package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.CanalOrigen;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets", uniqueConstraints = {
    @UniqueConstraint(name = "uq_ticket_numero_empresa", columnNames = {"empresa_id", "numero_ticket"})
}, indexes = {
    @Index(name = "idx_ticket_empresa_estado", columnList = "empresa_id, estado"),
    @Index(name = "idx_ticket_cliente", columnList = "cliente_id"),
    @Index(name = "idx_ticket_agente", columnList = "agente_id"),
    @Index(name = "idx_ticket_prioridad", columnList = "prioridad"),
    @Index(name = "idx_ticket_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

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
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaTicketEntity categoriaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id")
    private Usuario agente;

    @Column(name = "numero_ticket", nullable = false, length = 30)
    private String numeroTicket;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PrioridadTicket prioridad = PrioridadTicket.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoTicket estado = EstadoTicket.ABIERTO;

    @Column(name = "sla_respuesta_limite")
    private LocalDateTime slaRespuestaLimite;

    @Column(name = "sla_resolucion_limite")
    private LocalDateTime slaResolucionLimite;

    @Column(name = "fecha_primera_respuesta")
    private LocalDateTime fechaPrimeraRespuesta;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<TicketMensaje> mensajes = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<TicketHistorial> historial = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TicketAdjunto> adjuntos = new ArrayList<>();

    // Backward compatibility helpers
    public String getCodigo() {
        return numeroTicket;
    }

    public void setCodigo(String codigo) {
        this.numeroTicket = codigo;
    }

    public CategoriaTicket getCategoria() {
        if (categoriaEntity != null && categoriaEntity.getNombre() != null) {
            String name = categoriaEntity.getNombre().toUpperCase().replace(" ", "_");
            try {
                return CategoriaTicket.valueOf(name);
            } catch (Exception ignored) {}
        }
        return CategoriaTicket.SOPORTE_TECNICO;
    }

    public CanalOrigen getCanalOrigen() {
        return CanalOrigen.PORTAL_WEB;
    }

    public LocalDateTime getFechaCreacion() {
        return createdAt;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.createdAt = fechaCreacion;
    }

    public LocalDateTime getFechaLimitePrimeraRespuesta() {
        return slaRespuestaLimite;
    }

    public void setFechaLimitePrimeraRespuesta(LocalDateTime f) {
        this.slaRespuestaLimite = f;
    }

    public LocalDateTime getFechaLimiteResolucion() {
        return slaResolucionLimite;
    }

    public void setFechaLimiteResolucion(LocalDateTime f) {
        this.slaResolucionLimite = f;
    }

    public boolean isPrimeraRespuestaCumplida() {
        return fechaPrimeraRespuesta != null;
    }

    public boolean isFueraDeSla() {
        if (estado == EstadoTicket.RESUELTO || estado == EstadoTicket.CERRADO) {
            if (fechaResolucion != null && slaResolucionLimite != null) {
                return fechaResolucion.isAfter(slaResolucionLimite);
            }
            return false;
        }
        if (slaResolucionLimite != null && LocalDateTime.now().isAfter(slaResolucionLimite)) {
            return true;
        }
        return false;
    }

    public String getEstadoSlaTexto() {
        if (isFueraDeSla()) return "Vencido";
        if (estado == EstadoTicket.RESUELTO || estado == EstadoTicket.CERRADO) return "Cumplido";
        if (slaResolucionLimite != null) {
            long hoursLeft = Duration.between(LocalDateTime.now(), slaResolucionLimite).toHours();
            if (hoursLeft <= 4) return "Próximo a vencer";
        }
        return "Saludable";
    }

    public String getTiempoTranscurridoFormateado() {
        LocalDateTime fin = (fechaResolucion != null) ? fechaResolucion : LocalDateTime.now();
        Duration duration = Duration.between(createdAt != null ? createdAt : LocalDateTime.now(), fin);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return hours + "h " + minutes + "m";
    }

    public String getTiempoRestanteFormateado() {
        if (slaResolucionLimite == null || estado == EstadoTicket.RESUELTO || estado == EstadoTicket.CERRADO) {
            return "0h 0m";
        }
        if (LocalDateTime.now().isAfter(slaResolucionLimite)) {
            Duration over = Duration.between(slaResolucionLimite, LocalDateTime.now());
            return "-" + over.toHours() + "h " + over.toMinutesPart() + "m";
        }
        Duration remaining = Duration.between(LocalDateTime.now(), slaResolucionLimite);
        return remaining.toHours() + "h " + remaining.toMinutesPart() + "m";
    }

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
