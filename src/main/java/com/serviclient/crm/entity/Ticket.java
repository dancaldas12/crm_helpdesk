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
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo; // ej: TK-4029

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
    @JoinColumn(name = "agente_id")
    private Usuario agente;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CategoriaTicket categoria = CategoriaTicket.SOPORTE_TECNICO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PrioridadTicket prioridad = PrioridadTicket.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoTicket estado = EstadoTicket.ABIERTO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CanalOrigen canalOrigen = CanalOrigen.PORTAL_WEB;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaLimitePrimeraRespuesta;
    private LocalDateTime fechaPrimeraRespuesta;
    private LocalDateTime fechaLimiteResolucion;
    private LocalDateTime fechaResolucion;
    private LocalDateTime fechaCierre;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaCreacion ASC")
    @Builder.Default
    private List<TicketMensaje> mensajes = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaCreacion DESC")
    @Builder.Default
    private List<TicketHistorial> historial = new ArrayList<>();

    public boolean isPrimeraRespuestaCumplida() {
        return fechaPrimeraRespuesta != null;
    }

    public boolean isFueraDeSla() {
        if (estado == EstadoTicket.RESUELTO || estado == EstadoTicket.CERRADO) {
            if (fechaResolucion != null && fechaLimiteResolucion != null) {
                return fechaResolucion.isAfter(fechaLimiteResolucion);
            }
            return false;
        }
        if (fechaLimiteResolucion != null && LocalDateTime.now().isAfter(fechaLimiteResolucion)) {
            return true;
        }
        return false;
    }

    public String getEstadoSlaTexto() {
        if (isFueraDeSla()) return "Vencido";
        if (estado == EstadoTicket.RESUELTO || estado == EstadoTicket.CERRADO) return "Cumplido";
        if (fechaLimiteResolucion != null) {
            long hoursLeft = Duration.between(LocalDateTime.now(), fechaLimiteResolucion).toHours();
            if (hoursLeft <= 4) return "Próximo a vencer";
        }
        return "Saludable";
    }

    public String getTiempoTranscurridoFormateado() {
        LocalDateTime fin = (fechaResolucion != null) ? fechaResolucion : LocalDateTime.now();
        Duration duration = Duration.between(fechaCreacion, fin);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return hours + "h " + minutes + "m";
    }

    public String getTiempoRestanteFormateado() {
        if (fechaLimiteResolucion == null || estado == EstadoTicket.RESUELTO || estado == EstadoTicket.CERRADO) {
            return "0h 0m";
        }
        if (LocalDateTime.now().isAfter(fechaLimiteResolucion)) {
            Duration over = Duration.between(fechaLimiteResolucion, LocalDateTime.now());
            return "-" + over.toHours() + "h " + over.toMinutesPart() + "m";
        }
        Duration remaining = Duration.between(LocalDateTime.now(), fechaLimiteResolucion);
        return remaining.toHours() + "h " + remaining.toMinutesPart() + "m";
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
