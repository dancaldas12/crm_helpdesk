package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.enums.EstadoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByEmpresaId(Long empresaId);

    long countByEmpresaId(Long empresaId);

    long countByEmpresaIdAndEstado(Long empresaId, EstadoCliente estado);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND " +
           "(:busqueda IS NULL OR :busqueda = '' OR " +
           "LOWER(c.nombreComercial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.servicioContratado) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.responsable.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.responsable.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND " +
           "(:estado IS NULL OR c.estado = :estado)")
    Page<Cliente> buscarConFiltros(@Param("empresaId") Long empresaId,
                                   @Param("busqueda") String busqueda,
                                   @Param("estado") EstadoCliente estado,
                                   Pageable pageable);
}
