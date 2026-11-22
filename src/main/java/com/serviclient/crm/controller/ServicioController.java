package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.entity.Servicio;
import com.serviclient.crm.service.ServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {

  private final ServicioService servicioService;

  @GetMapping
  public String listar(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

    Long empresaId = userDetails.getEmpresaId();

    List<Servicio> servicios = servicioService.listar(empresaId);

    long activos = servicios.stream().filter(s -> "ACTIVO".equals(s.getEstado())).count();

    model.addAttribute("servicios", servicios);
    model.addAttribute("totalServicios", servicios.size());
    model.addAttribute("serviciosActivos", activos);
    model.addAttribute("serviciosInactivos", servicios.size() - activos);

    return "servicios/index";
  }

  @PostMapping
  public String crear(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam("codigo") String codigo,
      @RequestParam("nombre") String nombre,
      @RequestParam(value = "descripcion", required = false) String descripcion,
      RedirectAttributes flash) {

    try {

      servicioService.crear(userDetails.getEmpresaId(), codigo, nombre, descripcion);

      flash.addFlashAttribute("successMessage", "Servicio registrado correctamente.");

    } catch (IllegalArgumentException ex) {

      flash.addFlashAttribute("errorMessage", ex.getMessage());
    }

    return "redirect:/servicios";
  }

  @PostMapping("/{id}/editar")
  public String editar(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long id,
      @RequestParam("codigo") String codigo,
      @RequestParam("nombre") String nombre,
      @RequestParam(value = "descripcion", required = false) String descripcion,
      RedirectAttributes flash) {

    try {

      servicioService.actualizar(id, userDetails.getEmpresaId(), codigo, nombre, descripcion);

      flash.addFlashAttribute("successMessage", "Servicio actualizado correctamente.");

    } catch (IllegalArgumentException ex) {

      flash.addFlashAttribute("errorMessage", ex.getMessage());
    }

    return "redirect:/servicios";
  }

  @PostMapping("/{id}/estado")
  public String cambiarEstado(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long id,
      @RequestParam("activo") boolean activo,
      RedirectAttributes flash) {

    try {

      servicioService.cambiarEstado(id, userDetails.getEmpresaId(), activo);

      flash.addFlashAttribute(
          "successMessage",
          activo ? "Servicio activado correctamente." : "Servicio desactivado correctamente.");

    } catch (IllegalArgumentException ex) {

      flash.addFlashAttribute("errorMessage", ex.getMessage());
    }

    return "redirect:/servicios";
  }
}
