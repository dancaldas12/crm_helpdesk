package com.serviclient.crm.service;

import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.*;
import com.serviclient.crm.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NegociacionRenovacionService {

  private final RenovacionRepository renovacionRepository;
  private final RenovacionSeguimientoRepository seguimientoRepository;
  private final ClienteServicioRepository clienteServicioRepository;
  private final HealthScoreService healthScoreService;
  private final NotificacionService notificacionService;

  // ADMINISTRADOR: enviar o modificar una propuesta.
  @Transactional
  public void enviarPropuesta(
      Long renovacionId, Usuario administrador, LocalDate nuevaFecha, String mensaje) {

    Renovacion r = buscarActiva(renovacionId);
    validarAdministrador(r, administrador);
    validarFecha(r, nuevaFecha);

    r.setEstado(EstadoRenovacion.EN_NEGOCIACION);
    r.setFechaPropuesta(nuevaFecha);
    r.setPendienteDe("CLIENTE");
    r.setMensajePropuesta(mensaje);
    renovacionRepository.save(r);

    registrar(
        r,
        administrador,
        "ADMIN: propuesta enviada. Nueva fecha: " + nuevaFecha + ". " + texto(mensaje));

    // La respuesta anterior ya fue atendida.
    cerrarAvisoRespuesta(r);
  }

  // CLIENTE: aceptar, contraofertar o rechazar.
  @Transactional
  public void responderCliente(
      Long renovacionId, CuentaPortal cuenta, String accion, LocalDate nuevaFecha, String mensaje) {

    Renovacion r = buscarActiva(renovacionId);

    if (cuenta == null
        || !cuenta.estaActiva()
        || cuenta.getContacto() == null
        || cuenta.getContacto().getCliente() == null
        || !r.getCliente().getId().equals(cuenta.getContacto().getCliente().getId())) {
      throw new IllegalArgumentException("No tienes permiso para responder esta renovación.");
    }

    if (!Boolean.TRUE.equals(cuenta.getContacto().getEsPrincipal())) {
      throw new IllegalArgumentException("Solo el contacto principal puede aprobar la renovación.");
    }

    if (!"CLIENTE".equals(r.getPendienteDe())) {
      throw new IllegalArgumentException("No existe una propuesta pendiente de tu respuesta.");
    }

    String actor = "CLIENTE (contacto ID " + cuenta.getContacto().getId() + "): ";

    switch (accion) {
      case "ACEPTAR" -> {
        LocalDate fechaAceptada = r.getFechaPropuesta();
        validarFecha(r, fechaAceptada);

        confirmarRenovacion(r, fechaAceptada);

        registrar(r, null, actor + "aceptó la renovación hasta " + fechaAceptada);
        cerrarAvisoRespuesta(r);
      }

      case "CONTRAOFERTA" -> {
        validarFecha(r, nuevaFecha);

        r.setFechaPropuesta(nuevaFecha);
        r.setPendienteDe("ADMIN");
        r.setMensajePropuesta(mensaje);
        renovacionRepository.save(r);

        registrar(
            r, null, actor + "envió contraoferta hasta " + nuevaFecha + ". " + texto(mensaje));

        notificarRespuesta(r, "Nueva contraoferta del cliente");
      }

      case "RECHAZAR" -> {
        r.setFechaPropuesta(null);
        r.setPendienteDe("ADMIN");
        r.setMensajePropuesta(mensaje);
        renovacionRepository.save(r);

        registrar(r, null, actor + "rechazó la propuesta, pero desea negociar. " + texto(mensaje));

        notificarRespuesta(r, "El cliente solicita otra propuesta");
      }

      case "RECHAZAR_DEFINITIVO" -> {
        r.setEstado(EstadoRenovacion.NO_RENOVADO);
        r.setPendienteDe(null);
        r.setFechaPropuesta(null);
        r.setMensajePropuesta(mensaje);
        renovacionRepository.save(r);

        registrar(r, null, actor + "rechazó definitivamente la renovación. " + texto(mensaje));

        notificarRespuesta(r, "Renovación rechazada definitivamente");
      }

      default -> throw new IllegalArgumentException("Acción de renovación no válida.");
    }

    healthScoreService.recalcularYActualizarHealthScore(r.getCliente());
  }

  // ADMINISTRADOR: aceptar la contraoferta del cliente.
  @Transactional
  public void aceptarContraoferta(Long renovacionId, Usuario administrador) {

    Renovacion r = buscarActiva(renovacionId);
    validarAdministrador(r, administrador);

    if (!"ADMIN".equals(r.getPendienteDe()) || r.getFechaPropuesta() == null) {
      throw new IllegalArgumentException("No hay una contraoferta pendiente.");
    }

    LocalDate fechaAceptada = r.getFechaPropuesta();
    validarFecha(r, fechaAceptada);

    confirmarRenovacion(r, fechaAceptada);

    registrar(r, administrador, "ADMIN: aceptó la contraoferta hasta " + fechaAceptada);

    cerrarAvisoRespuesta(r);

    healthScoreService.recalcularYActualizarHealthScore(r.getCliente());
  }

  // Actualizar el contrato solo después de la aceptación.
  private void confirmarRenovacion(Renovacion r, LocalDate nuevaFecha) {

    ClienteServicio contrato = r.getClienteServicio();

    if (contrato == null) {
      throw new IllegalStateException("La renovación no tiene un servicio asociado.");
    }

    contrato.setFechaFin(nuevaFecha);
    contrato.setEstado(EstadoServicioCliente.ACTIVO);
    clienteServicioRepository.save(contrato);

    r.setEstado(EstadoRenovacion.RENOVADO);
    r.setFechaRenovacion(LocalDate.now());
    r.setPendienteDe(null);
    r.setFechaPropuesta(null);
    renovacionRepository.save(r);

    // Nuevo ciclo en la misma tabla; no se crea otro servicio.
    Renovacion siguiente =
        Renovacion.builder()
            .empresa(r.getEmpresa())
            .cliente(r.getCliente())
            .clienteServicio(contrato)
            .responsable(r.getResponsable())
            .fechaVencimiento(nuevaFecha)
            .diasAlerta(r.getDiasAlerta())
            .estado(EstadoRenovacion.PENDIENTE)
            .build();

    renovacionRepository.save(siguiente);
  }

  private Renovacion buscarActiva(Long id) {

    Renovacion r =
        renovacionRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Renovación no encontrada."));

    if (r.getEstado() == EstadoRenovacion.RENOVADO
        || r.getEstado() == EstadoRenovacion.NO_RENOVADO
        || r.getEstado() == EstadoRenovacion.NO_REALIZADO) {
      throw new IllegalArgumentException("Esta negociación ya fue finalizada.");
    }

    return r;
  }

  private void validarAdministrador(Renovacion r, Usuario usuario) {

    if (usuario == null
        || usuario.getRol() != Rol.ADMIN
        || usuario.getEmpresa() == null
        || !r.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
      throw new IllegalArgumentException("No tienes permiso para gestionar esta renovación.");
    }
  }

  private void validarFecha(Renovacion r, LocalDate fecha) {

    if (fecha == null
        || !fecha.isAfter(LocalDate.now())
        || !fecha.isAfter(r.getFechaVencimiento())) {
      throw new IllegalArgumentException(
          "La nueva fecha debe ser futura y posterior " + "al vencimiento del contrato actual.");
    }
  }

  private void registrar(Renovacion r, Usuario autor, String comentario) {

    seguimientoRepository.save(
        RenovacionSeguimiento.builder()
            .renovacion(r)
            .usuario(autor)
            .estado(r.getEstado())
            .fecha(LocalDateTime.now())
            .comentario(comentario)
            .build());
  }

  private String texto(String mensaje) {
    return mensaje == null ? "" : mensaje.trim();
  }

  // Reutilizamos las notificaciones existentes.
  private void cerrarAvisoRespuesta(Renovacion r) {

    notificacionService.sincronizarAlerta(
        r.getEmpresa(),
        TipoNotificacion.RENOVACION_PROXIMA,
        "NEGOCIACION",
        r.getId(),
        false,
        "",
        "",
        null);
  }

  private void notificarRespuesta(Renovacion r, String titulo) {

    // Cierra el aviso anterior antes de crear el nuevo.
    cerrarAvisoRespuesta(r);

    notificacionService.sincronizarAlerta(
        r.getEmpresa(),
        TipoNotificacion.RENOVACION_PROXIMA,
        "NEGOCIACION",
        r.getId(),
        true,
        titulo,
        "El cliente "
            + r.getCliente().getNombreComercial()
            + " respondió a una propuesta de renovación.",
        null);
  }
}
