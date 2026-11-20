package com.serviclient.crm.service;

import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.Rol;
import com.serviclient.crm.entity.enums.TipoNotificacion;
import com.serviclient.crm.repository.EmpresaRepository;
import com.serviclient.crm.repository.RenovacionRepository;
import com.serviclient.crm.repository.TicketRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertaProgramadaService {

  private final EmpresaRepository empresaRepository;
  private final RenovacionRepository renovacionRepository;
  private final TicketRepository ticketRepository;
  private final NotificacionService notificacionService;

  // Primera revisión a los 30 segundos.
  // Después, cada 5 minutos.
  @Scheduled(initialDelay = 30000, fixedDelay = 300000)
  @Transactional
  public void verificarAlertas() {

    LocalDate hoy = LocalDate.now();
    LocalDateTime ahora = LocalDateTime.now();

    for (Empresa empresa : empresaRepository.findAll()) {

      Long empresaId = empresa.getId();

      // 1. RENOVACIONES
      for (Renovacion renovacion : renovacionRepository.findByEmpresaId(empresaId)) {

        EstadoRenovacion estado = renovacion.getEstado();

        boolean pendiente =
            estado != EstadoRenovacion.RENOVADO
                && estado != EstadoRenovacion.NO_RENOVADO
                && estado != EstadoRenovacion.NO_REALIZADO;

        int diasAlerta =
            renovacion.getDiasAlerta() != null ? Math.max(0, renovacion.getDiasAlerta()) : 30;

        boolean proxima =
            pendiente
                && renovacion.getFechaVencimiento() != null
                && !renovacion.getFechaVencimiento().isAfter(hoy.plusDays(diasAlerta));

        Usuario responsable = renovacion.getResponsable();

        // Solo los roles autorizados pueden abrir Renovaciones.
        if (responsable != null
            && responsable.getRol() != Rol.ADMIN
            && responsable.getRol() != Rol.SUPERVISOR) {
          responsable = null;
        }

        notificacionService.sincronizarAlerta(
            empresa,
            TipoNotificacion.RENOVACION_PROXIMA,
            "RENOVACION",
            renovacion.getId(),
            proxima,
            "Renovación próxima",
            "El contrato del cliente "
                + renovacion.getCliente().getNombreComercial()
                + " requiere seguimiento.",
            responsable);
      }

      // 2. SLA DE TICKETS
      for (Ticket ticket : ticketRepository.findByEmpresaId(empresaId)) {

        boolean pendiente =
            ticket.getEstado() != EstadoTicket.RESUELTO
                && ticket.getEstado() != EstadoTicket.CERRADO;

        LocalDateTime limite = ticket.getSlaResolucionLimite();

        boolean incumplido = pendiente && limite != null && !limite.isAfter(ahora);

        boolean proximo =
            pendiente && limite != null && !incumplido && !limite.isAfter(ahora.plusHours(4));

        notificacionService.sincronizarAlerta(
            empresa,
            TipoNotificacion.SLA_PROXIMO_VENCER,
            "TICKET",
            ticket.getId(),
            proximo,
            "SLA próximo a vencer: " + ticket.getNumeroTicket(),
            "El plazo de resolución del ticket "
                + ticket.getNumeroTicket()
                + " está próximo a vencer.",
            ticket.getAgente());

        notificacionService.sincronizarAlerta(
            empresa,
            TipoNotificacion.SLA_INCUMPLIDO,
            "TICKET",
            ticket.getId(),
            incumplido,
            "SLA incumplido: " + ticket.getNumeroTicket(),
            "El ticket " + ticket.getNumeroTicket() + " superó su plazo de resolución.",
            ticket.getAgente());
      }
    }
  }
}
