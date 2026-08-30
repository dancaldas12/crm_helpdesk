package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByEmpresaId(Long empresaId);
    List<Usuario> findByEmpresaIdAndRol(Long empresaId, Rol rol);
    List<Usuario> findByRol(Rol rol);
}
