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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * Controlador MVC para la gestión de clientes B2B.
 *
 * <p>Proporciona operaciones CRUD básicas sobre la entidad {@link com.serviclient.crm.entity.Cliente}
 * y expone la vista 360° del cliente con sus KPIs, historial de tickets,
 * encuestas CSAT, renovaciones e interacciones.</p>
 *
 * <p><b>Rutas expuestas:</b></p>
 * <pre>
 * GET  /clientes               → listado paginado con filtros y métricas
 * GET  /clientes/nuevo         → formulario de alta
 * POST /clientes               → persistir nuevo cliente
 * GET  /clientes/{id}          → vista 360° del cliente (tabs: resumen, tickets, etc.)
 * POST /clientes/{id}/contactos → agregar contacto al cliente
 * </pre>
 *
 * <p>Todos los endpoints requeiren autenticación. El {@code empresaId} se extrae
 * del principal de Spring Security ({@link com.serviclient.crm.config.security.CustomUserDetails}).</p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.ClienteService
 * @see com.serviclient.crm.service.HealthScoreService
 */
@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "Gestión de clientes B2B: listado paginado, alta, vista 360° y administración de contactos")
public class ClienteController {

    private final ClienteService clienteService;
    private final HealthScoreService healthScoreService;
    private final UsuarioService usuarioService;
    private final EncuestaSatisfaccionRepository encuestaRepository;
    private final RenovacionRepository renovacionRepository;

    /**
     * Lista los clientes de la empresa autenticada con soporte de búsqueda,
     * filtro por estado de salud y paginación.
     *
     * @param userDetails  principal autenticado para obtener el {@code empresaId}
     * @param busqueda     término de búsqueda por nombre comercial (opcional)
     * @param estado       filtro por {@link com.serviclient.crm.entity.enums.EstadoCliente} (opcional)
     * @param page         número de página (0-indexed, por defecto 0)
     * @param size         tamaño de página (por defecto 10)
     * @param model        modelo Thymeleaf
     * @return vista {@code clientes/index} con listado paginado y métricas
     */
    @Operation(
        summary = "Listar clientes",
        description = "Obtiene el listado paginado de clientes de la empresa activa, con métricas de salud (saludables, en riesgo, churn) y filtros opcionales por nombre y estado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vista del listado de clientes cargada correctamente"),
        @ApiResponse(responseCode = "403", description = "No autenticado o sin permisos para la empresa")
    })
    @GetMapping
    public String listarClientes(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Parameter(description = "Búsqueda por nombre comercial o razón social")
                                 @RequestParam(value = "busqueda", required = false) String busqueda,
                                 @Parameter(description = "Filtro por estado de salud del cliente (SALUDABLE, ATENCION, EN_RIESGO, CHURN, INACTIVO)")
                                 @RequestParam(value = "estado", required = false) EstadoCliente estado,
                                 @Parameter(description = "Número de página (0-indexed)", example = "0")
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @Parameter(description = "Cantidad de elementos por página", example = "10")
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

    /**
     * Muestra el formulario de alta de un nuevo cliente.
     * Carga la lista de usuarios (responsables potenciales) de la empresa.
     *
     * @param userDetails principal autenticado
     * @param model       modelo Thymeleaf
     * @return vista {@code clientes/form} con DTO vacío
     */
    @Operation(
        summary = "Formulario de nuevo cliente",
        description = "Renderiza el formulario para dar de alta a un nuevo cliente corporativo, pre-cargando la lista de ejecutivos responsables asignables."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Formulario de alta renderizado correctamente")
    })
    @GetMapping("/nuevo")
    public String formularioNuevoCliente(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long empresaId = (userDetails != null && userDetails.getEmpresaId() != null) ? userDetails.getEmpresaId() : 1L;
        List<Usuario> usuarios = usuarioService.obtenerTodosPorEmpresa(empresaId);

        model.addAttribute("clienteDto", new ClienteDto());
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("activeMenu", "clientes");
        return "clientes/form";
    }

    /**
     * Persiste un nuevo cliente tras validar el DTO recibido desde el formulario.
     * En caso de éxito redirige a la vista 360° del cliente recén creado.
     *
     * @param userDetails        principal autenticado
     * @param dto                datos del cliente a crear
     * @param bindingResult      resultado de las validaciones de Bean Validation
     * @param model              modelo Thymeleaf para reportar errores
     * @param redirectAttributes atributos flash para el mensaje de éxito
     * @return redirección a {@code /clientes/{id}} o de vuelta al formulario si hay errores
     */
    @Operation(
        summary = "Crear nuevo cliente",
        description = "Valida y almacena un nuevo cliente empresarial en la base de datos MySQL, calculando su Health Score inicial."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Cliente creado exitosamente, redirige a la vista 360°"),
        @ApiResponse(responseCode = "200", description = "Errores de validación en el formulario")
    })
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

    /**
     * Muestra la Vista 360° del cliente seleccionado con tabs navegables:
     * resumen, tickets, encuestas CSAT, renovaciones e interacciones.
     *
     * @param id    identificador del cliente
     * @param tab   tab activo a mostrar (por defecto {@code "resumen"})
     * @param model modelo Thymeleaf
     * @return vista {@code clientes/cliente360} con KPIs, factores de salud y colecciones relacionadas
     * @throws jakarta.persistence.EntityNotFoundException si el cliente no existe
     */
    @Operation(
        summary = "Vista 360° del cliente",
        description = "Carga la ficha integral del cliente: KPIs en tiempo real (ARR, Health Score, NPS/CSAT), factores de riesgo ponderados, tickets históricos, encuestas y contratos de renovación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vista 360° cargada con éxito"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado para el ID proporcionado")
    })
    @GetMapping("/{id}")
    public String verCliente360(@Parameter(description = "ID único del cliente", example = "1")
                                @PathVariable("id") Long id,
                                @Parameter(description = "Pestaña activa a visualizar (resumen, tickets, contratos, contactos, encuestas)", example = "resumen")
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

    /**
     * Agrega un nuevo contacto al cliente indicado.
     * El contacto se crea con los datos del modelo {@link com.serviclient.crm.entity.Contacto}.
     *
     * @param clienteId          identificador del cliente destino
     * @param contacto           datos del contacto a agregar
     * @param redirectAttributes atributos flash para el mensaje de resultado
     * @return redirección a {@code /clientes/{id}?tab=contactos}
     */
    @Operation(
        summary = "Agregar contacto al cliente",
        description = "Registra una persona de contacto clave (tomador de decisiones, técnico, etc.) asociada a la ficha del cliente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Contacto registrado exitosamente, redirige a la pestaña de contactos"),
        @ApiResponse(responseCode = "400", description = "Error al procesar los datos del contacto")
    })
    @PostMapping("/{id}/contactos")
    public String agregarContacto(@Parameter(description = "ID del cliente", example = "1")
                                  @PathVariable("id") Long clienteId,
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
