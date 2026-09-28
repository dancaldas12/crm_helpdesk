package com.serviclient.crm.service;

import com.serviclient.crm.dto.TicketMensajeDto;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión de renovaciones de contratos.
 *
 * <p>Administra el ciclo de renovación: consulta de renovaciones próximas,
 * cálculo de métricas por rango de vencimiento y registro de seguimientos
 * con cambio de estado.</p>
 *
 * <p><b>Reglas de negocio:</b></p>
 * <ul>
 *   <li>Si el nuevo estado es {@code RENOVADO}, se establece automáticamente
 *       la {@code fechaRenovacion} con la fecha del día.</li>
 *   <li>Tras cada seguimiento se recalcula el Health Score del cliente
 *       asociado a la renovación.</li>
 * </ul>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.controller.RenovacionController
 * @see com.serviclient.crm.entity.Renovacion
 */
@Service
@RequiredArgsConstructor
public class RenovacionService {

    private final RenovacionRepository renovacionRepository;
    private final RenovacionSeguimientoRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HealthScoreService healthScoreService;

    /**
     * DTO inmutable con los KPIs del panel de renovaciones.
     * Agrupa los conteos por rango de vencimiento y estados especiales.
     */
    @Getter
    @Builder
    public static class MetricasRenovaciones {
        /** Renovaciones con vencimiento en los próximos 30 días. */
        private long menosDe30Dias;
        /** Renovaciones con vencimiento entre 31 y 90 días. */
        private long de31a90Dias;
        /** Renovaciones con vencimiento a más de 90 días. */
        private long masDe90Dias;
        /** Renovaciones ya completadas con estado {@code RENOVADO}. */
        private long completadas;
        /** Renovaciones que terminaron con estado {@code NO_RENOVADO}. */
        private long noRealizadas;
        /** Clientes en riesgo con renovación próxima (Health Score bajo). */
        private long clientesEnRiesgoProximos;
    }

    /**
     * Calcula y retorna las métricas de renovaciones agrupadas por rango de vencimiento.
     *
     * @param empresaId identificador de la empresa
     * @return {@link MetricasRenovaciones} con conteos actualizados
     */
    @Transactional(readOnly = true)
    public MetricasRenovaciones obtenerMetricas(Long empresaId) {
        LocalDate hoy = LocalDate.now();
        LocalDate en30Dias = hoy.plusDays(30);
        LocalDate en90Dias = hoy.plusDays(90);

        long menos30 = renovacionRepository.countRenovacionesEnRango(empresaId, hoy, en30Dias);
        long de31a90 = renovacionRepository.countRenovacionesEnRango(empresaId, en30Dias.plusDays(1), en90Dias);
        long mas90 = renovacionRepository.countRenovacionesMasDeDias(empresaId, en90Dias);
        long completadas = renovacionRepository.countByEmpresaIdAndEstado(empresaId, EstadoRenovacion.RENOVADO);
        long noRealizadas = renovacionRepository.countByEmpresaIdAndEstado(empresaId, EstadoRenovacion.NO_RENOVADO);
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

    /**
     * Retorna todas las renovaciones de la empresa ordenadas por fecha de vencimiento
     * de forma ascendente (más urgentes primero).
     *
     * @param empresaId identificador de la empresa
     * @return lista de {@link com.serviclient.crm.entity.Renovacion} ordenada por urgencia
     */
    @Transactional(readOnly = true)
    public List<Renovacion> listarProximasRenovaciones(Long empresaId) {
        return renovacionRepository.findByEmpresaIdOrderByFechaVencimientoAsc(empresaId);
    }

    /**
     * Obtiene una renovación por su identificador primario.
     *
     * @param id identificador de la renovación
     * @return entidad {@link com.serviclient.crm.entity.Renovacion}
     * @throws IllegalArgumentException si no existe la renovación
     */
    @Transactional(readOnly = true)
    public Renovacion obtenerPorId(Long id) {
        return renovacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Renovación no encontrada con id: " + id));
    }

    /**
     * Registra un seguimiento de renovación: actualiza el estado de la renovación,
     * persiste el comentario del responsable y recalcula el Health Score del cliente.
     *
     * @param dto    datos del seguimiento (renovacionId, estado, responsableId, comentario, fecha)
     * @param autor  usuario autenticado que registra el seguimiento (fallback si {@code dto.responsableId} es null)
     * @return entidad {@link com.serviclient.crm.entity.RenovacionSeguimiento} persistida
     * @throws IllegalArgumentException si la renovación no existe
     */
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
        if (dto.getEstado() == EstadoRenovacion.RENOVADO) {
            renovacion.setFechaRenovacion(LocalDate.now());
        }
        renovacionRepository.save(renovacion);

        LocalDateTime fechaSeg = dto.getFecha() != null ? dto.getFecha().atStartOfDay() : LocalDateTime.now();

        RenovacionSeguimiento seguimiento = RenovacionSeguimiento.builder()
                .renovacion(renovacion)
                .usuario(responsable)
                .fecha(fechaSeg)
                .estado(dto.getEstado())
                .comentario(dto.getComentario())
                .build();

        RenovacionSeguimiento guardado = seguimientoRepository.save(seguimiento);

        healthScoreService.recalcularYActualizarHealthScore(renovacion.getCliente());
        return guardado;
    }
}
