package com.serviclient.crm.repository;

import com.serviclient.crm.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<RolEntity, Long> {
    List<RolEntity> findByEmpresaId(Long empresaId);
    Optional<RolEntity> findByEmpresaIdAndNombre(Long empresaId, String nombre);
}
