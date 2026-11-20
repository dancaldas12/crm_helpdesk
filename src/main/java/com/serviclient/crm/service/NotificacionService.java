package com.serviclient.crm.service;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.Notificacion;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import com.serviclient.crm.entity.enums.TipoNotificacion;
import com.serviclient.crm.repository.NotificacionRepository;
import com.serviclient.crm.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificacionService {

  private final NotificacionRepository notificacionRepository;
  private final UsuarioRepository usuarioRepository;

  @Transactional
  public void notificarAsignacionTicket(Ticket ticket, Usuario agente) {

    if (ticket == null
        || ticket.getId() == null
        || ticket.getEmpresa() == null
        || agente == null
        || agente.getEmpresa() == null) {
      throw new IllegalArgumentException("Datos incompletos para notificar la asignación.");
    }

    Long empresaId = ticket.getEmpresa().getId();

    if (!empresaId.equals(agente.getEmpresa().getId())) {
      throw new IllegalArgumentException("El agente pertenece a otra empresa.");
    }

    // Administradores de la empresa
    List<Usuario> destinatarios =
        new ArrayList<>(usuarioRepository.findByEmpresaIdAndRol(empresaId, Rol.ADMIN));

    // Agente que recibió el ticket
    destinatarios.add(agente);

    // Evitar destinatarios repetidos
    Set<Long> notificados = new HashSet<>();

    for (Usuario usuario : destinatarios) {

      if (!Boolean.TRUE.equals(usuario.getActivo()) || !notificados.add(usuario.getId())) {
        continue;
      }

      Notificacion notificacion =
          Notificacion.builder()
              .empresa(ticket.getEmpresa())
              .usuario(usuario)
              .tipo(TipoNotificacion.TICKET_ASIGNADO)
              .titulo("Ticket asignado: " + ticket.getNumeroTicket())
              .mensaje(
                  "El ticket '"
                      + ticket.getAsunto()
                      + "' fue asignado a "
                      + agente.getNombreCompleto()
                      + ".")
              .entidadTipo("TICKET")
              .entidadId(ticket.getId())
              .leida(false)
              .build();

      notificacionRepository.save(notificacion);
    }
  }

  @Transactional
  public void sincronizarRiesgoCliente(Cliente cliente, int puntaje) {

    Long empresaId = cliente.getEmpresa().getId();

    List<Notificacion> existentes =
        notificacionRepository.findByEmpresaIdAndTipoAndEntidadTipoAndEntidadIdAndResueltaFalse(
            empresaId, TipoNotificacion.CLIENTE_EN_RIESGO, "CLIENTE", cliente.getId());

    // Si el cliente salió del riesgo, resolver las alertas.
    if (puntaje >= 50) {
      for (Notificacion notificacion : existentes) {
        notificacion.setResuelta(true);
      }

      notificacionRepository.saveAll(existentes);
      return;
    }

    // Notificar a todos los administradores activos de la empresa.
    List<Usuario> administradores = usuarioRepository.findByEmpresaIdAndRol(empresaId, Rol.ADMIN);

    for (Usuario admin : administradores) {

      if (!Boolean.TRUE.equals(admin.getActivo())) {
        continue;
      }

      boolean yaExiste =
          existentes.stream().anyMatch(n -> n.getUsuario().getId().equals(admin.getId()));

      // No duplicar alertas activas.
      if (yaExiste) {
        continue;
      }

      Notificacion alerta =
          Notificacion.builder()
              .empresa(cliente.getEmpresa())
              .usuario(admin)
              .tipo(TipoNotificacion.CLIENTE_EN_RIESGO)
              .titulo("Cliente en riesgo: " + cliente.getNombreComercial())
              .mensaje(
                  "El Health Score del cliente es " + puntaje + "/100. Se requiere seguimiento.")
              .entidadTipo("CLIENTE")
              .entidadId(cliente.getId())
              .leida(false)
              .resuelta(false)
              .build();

      notificacionRepository.save(alerta);
    }
  }

  @Transactional
  public void sincronizarAlerta(
      Empresa empresa,
      TipoNotificacion tipo,
      String entidadTipo,
      Long entidadId,
      boolean activa,
      String titulo,
      String mensaje,
      Usuario responsable) {

    Long empresaId = empresa.getId();

    List<Notificacion> existentes =
        notificacionRepository.findByEmpresaIdAndTipoAndEntidadTipoAndEntidadIdAndResueltaFalse(
            empresaId, tipo, entidadTipo, entidadId);

    // Si el problema se solucionó, cerrar las alertas existentes.
    if (!activa) {
      existentes.forEach(n -> n.setResuelta(true));
      notificacionRepository.saveAll(existentes);
      return;
    }

    List<Usuario> destinatarios =
        new ArrayList<>(usuarioRepository.findByEmpresaIdAndRol(empresaId, Rol.ADMIN));

    // Notificar también al responsable si pertenece a la empresa.
    if (responsable != null
        && responsable.getEmpresa() != null
        && empresaId.equals(responsable.getEmpresa().getId())) {
      destinatarios.add(responsable);
    }

    Set<Long> procesados = new HashSet<>();

    for (Usuario usuario : destinatarios) {

      if (!Boolean.TRUE.equals(usuario.getActivo()) || !procesados.add(usuario.getId())) {
        continue;
      }

      boolean yaExiste =
          existentes.stream().anyMatch(n -> n.getUsuario().getId().equals(usuario.getId()));

      if (yaExiste) {
        continue;
      }

      notificacionRepository.save(
          Notificacion.builder()
              .empresa(empresa)
              .usuario(usuario)
              .tipo(tipo)
              .titulo(titulo.length() > 150 ? titulo.substring(0, 150) : titulo)
              .mensaje(mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje)
              .entidadTipo(entidadTipo)
              .entidadId(entidadId)
              .leida(false)
              .resuelta(false)
              .build());
    }
  }

  @Transactional
  public void notificarActualizacionTicket(Ticket ticket) {

    Long empresaId = ticket.getEmpresa().getId();

    List<Usuario> destinatarios =
        new ArrayList<>(usuarioRepository.findByEmpresaIdAndRol(empresaId, Rol.ADMIN));

    if (ticket.getAgente() != null) {
      destinatarios.add(ticket.getAgente());
    }

    Set<Long> procesados = new HashSet<>();

    for (Usuario usuario : destinatarios) {

      if (!Boolean.TRUE.equals(usuario.getActivo()) || !procesados.add(usuario.getId())) {
        continue;
      }

      notificacionRepository.save(
          Notificacion.builder()
              .empresa(ticket.getEmpresa())
              .usuario(usuario)
              .tipo(TipoNotificacion.TICKET_ACTUALIZADO)
              .titulo("Ticket actualizado: " + ticket.getNumeroTicket())
              .mensaje(
                  "Se modificó el estado o la prioridad "
                      + "del ticket "
                      + ticket.getNumeroTicket()
                      + ".")
              .entidadTipo("TICKET")
              .entidadId(ticket.getId())
              .leida(false)
              .build());
    }
  }
}
