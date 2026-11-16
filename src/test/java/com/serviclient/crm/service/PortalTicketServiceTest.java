package com.serviclient.crm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.EncuestaCsat;
import com.serviclient.crm.entity.RespuestaCsat;
import com.serviclient.crm.entity.enums.EstadoEncuestaCsat;
import com.serviclient.crm.repository.EncuestaCsatRepository;
import com.serviclient.crm.repository.RespuestaCsatRepository;
import com.serviclient.crm.repository.TicketAdjuntoRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PortalTicketServiceTest {

  @Mock private TicketService ticketService;

  @Mock private TicketAdjuntoRepository ticketAdjuntoRepository;

  @Mock private EncuestaCsatRepository encuestaCsatRepository;

  @Mock private RespuestaCsatRepository respuestaCsatRepository;

  @Mock private HealthScoreService healthScoreService;

  @InjectMocks private PortalTicketService portalTicketService;

  private Cliente cliente;
  private CuentaPortal cuenta;
  private EncuestaCsat encuesta;

  @BeforeEach
  void setUp() {
    cliente = Cliente.builder().id(10L).build();

    Contacto contacto = Contacto.builder().id(20L).cliente(cliente).build();

    cuenta = CuentaPortal.builder().contacto(contacto).build();

    encuesta =
        EncuestaCsat.builder()
            .id(30L)
            .cliente(cliente)
            .contacto(contacto)
            .estado(EstadoEncuestaCsat.PENDIENTE)
            .activa(true)
            .build();
  }

  @Test
  void debeGuardarEncuestaValida() {
    when(encuestaCsatRepository.findById(30L)).thenReturn(Optional.of(encuesta));

    portalTicketService.responderEncuestaCsat(30L, 5, "Excelente atención", cuenta);

    ArgumentCaptor<RespuestaCsat> captor = ArgumentCaptor.forClass(RespuestaCsat.class);

    verify(respuestaCsatRepository).save(captor.capture());

    RespuestaCsat respuesta = captor.getValue();

    assertEquals(5, respuesta.getPuntuacion());
    assertEquals("Excelente atención", respuesta.getComentario());
    assertEquals(encuesta, respuesta.getEncuesta());

    assertEquals(EstadoEncuestaCsat.RESPONDIDA, encuesta.getEstado());

    assertNotNull(encuesta.getFechaRespuesta());

    verify(encuestaCsatRepository).save(encuesta);
    verify(healthScoreService).recalcularYActualizarHealthScore(cliente);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(ints = {0, -1, 6, 10})
  void debeRechazarCalificacionesInvalidas(Integer puntuacion) {
    when(encuestaCsatRepository.findById(30L)).thenReturn(Optional.of(encuesta));

    assertThrows(
        IllegalArgumentException.class,
        () -> portalTicketService.responderEncuestaCsat(30L, puntuacion, "Prueba", cuenta));

    verifyNoInteractions(respuestaCsatRepository);
    verify(healthScoreService, never()).recalcularYActualizarHealthScore(any());
  }

  @Test
  void debeRechazarEncuestaDeOtroContacto() {
    Contacto otroContacto = Contacto.builder().id(99L).cliente(cliente).build();

    CuentaPortal otraCuenta = CuentaPortal.builder().contacto(otroContacto).build();

    when(encuestaCsatRepository.findById(30L)).thenReturn(Optional.of(encuesta));

    assertThrows(
        ResponseStatusException.class,
        () -> portalTicketService.responderEncuestaCsat(30L, 5, "Prueba", otraCuenta));

    verifyNoInteractions(respuestaCsatRepository);
  }

  @Test
  void debeRechazarEncuestaYaRespondida() {
    encuesta.setEstado(EstadoEncuestaCsat.RESPONDIDA);

    when(encuestaCsatRepository.findById(30L)).thenReturn(Optional.of(encuesta));

    assertThrows(
        IllegalArgumentException.class,
        () -> portalTicketService.responderEncuestaCsat(30L, 5, "Prueba", cuenta));

    verifyNoInteractions(respuestaCsatRepository);
  }

  @Test
  void debeRechazarComentarioDemasiadoLargo() {
    String comentario = "A".repeat(2001);

    when(encuestaCsatRepository.findById(30L)).thenReturn(Optional.of(encuesta));

    assertThrows(
        IllegalArgumentException.class,
        () -> portalTicketService.responderEncuestaCsat(30L, 5, comentario, cuenta));

    verifyNoInteractions(respuestaCsatRepository);
  }
}
