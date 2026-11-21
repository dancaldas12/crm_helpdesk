package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.entity.Notificacion;
import com.serviclient.crm.entity.enums.Rol;
import com.serviclient.crm.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

  private final NotificacionRepository notificacionRepository;

  @PostMapping("/{id}/abrir")
  public String abrirNotificacion(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails usuario) {

    if (usuario == null) {
      return "redirect:/login";
    }

    Notificacion notificacion =
        notificacionRepository
            .findByIdAndUsuarioId(id, usuario.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // Confirmar que pertenece a la empresa autenticada.
    if (!notificacion.getEmpresa().getId().equals(usuario.getEmpresaId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    // Marcar como leída.
    if (!Boolean.TRUE.equals(notificacion.getLeida())) {
      notificacion.setLeida(true);
      notificacionRepository.save(notificacion);
    }

    Long entidadId = notificacion.getEntidadId();
    String entidadTipo = notificacion.getEntidadTipo();

    if (entidadId == null || entidadTipo == null) {
      return "redirect:/dashboard";
    }

    // Redirigir según la entidad relacionada.
    return switch (entidadTipo) {
      case "CLIENTE" -> "redirect:/clientes/" + entidadId;

      case "TICKET" ->
          usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.AGENTE
              ? "redirect:/tickets/" + entidadId
              : "redirect:/dashboard";

      case "RENOVACION" ->
          usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.SUPERVISOR
              ? "redirect:/renovaciones"
              : "redirect:/dashboard";

      case "NEGOCIACION" ->
          usuario.getRol() == Rol.ADMIN ? "redirect:/renovaciones" : "redirect:/dashboard";

      default -> "redirect:/dashboard";
    };
  }
}
