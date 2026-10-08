package com.serviclient.crm.dao;

import com.serviclient.crm.entity.CategoriaTicketEntity;

import java.util.List;
import java.util.Optional;

public interface CategoriaTicketDAO {
    Optional<CategoriaTicketEntity> findById(Long id);
    List<CategoriaTicketEntity> findByEmpresaId(Long empresaId);
    Optional<CategoriaTicketEntity> findByEmpresaIdAndNombre(Long empresaId, String nombre);
    CategoriaTicketEntity save(CategoriaTicketEntity categoria);
    List<CategoriaTicketEntity> findAll();
    void deleteById(Long id);
    void delete(CategoriaTicketEntity categoria);
}
