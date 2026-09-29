package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.repository.EncuestaCsatRepository;
import com.serviclient.crm.repository.TicketRepository;
import com.serviclient.crm.service.ClienteService;
import com.serviclient.crm.service.RenovacionService;
import com.serviclient.crm.service.TicketService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

  private final ClienteService clienteService;
  private final TicketService ticketService;
  private final RenovacionService renovacionService;
  private final TicketRepository ticketRepository;
  private final EncuestaCsatRepository encuestaCsatRepository;

  @GetMapping("/dashboard")
  public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

    Long empresaId =
        (userDetails != null && userDetails.getEmpresaId() != null)
            ? userDetails.getEmpresaId()
            : 1L;

    var clientes = clienteService.obtenerMetricas(empresaId);
    var tickets = ticketService.obtenerMetricas(empresaId);
    var renovaciones = renovacionService.obtenerMetricas(empresaId);

    long totalTickets = ticketRepository.countByEmpresaId(empresaId);

    Double csatPromedio = encuestaCsatRepository.findAverageScoreByEmpresaId(empresaId);

    if (csatPromedio == null) {
      csatPromedio = 0.0;
    }

    int satisfaccionPorcentaje = (int) Math.round((csatPromedio / 5.0) * 100);

    long totalEncuestas = encuestaCsatRepository.findByEmpresaId(empresaId).size();

    List<Renovacion> proximasRenovaciones =
        renovacionService.listarProximasRenovaciones(empresaId).stream()
            .filter(r -> r.getDiasRestantes() >= 0 && r.getDiasRestantes() <= 90)
            .limit(3)
            .toList();

    long totalClientes = clientes.getTotalClientes();

    int porcentajeSaludables =
        totalClientes > 0 ? (int) Math.round(clientes.getSaludables() * 100.0 / totalClientes) : 0;

    int porcentajeObservacion =
        totalClientes > 0
            ? (int) Math.round(clientes.getEnObservacion() * 100.0 / totalClientes)
            : 0;

    int porcentajeRiesgo =
        totalClientes > 0 ? 100 - porcentajeSaludables - porcentajeObservacion : 0;

    model.addAttribute("clientesMetricas", clientes);
    model.addAttribute("ticketMetricas", tickets);
    model.addAttribute("renovacionMetricas", renovaciones);

    model.addAttribute("totalTickets", totalTickets);

    model.addAttribute("csatPromedio", Math.round(csatPromedio * 10.0) / 10.0);

    model.addAttribute("satisfaccionPorcentaje", satisfaccionPorcentaje);

    model.addAttribute("totalEncuestas", totalEncuestas);

    model.addAttribute("proximasRenovaciones", proximasRenovaciones);

    model.addAttribute("porcentajeSaludables", porcentajeSaludables);

    model.addAttribute("porcentajeObservacion", porcentajeObservacion);

    model.addAttribute("porcentajeRiesgo", porcentajeRiesgo);

    model.addAttribute("activeMenu", "dashboard");

    return "dashboard/index";
  }
}
