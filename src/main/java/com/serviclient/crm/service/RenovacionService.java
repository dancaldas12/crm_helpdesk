package com.serviclient.crm.service;

import com.serviclient.crm.dto.TicketMensajeDto;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.RenovacionSeguimiento;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import com.serviclient.crm.repository.RenovacionRepository;
import com.serviclient.crm.repository.RenovacionSeguimientoRepository;
import com.serviclient.crm.repository.UsuarioRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RenovacionService {

    private final RenovacionRepository renovacionRepository;
    private final RenovacionSeguimientoRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HealthScoreService healthScoreService;

    @Getter
    @Builder
    public static class MetricasRenovaciones {
        private long menosDe30Dias;
        private long de31a90Dias;
        private long masDe90Dias;
        private long completadas;
        private long noRealizadas;
        private long clientesEnRiesgoProximos;
    }

    @Transactional(readOnly = true)
    public MetricasRenovaciones obtenerMetricas(Long empresaId) {
        LocalDate hoy = LocalDate.now();
        LocalDate en30Dias = hoy.plusDays(30);
        LocalDate en90Dias = hoy.plusDays(90);

        long menos30 = renovacionRepository.countRenovacionesEnRango(empresaId, hoy, en30Dias);
        long de31a90 = renovacionRepository.countRenovacionesEnRango(empresaId, en30Dias.plusDays(1), en90Dias);
        long mas90 = renovacionRepository.countRenovacionesMasDeDias(empresaId, en90Dias);
        long completadas = renovacionRepository.countByEmpresaIdAndEstado(empresaId, EstadoRenovacion.RENOVADO);
        long noRealizadas = renovacionRepository.countByEmpresaIdAndEstado(empresaId, EstadoRenovacion.NO_REALIZADO);
        long clientesRiesgo = renovacionRepository.countClientesEnRiesgoRenovacionProxima(empresaId, en90Dias);

        return MetricasRenovaciones.builder()
                .menosDe30Dias(menos30)
                .de31a90Dias(de31a90)
                .masDe90Dias(mas90)
                .completadas(completadas)
                .noRealizadas(noRealizadas)
                .clientesEnRiesgoProximos(clientesRiesgo)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Renovacion> listarProximasRenovaciones(Long empresaId) {
        return renovacionRepository.findByEmpresaIdOrderByFechaVencimientoAsc(empresaId);
    }

    @Transactional(readOnly = true)
    public Renovacion obtenerPorId(Long id) {
        return renovacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Renovación no encontrada con id: " + id));
    }

    @Transactional
    public RenovacionSeguimiento registrarSeguimiento(TicketMensajeDto.RegistrarSeguimientoRenovacion dto, Usuario autor) {
        Renovacion renovacion = obtenerPorId(dto.getRenovacionId());
        
        Usuario responsable = autor;
        if (dto.getResponsableId() != null) {
            responsable = usuarioRepository.findById(dto.getResponsableId()).orElse(autor);
        }

        // Actualizar estado de la renovación
        renovacion.setEstado(dto.getEstado());
        if (responsable != null) {
            renovacion.setResponsable(responsable);
        }
        renovacionRepository.save(renovacion);

        RenovacionSeguimiento seguimiento = RenovacionSeguimiento.builder()
                .renovacion(renovacion)
                .responsable(responsable)
                .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now())
                .estado(dto.getEstado())
                .comentario(dto.getComentario())
                .build();

        RenovacionSeguimiento guardado = seguimientoRepository.save(seguimiento);

        healthScoreService.recalcularYActualizarHealthScore(renovacion.getCliente());
        return guardado;
    }
}
