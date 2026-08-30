package com.serviclient.crm.service;

import com.serviclient.crm.dto.ClienteDto;
import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.ClienteRepository;
import com.serviclient.crm.repository.ContactoRepository;
import com.serviclient.crm.repository.TicketRepository;
import com.serviclient.crm.repository.UsuarioRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContactoRepository contactoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final EmpresaService empresaService;
    private final HealthScoreService healthScoreService;

    @Getter
    @Builder
    public static class MetricasClientes {
        private long totalClientes;
        private long saludables;
        private long enObservacion;
        private long enRiesgo;
    }

    @Getter
    @Builder
    public static class KpisCliente360 {
        private double csat;
        private long ticketsAbiertos;
        private long ticketsCriticos;
        private long diasParaRenovacion;
    }

    @Transactional(readOnly = true)
    public MetricasClientes obtenerMetricas(Long empresaId) {
        long total = clienteRepository.countByEmpresaId(empresaId);
        long saludables = clienteRepository.countByEmpresaIdAndEstado(empresaId, EstadoCliente.SALUDABLE);
        long observacion = clienteRepository.countByEmpresaIdAndEstado(empresaId, EstadoCliente.OBSERVACION);
        long riesgo = clienteRepository.countByEmpresaIdAndEstado(empresaId, EstadoCliente.EN_RIESGO);

        return MetricasClientes.builder()
                .totalClientes(total)
                .saludables(saludables)
                .enObservacion(observacion)
                .enRiesgo(riesgo)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<Cliente> listarClientes(Long empresaId, String busqueda, EstadoCliente estado, Pageable pageable) {
        return clienteRepository.buscarConFiltros(empresaId, busqueda, estado, pageable);
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
                .servicioContratado(dto.getServicioContratado())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoCliente.SALUDABLE)
                .healthScore(dto.getHealthScore() != null ? dto.getHealthScore() : 80)
                .fechaInicio(dto.getFechaInicio())
                .fechaRenovacion(dto.getFechaRenovacion())
                .logoUrl(dto.getLogoUrl())
                .build();

        Cliente guardado = clienteRepository.save(cliente);

        if (dto.getContactoNombre() != null && !dto.getContactoNombre().isBlank() &&
            dto.getContactoEmail() != null && !dto.getContactoEmail().isBlank()) {
            Contacto contacto = Contacto.builder()
                    .cliente(guardado)
                    .nombre(dto.getContactoNombre())
                    .cargo(dto.getContactoCargo())
                    .email(dto.getContactoEmail())
                    .telefono(dto.getContactoTelefono())
                    .esPrincipal(true)
                    .build();
            contactoRepository.save(contacto);
        }

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
