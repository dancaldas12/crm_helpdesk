package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dto.TicketMensajeDto;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import com.serviclient.crm.service.RenovacionService;
import com.serviclient.crm.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/renovaciones")
@RequiredArgsConstructor
@Slf4j
public class RenovacionController {

    private final RenovacionService renovacionService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarRenovaciones(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;

        RenovacionService.MetricasRenovaciones metricas = renovacionService.obtenerMetricas(empresaId);
        List<Renovacion> renovaciones = renovacionService.listarProximasRenovaciones(empresaId);
        List<Usuario> agentes = usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId);

        model.addAttribute("metricas", metricas);
        model.addAttribute("renovaciones", renovaciones);
        model.addAttribute("agentes", agentes);
        model.addAttribute("estadosRenovacion", EstadoRenovacion.values());
        model.addAttribute("nuevoSeguimiento", new TicketMensajeDto.RegistrarSeguimientoRenovacion());
        model.addAttribute("fechaHoy", LocalDate.now());
        model.addAttribute("activeMenu", "renovaciones");

        return "renovaciones/index";
    }

    @PostMapping("/seguimiento")
    public String registrarSeguimiento(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @Valid @ModelAttribute("nuevoSeguimiento") TicketMensajeDto.RegistrarSeguimientoRenovacion dto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos incompletos para registrar el seguimiento.");
            return "redirect:/renovaciones";
        }

        try {
            renovacionService.registrarSeguimiento(dto, autor);
            redirectAttributes.addFlashAttribute("successMessage", "Seguimiento de renovación guardado exitosamente.");
        } catch (Exception ex) {
            log.error("Error al registrar seguimiento", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar seguimiento: " + ex.getMessage());
        }

        return "redirect:/renovaciones";
    }
}
