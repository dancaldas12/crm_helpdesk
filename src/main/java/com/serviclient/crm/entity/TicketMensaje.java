package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.TipoMensajeTicket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_mensajes", indexes = {
    @Index(name = "idx_mensaje_ticket_fecha", columnList = "ticket_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_id")
    private Contacto contacto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMensajeTicket tipo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Builder.Default
    @Column(name = "es_interno", nullable = false)
    private Boolean esInterno = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Compatibility helpers
    public String getContenido() {
        return mensaje;
    }

    public void setContenido(String c) {
        this.mensaje = c;
    }

    public Boolean getEsNotaInterna() {
        return esInterno;
    }

    public void setEsNotaInterna(Boolean b) {
        this.esInterno = b;
    }

    public Boolean getEsAgente() {
        return tipo == TipoMensajeTicket.AGENTE;
    }

    public String getRemitenteNombre() {
        if (usuario != null) {
            return usuario.getNombreCompleto();
        }
        if (contacto != null) {
            return contacto.getNombreCompleto();
        }
        return "Sistema";
    }

    public String getRemitenteAvatar() {
        if (usuario != null) {
            return usuario.getAvatarUrl();
        }
        return null;
    }

    public String getIniciales() {
        String name = getRemitenteNombre();
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    public LocalDateTime getFechaCreacion() {
        return createdAt;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.createdAt = fechaCreacion;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.tipo == null) {
            this.tipo = (usuario != null) ? TipoMensajeTicket.AGENTE : TipoMensajeTicket.CLIENTE;
        }
    }
}
