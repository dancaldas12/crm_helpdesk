package com.serviclient.crm.service;

import com.serviclient.crm.dto.RegistroEmpresaDto;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import com.serviclient.crm.repository.EmpresaRepository;
import com.serviclient.crm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Empresa registrarEmpresaCompleta(RegistroEmpresaDto dto) {
        if (usuarioRepository.existsByEmail(dto.getAdminEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el correo: " + dto.getAdminEmail());
        }

        // 1. Crear Empresa con Configuración de SLA/CSAT/Health Score
        Empresa empresa = Empresa.builder()
                .nombreComercial(dto.getNombreComercial())
                .razonSocial(dto.getRazonSocial())
                .ruc(dto.getRuc())
                .industria(dto.getIndustria())
                .pais(dto.getPais())
                .ciudad(dto.getCiudad())
                .direccion(dto.getDireccion())
                .logoUrl(dto.getLogoUrl())
                .slaPrimerRespuestaHoras(dto.getSlaPrimerRespuestaHoras() != null ? dto.getSlaPrimerRespuestaHoras() : 2)
                .slaResolucionHoras(dto.getSlaResolucionHoras() != null ? dto.getSlaResolucionHoras() : 24)
                .activarCsat(dto.getActivarCsat() != null ? dto.getActivarCsat() : true)
                .escalaCsat(dto.getEscalaCsat() != null ? dto.getEscalaCsat() : "1 a 5")
                .factorTicketsCriticos(dto.getFactorTicketsCriticos() != null ? dto.getFactorTicketsCriticos() : true)
                .factorCsat(dto.getFactorCsat() != null ? dto.getFactorCsat() : true)
                .factorRenovacionProxima(dto.getFactorRenovacionProxima() != null ? dto.getFactorRenovacionProxima() : true)
                .build();

        Empresa empresaGuardada = empresaRepository.save(empresa);

        // 2. Crear Usuario Administrador
        Usuario admin = Usuario.builder()
                .empresa(empresaGuardada)
                .nombre(dto.getAdminNombre())
                .apellido(dto.getAdminApellido())
                .email(dto.getAdminEmail())
                .password(passwordEncoder.encode(dto.getAdminPassword()))
                .rol(Rol.ADMIN)
                .activo(true)
                .build();

        usuarioRepository.save(admin);

        return empresaGuardada;
    }

    @Transactional(readOnly = true)
    public Empresa obtenerPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con id: " + id));
    }
}
