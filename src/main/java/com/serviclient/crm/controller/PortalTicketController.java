package com.serviclient.crm.controller;

import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.CuentaPortalRepository;
import com.serviclient.crm.service.ITicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC del Portal de Clientes para la gestión de tickets.
 *
 * <p>Permite a los clientes autenticados mediante {@link CuentaPortal} crear
 * nuevas incidencias y consultar el detalle de sus tickets existentes.
 *
 * <p><b>Rutas expuestas:</b>
 * <pre>
 * GET  /portal/tickets/nuevo  → Formulario de nueva incidencia
 * POST /portal/tickets        → Registrar nueva incidencia
 * GET  /portal/tickets/{id}   → Detalle del ticket del cliente
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.ITicketService
 */
@Controller
@RequestMapping("/portal/tickets")
@RequiredArgsConstructor
@Tag(name = "Portal de Clientes", description = "Gestión de incidencias del portal")
public class PortalTicketController {

    private final ITicketService ticketService;
    private final CuentaPortalRepository cuentaPortalRepository;

    /**
     * Muestra el formulario para que el cliente registre una nueva incidencia.
     */
    @Operation(summary = "Formulario de nueva incidencia")
    @ApiResponse(responseCode = "200", description = "Formulario renderizado")
    @GetMapping("/nuevo")
    public String nuevaIncidencia(Model model, Principal principal) {
        CuentaPortal cuenta = obtenerCuenta(principal);
        cargarDatosUsuarioAutenticado(model, cuenta);

        if (!model.containsAttribute("ticketForm")) {
            model.addAttribute("ticketForm", new PortalTicketDto());
        }
        model.addAttribute("categorias", CategoriaTicket.values());
        model.addAttribute("prioridades", PrioridadTicket.values());

        return "portal/nueva-incidencia";
    }

    /**
     * Procesa el formulario, crea el ticket y prepara los datos para el modal de confirmación.
     */
    @Operation(summary = "Registrar nueva incidencia")
    @ApiResponse(responseCode = "302", description = "Redirección con modal de éxito")
    @PostMapping
    public String registrarIncidencia(
            @Valid @ModelAttribute("ticketForm") PortalTicketDto ticketForm,
            BindingResult bindingResult,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        CuentaPortal cuenta = obtenerCuenta(principal);

        if (bindingResult.hasErrors()) {
            cargarDatosUsuarioAutenticado(model, cuenta);
            model.addAttribute("categorias", CategoriaTicket.values());
            model.addAttribute("prioridades", PrioridadTicket.values());
            return "portal/nueva-incidencia";
        }

        // Crear ticket a través de tu servicio de negocio
        Ticket ticketCreado = ticketService.crearTicketDesdePortal(ticketForm, cuenta);

        // Atributos flash para activar la ventana emergente en 'nueva-incidencia.html'
        redirectAttributes.addFlashAttribute("ticketCreadoExito", true);
        redirectAttributes.addFlashAttribute("ticketId", ticketCreado.getId());
        redirectAttributes.addFlashAttribute("ticketNumero", ticketCreado.getNumeroTicket());
        redirectAttributes.addFlashAttribute("ticketAsunto", ticketCreado.getAsunto());
        redirectAttributes.addFlashAttribute("ticketPrioridad", 
                ticketCreado.getPrioridad() != null ? ticketCreado.getPrioridad().getEtiqueta() : "Media");
        redirectAttributes.addFlashAttribute("ticketEstado", 
                ticketCreado.getEstado() != null ? ticketCreado.getEstado().getEtiqueta() : "Abierto (En espera de asignación)");

        return "redirect:/portal/tickets/nuevo";
    }

    /**
     * Muestra el detalle de un ticket específico del cliente autenticado.
     */
    @Operation(summary = "Detalle del ticket del cliente")
    @ApiResponse(responseCode = "200", description = "Ticket encontrado")
    @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    @GetMapping("/{id}")
    public String detalleTicket(
            @Parameter(description = "ID del ticket")
            @PathVariable Long id,
            Model model,
            Principal principal) {

        CuentaPortal cuenta = obtenerCuenta(principal);

        Long clienteId = cuenta.getContacto().getCliente().getId();
        Long empresaId = cuenta.getContacto().getCliente().getEmpresa().getId();

        Ticket ticket = ticketService.obtenerPorIdYEmpresa(id, empresaId);

        if (!clienteId.equals(ticket.getCliente().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ticket no encontrado.");
        }

        cargarDatosUsuarioAutenticado(model, cuenta);
        model.addAttribute("ticket", ticket);

        return "portal/detalle-ticket";
    }

    /**
     * Obtiene la cuenta de portal activa a partir del principal autenticado.
     */
    private CuentaPortal obtenerCuenta(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        CuentaPortal cuenta = cuentaPortalRepository
                .findByEmail(principal.getName())
                .filter(CuentaPortal::estaActiva)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Cuenta no disponible."));

        if (cuenta.getContacto() == null || cuenta.getContacto().getCliente() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Cliente no identificado.");
        }

        return cuenta;
    }

    /**
     * Carga en el modelo los datos del usuario autenticado en el portal.
     */
    private void cargarDatosUsuarioAutenticado(Model model, CuentaPortal cuenta) {
        Contacto contacto = cuenta.getContacto();

        String nombreCompleto = StringUtils.trimToEmpty(
                StringUtils.joinWith(" ", contacto.getNombre(), contacto.getApellido())
        );

        String inicialNombre = StringUtils.left(contacto.getNombre(), 1);
        String inicialApellido = StringUtils.left(contacto.getApellido(), 1);
        String iniciales = StringUtils.upperCase(inicialNombre + inicialApellido);

        model.addAttribute("usuarioNombre",
                StringUtils.defaultIfBlank(nombreCompleto, cuenta.getEmail()));

        model.addAttribute("usuarioIniciales",
                StringUtils.defaultIfBlank(iniciales, "U"));

        model.addAttribute("usuarioEmail", cuenta.getEmail());

        model.addAttribute("empresaNombre",
                StringUtils.defaultIfBlank(contacto.getCliente().getRazonSocial(), "Mi Empresa"));

        boolean debeCambiar = Boolean.TRUE.equals(cuenta.getRequiereCambioPassword());
        model.addAttribute("debeCambiarPassword", debeCambiar);
    }
}