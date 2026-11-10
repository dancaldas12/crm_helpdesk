package com.serviclient.crm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ActiveProfiles("dev")
class ClienteControllerHttpTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @WithUserDetails(
      value = "admin@serviclient.com",
      userDetailsServiceBeanName = "customUserDetailsService")
  void debeRecibirFiltroYMostrarClientes() throws Exception {

    System.out.println("\n===== PRUEBA CONTROLLER =====");
    String filtro = "Alpha";

    System.out.println("Solicitud: GET /clientes?busqueda=" + filtro);

    MvcResult resultado = mockMvc.perform(get("/clientes").param("busqueda", filtro)).andReturn();

    if (resultado.getHandler() instanceof HandlerMethod handler) {

      System.out.println("[INFO] Controlador: " + handler.getBeanType().getSimpleName());

      System.out.println("[INFO] Metodo: " + handler.getMethod().getName() + "()");

      System.out.println("[INFO] Archivo: " + handler.getBeanType().getSimpleName() + ".java");
    }

    verificar("Estado HTTP", 200, resultado.getResponse().getStatus());

    ModelAndView vista = resultado.getModelAndView();

    verificar("Vista seleccionada", "clientes/index", vista == null ? null : vista.getViewName());

    verificar("Filtro recibido", "Alpha", vista == null ? null : vista.getModel().get("busqueda"));

    verificar(
        "Listado de clientes", true, vista != null && vista.getModel().get("clientesPage") != null);

    verificar(
        "Metricas disponibles", true, vista != null && vista.getModel().get("metricas") != null);

    System.out.println("[OK] Todas las validaciones pasaron.");
    System.out.println("==============================\n");
  }

  private void verificar(String descripcion, Object esperado, Object obtenido) {

    if (Objects.equals(esperado, obtenido)) {
      System.out.println(
          "[OK] " + descripcion + " | Esperado: " + esperado + " | Obtenido: " + obtenido);
    } else {
      System.err.println("[FALLO] " + descripcion);
      System.err.println("  Esperado: " + esperado);
      System.err.println("  Obtenido: " + obtenido);
      System.err.println("  Prueba: ClienteControllerHttpTest.debeRecibirFiltroYMostrarClientes()");
    }

    assertEquals(esperado, obtenido, descripcion);
  }
}
