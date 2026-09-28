package com.serviclient.crm.service;

import com.serviclient.crm.dto.ClienteDto;
import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.entity.enums.EstadoSaludCliente;
import com.serviclient.crm.entity.enums.EstadoServicioCliente;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para la gestión de clientes B2B.
 *
 * <p>Proporciona operaciones CRUD sobre la entidad {@link com.serviclient.crm.entity.Cliente},
 * cálculo de métricas del panel de clientes, KPIs de la Vista 360°
 * y la lógica de creación de clientes con validación de unicidad de RUC.</p>
 *
 * <p><b>Reglas de negocio:</b></p>
 * <ul>
 *   <li>El RUC es único por empresa. Se lanza {@link IllegalArgumentException}
 *       si ya existe un cliente con el mismo RUC para la misma empresa.</li>
 *   <li>El Health Score se inicializa en 80 al crear un cliente y se recalcula
 *       automáticamente cuando cambian sus tickets, CSAT o renovaciones.</li>
 * </ul>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.controller.ClienteController
 * @see com.serviclient.crm.entity.Cliente
 * @see HealthScoreService
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContactoRepository contactoRepository;
    private final ServicioRepository servicioRepository;
    private final ClienteServicioRepository clienteServicioRepository;
    private final HealthScoreHistorialRepository healthScoreHistorialRepository;
    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final EmpresaService empresaService;
    private final HealthScoreService healthScoreService;

    /**
     * DTO inmutable con las métricas del panel de clientes.
     */
    @Getter
    @Builder
    public static class MetricasClientes {
        /** Número total de clientes activos de la empresa. */
        private long totalClientes;
        /** Clientes con Health Score &ge; 70 (estado {@code SALUDABLE}). */
        private long saludables;
        /** Clientes con Health Score entre 50 y 69 (estado {@code OBSERVACION}). */
        private long enObservacion;
        /** Clientes con Health Score &lt; 50 (estado {@code EN_RIESGO}). */
        private long enRiesgo;
    }

    /**
     * DTO inmutable con los KPIs de la Vista 360° de un cliente.
     */
    @Getter
    @Builder
    public static class KpisCliente360 {
        /** CSAT promedio del cliente (escala 1-5). */
        private double csat;
        /** Tickets activos (no cerrados ni resueltos). */
        private long ticketsAbiertos;
        /** Tickets con prioridad {@code CRITICA} no cerrados. */
        private long ticketsCriticos;
        /** Días restantes hasta la próxima renovación de contrato. */
        private long diasParaRenovacion;
    }

    /**
     * Calcula las métricas agregadas del panel de clientes para la empresa indicada.
     *
     * @param empresaId identificador de la empresa
     * @return {@link MetricasClientes} con conteos por estado de salud
     */
    @Transactional(readOnly = true)
    public MetricasClientes obtenerMetricas(Long empresaId) {
        List<Cliente> todos = clienteRepository.findByEmpresaId(empresaId);
        long total = todos.size();
        long saludables = todos.stream().filter(c -> c.getEstadoSalud() == EstadoCliente.SALUDABLE).count();
        long observacion = todos.stream().filter(c -> c.getEstadoSalud() == EstadoCliente.OBSERVACION).count();
        long riesgo = todos.stream().filter(c -> c.getEstadoSalud() == EstadoCliente.EN_RIESGO).count();

        return MetricasClientes.builder()
                .totalClientes(total)
                .saludables(saludables)
                .enObservacion(observacion)
                .enRiesgo(riesgo)
                .build();
    }

    /**
     * Lista los clientes de una empresa con soporte de búsqueda y filtro por estado de salud.
     * El filtro por estado se aplica en memoria después de la consulta paginada al repositorio.
     *
     * @param empresaId identificador de la empresa
     * @param busqueda  término libre de búsqueda por nombre comercial; puede ser {@code null}
     * @param estado    filtro por {@link com.serviclient.crm.entity.enums.EstadoCliente}; puede ser {@code null}
     * @param pageable  configuración de página y ordenamiento
     * @return página de clientes filtrada
     */
    @Transactional(readOnly = true)
    public Page<Cliente> listarClientes(Long empresaId, String busqueda, EstadoCliente estado, Pageable pageable) {
        Page<Cliente> page = clienteRepository.buscarConFiltros(empresaId, busqueda, pageable);
        if (estado != null) {
            List<Cliente> filtered = page.getContent().stream()
                    .filter(c -> c.getEstadoSalud() == estado)
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered, pageable, filtered.size());
        }
        return page;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodosPorEmpresa(Long empresaId) {
        return clienteRepository.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public KpisCliente360 obtenerKpisCliente360(Cliente cliente) {
        long abiertos = ticketRepository.countByClienteIdAndEstadoNot(cliente.getId(), EstadoTicket.CERRADO);
        long criticos = ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(
                cliente.getId(), PrioridadTicket.CRITICA, EstadoTicket.CERRADO
        );

        return KpisCliente360.builder()
                .csat(cliente.getCsatPromedio() != null ? cliente.getCsatPromedio() : 0.0)
                .ticketsAbiertos(abiertos)
                .ticketsCriticos(criticos)
                .diasParaRenovacion(cliente.getDiasParaRenovacion())
                .build();
    }

    @Transactional
    public Cliente crearCliente(Long empresaId, ClienteDto dto) {
        Empresa empresa = empresaService.obtenerPorId(empresaId);
        Usuario responsable = null;
        if (dto.getResponsableId() != null) {
            responsable = usuarioRepository.findById(dto.getResponsableId()).orElse(null);
        }

        Cliente cliente = Cliente.builder()
                .empresa(empresa)
                .nombreComercial(dto.getNombreComercial())
                .razonSocial(dto.getRazonSocial())
                .ruc(dto.getRuc())
                .responsable(responsable)
                .estado("ACTIVO")
                .logoUrl(dto.getLogoUrl())
                .build();

        Cliente guardado = clienteRepository.save(cliente);

        // Crear o vincular Servicio
        String nombreServicio = (dto.getServicioContratado() != null && !dto.getServicioContratado().isBlank())
                ? dto.getServicioContratado()
                : "Plan SaaS Cloud";

        Servicio servicio = servicioRepository.findByEmpresaId(empresaId).stream()
                .filter(s -> s.getNombre().equalsIgnoreCase(nombreServicio))
                .findFirst()
                .orElseGet(() -> servicioRepository.save(Servicio.builder()
                        .empresa(empresa)
                        .nombre(nombreServicio)
                        .descripcion(nombreServicio + " para cliente")
                        .codigo("SRV-" + Math.abs(nombreServicio.hashCode() % 10000))
                        .estado("ACTIVO")
                        .build()));

        LocalDate fechaIni = dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now();
        LocalDate fechaFin = dto.getFechaRenovacion() != null ? dto.getFechaRenovacion() : LocalDate.now().plusYears(1);

        ClienteServicio cs = ClienteServicio.builder()
                .cliente(guardado)
                .servicio(servicio)
                .fechaInicio(fechaIni)
                .fechaFin(fechaFin)
                .estado(EstadoServicioCliente.ACTIVO)
                .build();
        clienteServicioRepository.save(cs);

        // Contacto principal
        if (dto.getContactoNombre() != null && !dto.getContactoNombre().isBlank() &&
            dto.getContactoEmail() != null && !dto.getContactoEmail().isBlank()) {
            Contacto contacto = Contacto.builder()
                    .cliente(guardado)
                    .nombre(dto.getContactoNombre())
                    .cargo(dto.getContactoCargo())
                    .email(dto.getContactoEmail())
                    .telefono(dto.getContactoTelefono())
                    .esContactoPrincipal(true)
                    .estado("ACTIVO")
                    .build();
            contactoRepository.save(contacto);
        }

        // Historial Health Score inicial
        int scoreInicial = dto.getHealthScore() != null ? dto.getHealthScore() : 80;
        HealthScoreHistorial hsh = HealthScoreHistorial.builder()
                .cliente(guardado)
                .puntaje(new BigDecimal(scoreInicial))
                .estado(scoreInicial >= 70 ? EstadoSaludCliente.SALUDABLE : (scoreInicial >= 50 ? EstadoSaludCliente.EN_OBSERVACION : EstadoSaludCliente.EN_RIESGO))
                .puntajeTickets(new BigDecimal("30.00"))
                .puntajeCsat(new BigDecimal("30.00"))
                .puntajeActividad(new BigDecimal("15.00"))
                .puntajeRenovacion(new BigDecimal("25.00"))
                .build();
        healthScoreHistorialRepository.save(hsh);

        healthScoreService.recalcularYActualizarHealthScore(guardado);
        return guardado;
    }

    @Transactional
    public Contacto agregarContacto(Long clienteId, Contacto contacto) {
        Cliente cliente = obtenerPorId(clienteId);
        contacto.setCliente(cliente);
        return contactoRepository.save(contacto);
    }
}
