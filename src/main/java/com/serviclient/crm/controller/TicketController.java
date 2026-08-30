package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dto.TicketDto;
import com.serviclient.crm.dto.TicketMensajeDto;
import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.service.ClienteService;
import com.serviclient.crm.service.TicketService;
import com.serviclient.crm.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarTickets(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(value = "busqueda", required = false) String busqueda,
                                @RequestParam(value = "estado", required = false) EstadoTicket estado,
                                @RequestParam(value = "prioridad", required = false) PrioridadTicket prioridad,
                                @RequestParam(value = "categoria", required = false) CategoriaTicket categoria,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        Page<Ticket> ticketsPage = ticketService.listarTickets(empresaId, busqueda, estado, prioridad, categoria, pageable);
        TicketService.MetricasTickets metricas = ticketService.obtenerMetricas(empresaId);

        model.addAttribute("ticketsPage", ticketsPage);
        model.addAttribute("metricas", metricas);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("prioridadSeleccionada", prioridad);
        model.addAttribute("categoriaSeleccionada", categoria);

        model.addAttribute("estados", EstadoTicket.values());
        model.addAttribute("prioridades", PrioridadTicket.values());
        model.addAttribute("categorias", CategoriaTicket.values());
        model.addAttribute("activeMenu", "tickets");

        return "helpdesk/index";
    }

    @GetMapping("/nuevo")
    public String formularioNuevoTicket(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;
        List<Cliente> clientes = clienteService.listarTodosPorEmpresa(empresaId);
        List<Usuario> agentes = usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId);

        model.addAttribute("ticketDto", new TicketDto());
        model.addAttribute("clientes", clientes);
        model.addAttribute("agentes", agentes);
        model.addAttribute("categorias", CategoriaTicket.values());
        model.addAttribute("prioridades", PrioridadTicket.values());
        model.addAttribute("activeMenu", "tickets");

        return "helpdesk/form";
    }

    @PostMapping
    public String guardarTicket(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("ticketDto") TicketDto dto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;
        Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);

        if (bindingResult.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodosPorEmpresa(empresaId));
            model.addAttribute("agentes", usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId));
            model.addAttribute("categorias", CategoriaTicket.values());
            model.addAttribute("prioridades", PrioridadTicket.values());
            model.addAttribute("activeMenu", "tickets");
            return "helpdesk/form";
        }

        try {
            Ticket guardado = ticketService.crearTicket(empresaId, dto, autor);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket " + guardado.getCodigo() + " creado exitosamente.");
            return "redirect:/tickets/" + guardado.getId();
        } catch (Exception ex) {
            log.error("Error al crear ticket", ex);
            model.addAttribute("errorMessage", "Error al crear ticket: " + ex.getMessage());
            model.addAttribute("clientes", clienteService.listarTodosPorEmpresa(empresaId));
            model.addAttribute("agentes", usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId));
            model.addAttribute("categorias", CategoriaTicket.values());
            model.addAttribute("prioridades", PrioridadTicket.values());
            model.addAttribute("activeMenu", "tickets");
            return "helpdesk/form";
        }
    }

    @GetMapping("/{id}")
    public String verDetalleTicket(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @PathVariable("id") Long id,
                                   Model model) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;
        Ticket ticket = ticketService.obtenerPorId(id);
        List<Usuario> agentes = usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId);

        model.addAttribute("ticket", ticket);
        model.addAttribute("agentes", agentes);
        model.addAttribute("estados", EstadoTicket.values());
        model.addAttribute("prioridades", PrioridadTicket.values());
        model.addAttribute("nuevoMensaje", new TicketMensajeDto.EnviarMensaje());
        model.addAttribute("cambioEstado", new TicketMensajeDto.CambiarEstado());
        model.addAttribute("cierreTicket", new TicketMensajeDto.CerrarTicket());
        model.addAttribute("activeMenu", "tickets");

        return "helpdesk/detalle";
    }

    @PostMapping("/{id}/mensajes")
    public String enviarMensaje(@PathVariable("id") Long ticketId,
                                @RequestParam("contenido") String contenido,
                                @RequestParam(value = "esNotaInterna", defaultValue = "false") boolean esNotaInterna,
                                RedirectAttributes redirectAttributes) {
        Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);
        try {
            if (contenido != null && !contenido.trim().isBlank()) {
                ticketService.enviarMensaje(ticketId, autor, contenido.trim(), esNotaInterna);
                redirectAttributes.addFlashAttribute("successMessage", esNotaInterna ? "Comentario interno guardado." : "Respuesta enviada al cliente.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al enviar mensaje: " + ex.getMessage());
        }
        return "redirect:/tickets/" + ticketId;
    }

    @PostMapping("/{id}/asignar")
    public String asignarAgente(@PathVariable("id") Long ticketId,
                                @RequestParam("agenteId") Long agenteId,
                                RedirectAttributes redirectAttributes) {
        Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);
        try {
            ticketService.asignarAgente(ticketId, agenteId, autor);
            redirectAttributes.addFlashAttribute("successMessage", "Agente reasignado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al asignar agente: " + ex.getMessage());
        }
        return "redirect:/tickets/" + ticketId;
    }

    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable("id") Long ticketId,
                                @RequestParam(value = "estado", required = false) EstadoTicket estado,
                                @RequestParam(value = "prioridad", required = false) PrioridadTicket prioridad,
                                @RequestParam(value = "comentario", required = false) String comentario,
                                RedirectAttributes redirectAttributes) {
        Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);
        try {
            ticketService.cambiarEstadoYPrioridad(ticketId, estado, prioridad, comentario, autor);
            redirectAttributes.addFlashAttribute("successMessage", "Estado/Prioridad actualizado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar estado: " + ex.getMessage());
        }
        return "redirect:/tickets/" + ticketId;
    }

    @PostMapping("/{id}/cerrar")
    public String cerrarTicket(@PathVariable("id") Long ticketId,
                               @RequestParam(value = "enviarCsat", defaultValue = "true") Boolean enviarCsat,
                               @RequestParam(value = "comentarioCierre", required = false) String comentarioCierre,
                               RedirectAttributes redirectAttributes) {
        Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);
        try {
            ticketService.cerrarTicket(ticketId, enviarCsat, comentarioCierre, autor);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket cerrado satisfactoriamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cerrar ticket: " + ex.getMessage());
        }
        return "redirect:/tickets/" + ticketId;
    }
}
