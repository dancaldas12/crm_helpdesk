package com.serviclient.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_mensajes")
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
    @JoinColumn(name = "remitente_id")
    private Usuario remitente;

    @Column(nullable = false, length = 120)
    private String remitenteNombre;

    @Column(length = 500)
    private String remitenteAvatar;

    @Builder.Default
    @Column(nullable = false)
    private Boolean esAgente = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean esNotaInterna = false; // Comentario Interno en color amarillo según prototipo

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public String getIniciales() {
        if (remitenteNombre == null || remitenteNombre.isBlank()) return "U";
        String[] parts = remitenteNombre.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
