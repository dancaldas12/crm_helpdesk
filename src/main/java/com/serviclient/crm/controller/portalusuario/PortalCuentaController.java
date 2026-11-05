package com.serviclient.crm.controller.portalusuario;

import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.repository.CuentaPortalRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/portal")
@RequiredArgsConstructor
public class PortalCuentaController {

    private final CuentaPortalRepository cuentaPortalRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/cambiar-password-modal")
    public String cambiarPasswordModal(
            @RequestParam("passwordActual") String passwordActual,
            @RequestParam("nuevaPassword") String nuevaPassword,
            @RequestParam("confirmarPassword") String confirmarPassword,
            HttpServletRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        CuentaPortal cuenta = cuentaPortalRepository.findByEmail(principal.getName())
                .filter(CuentaPortal::estaActiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        // Determina la página previa para volver exactamente al mismo lugar
        String referer = request.getHeader("Referer");
        String destino = (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/portal/mis-tickets";

        if (StringUtils.isAnyBlank(passwordActual, nuevaPassword, confirmarPassword)) {
            redirectAttributes.addFlashAttribute("modalPasswordError", "Debes completar todos los campos.");
            redirectAttributes.addFlashAttribute("abrirModalPassword", true);
            return destino;
        }

        if (!passwordEncoder.matches(passwordActual, cuenta.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("modalPasswordError", "La contraseña actual no es correcta.");
            redirectAttributes.addFlashAttribute("abrirModalPassword", true);
            return destino;
        }

        if (!StringUtils.equals(nuevaPassword, confirmarPassword)) {
            redirectAttributes.addFlashAttribute("modalPasswordError", "Las nuevas contraseñas no coinciden.");
            redirectAttributes.addFlashAttribute("abrirModalPassword", true);
            return destino;
        }

        if (StringUtils.length(nuevaPassword) < 8 || StringUtils.length(nuevaPassword) > 72) {
            redirectAttributes.addFlashAttribute("modalPasswordError", "La nueva contraseña debe tener entre 8 y 72 caracteres.");
            redirectAttributes.addFlashAttribute("abrirModalPassword", true);
            return destino;
        }

        if (passwordEncoder.matches(nuevaPassword, cuenta.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("modalPasswordError", "La nueva contraseña no puede ser igual a la anterior.");
            redirectAttributes.addFlashAttribute("abrirModalPassword", true);
            return destino;
        }

        cuenta.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        cuenta.setRequiereCambioPassword(false);
        cuentaPortalRepository.save(cuenta);

        redirectAttributes.addFlashAttribute("modalPasswordExito", "¡Contraseña actualizada con éxito!");
        redirectAttributes.addFlashAttribute("abrirModalPassword", true);
        return destino;
    }
}