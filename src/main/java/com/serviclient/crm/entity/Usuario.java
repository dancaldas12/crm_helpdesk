package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getIniciales() {
        String n = (nombre != null && !nombre.isBlank()) ? String.valueOf(nombre.charAt(0)).toUpperCase() : "";
        String a = (apellido != null && !apellido.isBlank()) ? String.valueOf(apellido.charAt(0)).toUpperCase() : "";
        return (n + a).isBlank() ? "U" : n + a;
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
