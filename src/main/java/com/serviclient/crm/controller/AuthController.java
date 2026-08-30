package com.serviclient.crm.controller;

import com.serviclient.crm.dto.RegistroEmpresaDto;
import com.serviclient.crm.service.EmpresaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes("registroForm")
public class AuthController {

    private final EmpresaService empresaService;

    @ModelAttribute("registroForm")
    public RegistroEmpresaDto initRegistroForm() {
        return new RegistroEmpresaDto();
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/clientes";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Credenciales incorrectas. Verifique su correo y contraseña.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Ha cerrado sesión correctamente.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "¡Empresa y administrador registrados exitosamente! Inicie sesión a continuación.");
        }
        return "auth/login";
    }

    // Paso 1: Datos de Empresa
    @GetMapping("/registro/paso-1")
    public String registroPaso1(@ModelAttribute("registroForm") RegistroEmpresaDto form, Model model) {
        model.addAttribute("pasoActual", 1);
        return "auth/registro-paso1";
    }

    @PostMapping("/registro/paso-1")
    public String procesarPaso1(@ModelAttribute("registroForm") RegistroEmpresaDto form,
                                BindingResult bindingResult,
                                Model model) {
        if (form.getNombreComercial() == null || form.getNombreComercial().isBlank()) {
            bindingResult.rejectValue("nombreComercial", "error.nombreComercial", "El nombre comercial es obligatorio");
        }
        if (form.getRazonSocial() == null || form.getRazonSocial().isBlank()) {
            bindingResult.rejectValue("razonSocial", "error.razonSocial", "La razón social es obligatoria");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pasoActual", 1);
            return "auth/registro-paso1";
        }

        return "redirect:/registro/paso-2";
    }

    // Paso 2: Usuario Administrador
    @GetMapping("/registro/paso-2")
    public String registroPaso2(@ModelAttribute("registroForm") RegistroEmpresaDto form, Model model) {
        model.addAttribute("pasoActual", 2);
        return "auth/registro-paso2";
    }

    @PostMapping("/registro/paso-2")
    public String procesarPaso2(@ModelAttribute("registroForm") RegistroEmpresaDto form,
                                BindingResult bindingResult,
                                Model model) {
        if (form.getAdminNombre() == null || form.getAdminNombre().isBlank()) {
            bindingResult.rejectValue("adminNombre", "error.adminNombre", "El nombre es obligatorio");
        }
        if (form.getAdminApellido() == null || form.getAdminApellido().isBlank()) {
            bindingResult.rejectValue("adminApellido", "error.adminApellido", "El apellido es obligatorio");
        }
        if (form.getAdminEmail() == null || form.getAdminEmail().isBlank() || !form.getAdminEmail().contains("@")) {
            bindingResult.rejectValue("adminEmail", "error.adminEmail", "Correo electrónico válido obligatorio");
        }
        if (form.getAdminPassword() == null || form.getAdminPassword().length() < 6) {
            bindingResult.rejectValue("adminPassword", "error.adminPassword", "La contraseña debe tener al menos 6 caracteres");
        }
        if (form.getAdminPasswordConfirm() != null && !form.getAdminPasswordConfirm().equals(form.getAdminPassword())) {
            bindingResult.rejectValue("adminPasswordConfirm", "error.adminPasswordConfirm", "Las contraseñas no coinciden");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pasoActual", 2);
            return "auth/registro-paso2";
        }

        return "redirect:/registro/paso-3";
    }

    // Paso 3: Configuración Inicial SLA / CSAT / Health Score
    @GetMapping("/registro/paso-3")
    public String registroPaso3(@ModelAttribute("registroForm") RegistroEmpresaDto form, Model model) {
        model.addAttribute("pasoActual", 3);
        return "auth/registro-paso3";
    }

    @PostMapping("/registro/paso-3")
    public String procesarPaso3(@ModelAttribute("registroForm") RegistroEmpresaDto form,
                                BindingResult bindingResult,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            empresaService.registrarEmpresaCompleta(form);
            session.removeAttribute("registroForm");
            redirectAttributes.addFlashAttribute("successMessage", "¡Empresa registrada con éxito! Ya puedes iniciar sesión con tu cuenta de administrador.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pasoActual", 3);
            return "auth/registro-paso3";
        } catch (Exception ex) {
            log.error("Error al registrar empresa", ex);
            model.addAttribute("errorMessage", "Ocurrió un error inesperado durante el registro: " + ex.getMessage());
            model.addAttribute("pasoActual", 3);
            return "auth/registro-paso3";
        }
    }
}
