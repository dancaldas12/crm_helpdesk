package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.Notificacion;
import com.serviclient.crm.entity.enums.TipoNotificacion;
import com.serviclient.crm.repository.NotificacionRepository;
import com.serviclient.crm.service.IEmpresaService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

  private final IEmpresaService empresaService;
  private final NotificacionRepository notificacionRepository;

  @ModelAttribute("currentUser")
  public CustomUserDetails getCurrentUser(Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails;
    }
    return null;
  }

  @ModelAttribute("currentEmpresa")
  public Empresa getCurrentEmpresa(Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails
        && userDetails.getEmpresaId() != null) {
      return empresaService.obtenerPorId(userDetails.getEmpresaId());
    }
    return empresaService.obtenerPrimera();
  }

  @ModelAttribute("currentUri")
  public String getCurrentUri(HttpServletRequest request) {
    return request.getRequestURI();
  }

  private static final List<TipoNotificacion> ALERTAS_ACTIVAS =
      List.of(
          TipoNotificacion.CLIENTE_EN_RIESGO,
          TipoNotificacion.RENOVACION_PROXIMA,
          TipoNotificacion.SLA_PROXIMO_VENCER,
          TipoNotificacion.SLA_INCUMPLIDO,
          TipoNotificacion.CSAT_BAJO);

  @ModelAttribute("notificacionesHeader")
  public List<Notificacion> getNotificacionesHeader(Authentication authentication) {

    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails usuario) {

      return notificacionRepository.listarVisibles(
          usuario.getId(), ALERTAS_ACTIVAS, PageRequest.of(0, 20));
    }

    return List.of();
  }

  @ModelAttribute("totalNotificaciones")
  public long getTotalNotificaciones(Authentication authentication) {

    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails usuario) {

      return notificacionRepository.contarVisibles(usuario.getId(), ALERTAS_ACTIVAS);
    }

    return 0L;
  }
}
