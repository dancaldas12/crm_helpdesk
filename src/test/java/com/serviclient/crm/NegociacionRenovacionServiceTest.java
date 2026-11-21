package com.serviclient.crm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.*;
import com.serviclient.crm.repository.*;
import com.serviclient.crm.service.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NegociacionRenovacionServiceTest {

  @Mock private RenovacionRepository renovacionRepository;

  @Mock private RenovacionSeguimientoRepository seguimientoRepository;

  @Mock private ClienteServicioRepository clienteServicioRepository;

  @Mock private HealthScoreService healthScoreService;

  @Mock private NotificacionService notificacionService;

  @InjectMocks private NegociacionRenovacionService service;

  private Empresa empresa;
  private Cliente cliente;
  private ClienteServicio contrato;
  private Renovacion renovacion;
  private CuentaPortal cuenta;

  @BeforeEach
  void setUp() {

    empresa = Empresa.builder().id(1L).nombreComercial("ServiClient").build();

    cliente =
        Cliente.builder().id(20L).empresa(empresa).nombreComercial("Banco Metropolitano").build();

    contrato =
        ClienteServicio.builder()
            .id(30L)
            .cliente(cliente)
            .fechaFin(LocalDate.now().minusDays(5))
            .estado(EstadoServicioCliente.ACTIVO)
            .build();

    renovacion =
        Renovacion.builder()
            .id(40L)
            .empresa(empresa)
            .cliente(cliente)
            .clienteServicio(contrato)
            .fechaVencimiento(LocalDate.now().minusDays(5))
            .fechaPropuesta(LocalDate.now().plusYears(1))
            .pendienteDe("CLIENTE")
            .estado(EstadoRenovacion.EN_NEGOCIACION)
            .build();

    Contacto contacto =
        Contacto.builder().id(50L).cliente(cliente).esContactoPrincipal(true).build();

    cuenta = CuentaPortal.builder().id(60L).contacto(contacto).estado("ACTIVO").build();
  }

  @Test
  void otroClienteNoPuedeAceptarPropuesta() {

    when(renovacionRepository.findById(40L)).thenReturn(Optional.of(renovacion));

    Cliente otroCliente = Cliente.builder().id(999L).build();

    cuenta.getContacto().setCliente(otroCliente);

    assertThrows(
        IllegalArgumentException.class,
        () -> service.responderCliente(40L, cuenta, "ACEPTAR", null, ""));

    verify(renovacionRepository, never()).save(any());
    verifyNoInteractions(clienteServicioRepository);
  }

  @Test
  void contactoSecundarioNoPuedeAceptarPropuesta() {

    when(renovacionRepository.findById(40L)).thenReturn(Optional.of(renovacion));

    cuenta.getContacto().setEsPrincipal(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> service.responderCliente(40L, cuenta, "ACEPTAR", null, ""));

    verify(renovacionRepository, never()).save(any());
    verifyNoInteractions(clienteServicioRepository);
  }

  @Test
  void contraofertaNoModificaContrato() {

    when(renovacionRepository.findById(40L)).thenReturn(Optional.of(renovacion));

    LocalDate fechaAnterior = contrato.getFechaFin();
    LocalDate nuevaFecha = LocalDate.now().plusMonths(9);

    service.responderCliente(40L, cuenta, "CONTRAOFERTA", nuevaFecha, "Solicitamos otra fecha.");

    assertEquals("ADMIN", renovacion.getPendienteDe());
    assertEquals(nuevaFecha, renovacion.getFechaPropuesta());
    assertEquals(fechaAnterior, contrato.getFechaFin());

    verifyNoInteractions(clienteServicioRepository);
  }

  @Test
  void aceptarPropuestaRenuevaContrato() {

    when(renovacionRepository.findById(40L)).thenReturn(Optional.of(renovacion));

    LocalDate fechaAceptada = renovacion.getFechaPropuesta();

    service.responderCliente(40L, cuenta, "ACEPTAR", null, "Aceptamos.");

    assertEquals(EstadoRenovacion.RENOVADO, renovacion.getEstado());

    assertEquals(fechaAceptada, contrato.getFechaFin());

    assertNull(renovacion.getPendienteDe());

    verify(clienteServicioRepository).save(contrato);

    ArgumentCaptor<Renovacion> captor = ArgumentCaptor.forClass(Renovacion.class);

    verify(renovacionRepository, times(2)).save(captor.capture());

    Renovacion nuevoCiclo = captor.getAllValues().get(1);

    assertEquals(EstadoRenovacion.PENDIENTE, nuevoCiclo.getEstado());

    assertEquals(fechaAceptada, nuevoCiclo.getFechaVencimiento());
  }

  @Test
  void noPermiteAceptarDosVeces() {

    when(renovacionRepository.findById(40L)).thenReturn(Optional.of(renovacion));

    service.responderCliente(40L, cuenta, "ACEPTAR", null, "");

    assertThrows(
        IllegalArgumentException.class,
        () -> service.responderCliente(40L, cuenta, "ACEPTAR", null, ""));

    verify(clienteServicioRepository, times(1)).save(contrato);
  }

  @Test
  void rechazoDefinitivoNoExtiendeContrato() {

    when(renovacionRepository.findById(40L)).thenReturn(Optional.of(renovacion));

    LocalDate fechaAnterior = contrato.getFechaFin();

    service.responderCliente(40L, cuenta, "RECHAZAR_DEFINITIVO", null, "No deseamos renovar.");

    assertEquals(EstadoRenovacion.NO_RENOVADO, renovacion.getEstado());

    assertNull(renovacion.getPendienteDe());

    assertEquals(fechaAnterior, contrato.getFechaFin());

    verifyNoInteractions(clienteServicioRepository);
  }
}
