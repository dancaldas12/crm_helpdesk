package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dto.ClienteDto;
import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.EncuestaSatisfaccion;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.repository.EncuestaSatisfaccionRepository;
import com.serviclient.crm.repository.RenovacionRepository;
import com.serviclient.crm.service.ClienteService;
import com.serviclient.crm.service.HealthScoreService;
import com.serviclient.crm.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {

    private final ClienteService clienteService;
    private final HealthScoreService healthScoreService;
    private final UsuarioService usuarioService;
    private final EncuestaSatisfaccionRepository encuestaRepository;
    private final RenovacionRepository renovacionRepository;

    @GetMapping
    public String listarClientes(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam(value = "busqueda", required = false) String busqueda,
                                 @RequestParam(value = "estado", required = false) EstadoCliente estado,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombreComercial").ascending());
        Page<Cliente> clientesPage = clienteService.listarClientes(empresaId, busqueda, estado, pageable);
        ClienteService.MetricasClientes metricas = clienteService.obtenerMetricas(empresaId);

        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("metricas", metricas);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("estados", EstadoCliente.values());
        model.addAttribute("activeMenu", "clientes");

        return "clientes/index";
    }

    @GetMapping("/nuevo")
    public String formularioNuevoCliente(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;
        List<Usuario> usuarios = usuarioService.obtenerTodosPorEmpresa(empresaId);

        model.addAttribute("clienteDto", new ClienteDto());
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("activeMenu", "clientes");
        return "clientes/form";
    }

    @PostMapping
    public String guardarCliente(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("clienteDto") ClienteDto dto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;

        if (bindingResult.hasErrors()) {
            List<Usuario> usuarios = usuarioService.obtenerTodosPorEmpresa(empresaId);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("activeMenu", "clientes");
            return "clientes/form";
        }

        try {
            Cliente guardado = clienteService.crearCliente(empresaId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente " + guardado.getNombreComercial() + " creado exitosamente.");
            return "redirect:/clientes/" + guardado.getId();
        } catch (Exception ex) {
            log.error("Error al crear cliente", ex);
            model.addAttribute("errorMessage", "Error al crear cliente: " + ex.getMessage());
            model.addAttribute("usuarios", usuarioService.obtenerTodosPorEmpresa(empresaId));
            model.addAttribute("activeMenu", "clientes");
            return "clientes/form";
        }
    }

    @GetMapping("/{id}")
    public String verCliente360(@PathVariable("id") Long id,
                                @RequestParam(value = "tab", defaultValue = "resumen") String tab,
                                Model model) {
        Cliente cliente = clienteService.obtenerPorId(id);
        ClienteService.KpisCliente360 kpis = clienteService.obtenerKpisCliente360(cliente);
        List<HealthScoreService.FactorSalud> factores = healthScoreService.obtenerFactoresRiesgo(cliente);
        List<EncuestaSatisfaccion> encuestas = encuestaRepository.findByClienteId(id);
        List<Renovacion> renovaciones = renovacionRepository.findByClienteId(id);

        model.addAttribute("cliente", cliente);
        model.addAttribute("kpis", kpis);
        model.addAttribute("factores", factores);
        model.addAttribute("encuestas", encuestas);
        model.addAttribute("renovaciones", renovaciones);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activeMenu", "clientes");

        return "clientes/cliente360";
    }

    @PostMapping("/{id}/contactos")
    public String agregarContacto(@PathVariable("id") Long clienteId,
                                  @ModelAttribute Contacto contacto,
                                  RedirectAttributes redirectAttributes) {
        try {
            clienteService.agregarContacto(clienteId, contacto);
            redirectAttributes.addFlashAttribute("successMessage", "Contacto agregado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al agregar contacto: " + ex.getMessage());
        }
        return "redirect:/clientes/" + clienteId + "?tab=contactos";
    }
}
