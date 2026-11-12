package com.serviclient.crm.repository;

import com.serviclient.crm.entity.EncuestaCsat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncuestaCsatRepository extends JpaRepository<EncuestaCsat, Long> {

    List<EncuestaCsat> findByEmpresaId(Long empresaId);

    List<EncuestaCsat> findByClienteId(Long clienteId);

    Optional<EncuestaCsat> findByTicketId(Long ticketId);

    @Query("SELECT COALESCE(AVG(r.puntuacion), 0.0) FROM RespuestaCsat r WHERE r.encuesta.cliente.id = :clienteId")
    Double findAverageScoreByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT COALESCE(AVG(r.puntuacion), 0.0) FROM RespuestaCsat r WHERE r.encuesta.empresa.id = :empresaId")
    Double findAverageScoreByEmpresaId(@Param("empresaId") Long empresaId);

    boolean existsByTicketId(Long ticketId);

    @Modifying
    @Query("UPDATE EncuestaCsat e SET e.activa = false WHERE e.ticket.id = :ticketId")
    void desactivarEncuestasAnteriores(@Param("ticketId") Long ticketId);

    @Query("SELECT e FROM EncuestaCsat e JOIN FETCH e.ticket WHERE e.contacto.id = :contactoId AND e.estado = 'PENDIENTE' AND e.activa = true")
    List<EncuestaCsat> findPendientesActivasPorContacto(@Param("contactoId") Long contactoId);
}
