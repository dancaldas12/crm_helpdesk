package com.serviclient.crm.controller;

import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.TicketAdjunto;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.CuentaPortalRepository;
import com.serviclient.crm.service.IPortalTicketService;
import com.serviclient.crm.service.ITicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC del Portal de Clientes para la gestión de tickets.
 *
 * <p>Permite a los clientes autenticados mediante {@link CuentaPortal} crear
 * nuevas incidencias adjuntando archivos, visualizar adjuntos y consultar el detalle de sus tickets.
 *
 * <p><b>Rutas expuestas:</b>
 * <pre>
 * GET  /portal/tickets/nuevo                           → Formulario de nueva incidencia
 * POST /portal/tickets                                 → Registrar nueva incidencia con adjuntos
 * GET  /portal/tickets/{id}                            → Detalle del ticket del cliente
 * GET  /portal/tickets/{ticketId}/adjuntos/{adjuntoId} → Descargar/visualizar archivo adjunto
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.IPortalTicketService
 * @see com.serviclient.crm.service.ITicketService
 */
@Controller
@RequestMapping("/portal/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Portal de Clientes", description = "Gestión de incidencias del portal")
public class PortalTicketController {

    private final IPortalTicketService portalTicketService;
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
     * Procesa el formulario, delega la persistencia del ticket y archivos a PortalTicketService,
     * y prepara los datos para el modal de confirmación.
     */
    @Operation(summary = "Registrar nueva incidencia")
    @ApiResponse(responseCode = "302", description = "Redirección con modal de éxito")
    @PostMapping
    public String registrarIncidencia(
            @Valid @ModelAttribute("ticketForm") PortalTicketDto ticketForm,
            BindingResult bindingResult,
            @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
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

        try {
            Ticket ticketCreado = portalTicketService.crearTicketDesdePortal(cuenta, ticketForm, archivos);

            redirectAttributes.addFlashAttribute("ticketCreadoExito", true);
            redirectAttributes.addFlashAttribute("ticketId", ticketCreado.getId());
            redirectAttributes.addFlashAttribute("ticketNumero", ticketCreado.getNumeroTicket());
            redirectAttributes.addFlashAttribute("ticketAsunto", ticketCreado.getAsunto());
            redirectAttributes.addFlashAttribute("ticketPrioridad", 
                    ticketCreado.getPrioridad() != null ? ticketCreado.getPrioridad().getEtiqueta() : "Media");
            redirectAttributes.addFlashAttribute("ticketEstado", 
                    ticketCreado.getEstado() != null ? ticketCreado.getEstado().getEtiqueta() : "Abierto (En espera de asignación)");

        } catch (IllegalArgumentException | IllegalStateException | IOException ex) {
            log.error("Error al procesar ticket o archivos adjuntos desde portal", ex);
            cargarDatosUsuarioAutenticado(model, cuenta);
            model.addAttribute("categorias", CategoriaTicket.values());
            model.addAttribute("prioridades", PrioridadTicket.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "portal/nueva-incidencia";
        }

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
     * Transmite el archivo adjunto almacenado en el disco para su descarga o previsualización en el navegador.
     */
    @Operation(summary = "Descargar o visualizar archivo adjunto del ticket")
    @ApiResponse(responseCode = "200", description = "Archivo binario transmitido con éxito")
    @ApiResponse(responseCode = "404", description = "Ticket o archivo adjunto no encontrado")
    @GetMapping("/{ticketId}/adjuntos/{adjuntoId}")
    public ResponseEntity<Resource> descargarAdjunto(
            @PathVariable Long ticketId,
            @PathVariable Long adjuntoId,
            Principal principal) {

        CuentaPortal cuenta = obtenerCuenta(principal);
        Long clienteId = cuenta.getContacto().getCliente().getId();
        Long empresaId = cuenta.getContacto().getCliente().getEmpresa().getId();

        Ticket ticket = ticketService.obtenerPorIdYEmpresa(ticketId, empresaId);

        if (!clienteId.equals(ticket.getCliente().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado.");
        }

        TicketAdjunto adjunto = ticket.getAdjuntos().stream()
                .filter(a -> a.getId().equals(adjuntoId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Adjunto no encontrado."));

        try {
            Path rutaArchivo = Paths.get(adjunto.getUrlArchivo());
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El archivo físico no está disponible en el servidor.");
            }

            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (adjunto.getTipoArchivo() != null && !adjunto.getTipoArchivo().isBlank()) {
                try {
                    mediaType = MediaType.parseMediaType(adjunto.getTipoArchivo());
                } catch (Exception ignored) {}
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + adjunto.getNombreArchivo() + "\"")
                    .body(recurso);

        } catch (Exception ex) {
            log.error("Error al leer el archivo adjunto id={}", adjuntoId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al servir el archivo adjunto.");
        }
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