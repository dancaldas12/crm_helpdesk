package com.serviclient.crm.repository;

import com.serviclient.crm.entity.EncuestaCsat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncuestaCsatRepository extends JpaRepository<EncuestaCsat, Long> {
    List<EncuestaCsat> findByEmpresaId(Long empresaId);
    List<EncuestaCsat> findByClienteId(Long clienteId);
    Optional<EncuestaCsat> findByTicketId(Long ticketId);

    @Query("SELECT AVG(r.puntuacion) FROM RespuestaCsat r WHERE r.encuesta.cliente.id = :clienteId")
    Double findAverageScoreByClienteId(Long clienteId);

    @Query("SELECT AVG(r.puntuacion) FROM RespuestaCsat r WHERE r.encuesta.empresa.id = :empresaId")
    Double findAverageScoreByEmpresaId(Long empresaId);
}
