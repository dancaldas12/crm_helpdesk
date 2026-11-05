package com.serviclient.crm.controller.portalusuario;

import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.CuentaPortalRepository;
import com.serviclient.crm.repository.TicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/portal/mis-tickets")
@RequiredArgsConstructor
@Tag(name = "Portal Cliente - Mis Tickets", description = "Listado y seguimiento de tickets del cliente")
public class MisTicketsController {

    private final CuentaPortalRepository cuentaPortalRepository;
    private final TicketRepository ticketRepository;

    @Operation(summary = "Bandeja Mis Tickets del Portal de Clientes")
    @GetMapping
    public String listarMisTickets(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) EstadoTicket estado,
            @RequestParam(value = "prioridad", required = false) PrioridadTicket prioridad,
            Model model,
            Principal principal) {

        CuentaPortal cuenta = obtenerCuentaAutenticada(principal);
        cargarDatosContacto(model, cuenta);

        Long clienteId = cuenta.getContacto().getCliente().getId();

        // Obtener la totalidad de tickets registrados para la empresa cliente
        List<Ticket> todosLosTickets = ticketRepository.findByClienteId(clienteId);

        // Cálculo de las 5 tarjetas KPI superiores
        long abiertos = todosLosTickets.stream().filter(t -> t.getEstado() == EstadoTicket.ABIERTO).count();
        long enProceso = todosLosTickets.stream().filter(t -> t.getEstado() == EstadoTicket.EN_PROCESO).count();
        long criticos = todosLosTickets.stream().filter(t -> t.getPrioridad() == PrioridadTicket.CRITICA).count();
        long pendientes = todosLosTickets.stream().filter(t -> t.getEstado() == EstadoTicket.PENDIENTE).count();
        long fueraDeSla = todosLosTickets.stream().filter(Ticket::isFueraDeSla).count();

        // Filtrado por buscador, estado y prioridad
        List<Ticket> ticketsFiltrados = todosLosTickets.stream()
                .filter(t -> busqueda == null || busqueda.isBlank() 
                        || (t.getNumeroTicket() != null && t.getNumeroTicket().toLowerCase().contains(busqueda.toLowerCase()))
                        || (t.getAsunto() != null && t.getAsunto().toLowerCase().contains(busqueda.toLowerCase())))
                .filter(t -> estado == null || t.getEstado() == estado)
                .filter(t -> prioridad == null || t.getPrioridad() == prioridad)
                .toList();

        model.addAttribute("tickets", ticketsFiltrados);
        model.addAttribute("kpiAbiertos", abiertos);
        model.addAttribute("kpiEnProceso", enProceso);
        model.addAttribute("kpiCriticos", criticos);
        model.addAttribute("kpiPendientes", pendientes);
        model.addAttribute("kpiFueraDeSla", fueraDeSla);

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("prioridadSeleccionada", prioridad);
        model.addAttribute("estados", EstadoTicket.values());
        model.addAttribute("prioridades", PrioridadTicket.values());

        return "portal/mis-tickets";
    }

    private CuentaPortal obtenerCuentaAutenticada(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sesión no válida.");
        }
        CuentaPortal cuenta = cuentaPortalRepository.findByEmail(principal.getName())
                .filter(CuentaPortal::estaActiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta de portal inactiva o no encontrada."));

        if (cuenta.getContacto() == null || cuenta.getContacto().getCliente() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no tiene una empresa cliente asignada.");
        }
        return cuenta;
    }

    private void cargarDatosContacto(Model model, CuentaPortal cuenta) {
        Contacto contacto = cuenta.getContacto();
        String nombreCompleto = StringUtils.trimToEmpty(
                StringUtils.joinWith(" ", contacto.getNombre(), contacto.getApellido()));
        String inicialNombre = StringUtils.left(contacto.getNombre(), 1);
        String inicialApellido = StringUtils.left(contacto.getApellido(), 1);
        String iniciales = StringUtils.upperCase(inicialNombre + inicialApellido);

        model.addAttribute("usuarioNombre", StringUtils.defaultIfBlank(nombreCompleto, cuenta.getEmail()));
        model.addAttribute("usuarioIniciales", StringUtils.defaultIfBlank(iniciales, "U"));
        model.addAttribute("usuarioEmail", cuenta.getEmail());
        model.addAttribute("empresaNombre", StringUtils.defaultIfBlank(contacto.getCliente().getRazonSocial(), "Mi Empresa"));
    }
}