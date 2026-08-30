package com.serviclient.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombreComercial;

    @Column(nullable = false, length = 200)
    private String razonSocial;

    @Column(length = 20)
    private String ruc;

    @Column(length = 100)
    private String industria;

    @Column(length = 100)
    private String pais;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 255)
    private String direccion;

    @Column(length = 500)
    private String logoUrl;

    // Configuración Inicial SLA
    @Builder.Default
    @Column(nullable = false)
    private Integer slaPrimerRespuestaHoras = 2;

    @Builder.Default
    @Column(nullable = false)
    private Integer slaResolucionHoras = 24;

    // Configuración Inicial CSAT
    @Builder.Default
    @Column(nullable = false)
    private Boolean activarCsat = true;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String escalaCsat = "1 a 5";

    // Configuración Factores Health Score
    @Builder.Default
    @Column(nullable = false)
    private Boolean factorTicketsCriticos = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean factorCsat = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean factorRenovacionProxima = true;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Usuario> usuarios = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Cliente> clientes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
