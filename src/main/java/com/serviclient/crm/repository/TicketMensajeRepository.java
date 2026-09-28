package com.serviclient.crm.repository;

import com.serviclient.crm.entity.TicketMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMensajeRepository extends JpaRepository<TicketMensaje, Long> {
    List<TicketMensaje> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    @Query("SELECT m FROM TicketMensaje m WHERE m.ticket.id = :ticketId ORDER BY m.createdAt ASC")
    List<TicketMensaje> findByTicketIdOrderByFechaCreacionAsc(@Param("ticketId") Long ticketId);
}
