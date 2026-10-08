package com.serviclient.crm.service;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import com.serviclient.crm.dao.UsuarioDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return usuarioDAO.findById(userDetails.getId());
        }
        return usuarioDAO.findByEmail(auth.getName());
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerAgentesYAdminsPorEmpresa(Long empresaId) {
        return usuarioDAO.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosPorEmpresa(Long empresaId) {
        return usuarioDAO.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
    }
}
