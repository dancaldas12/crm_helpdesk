package com.serviclient.crm.repository;

import com.serviclient.crm.entity.TicketHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistorialRepository extends JpaRepository<TicketHistorial, Long> {
    List<TicketHistorial> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    @Query("SELECT h FROM TicketHistorial h WHERE h.ticket.id = :ticketId ORDER BY h.createdAt DESC")
    List<TicketHistorial> findByTicketIdOrderByFechaCreacionDesc(@Param("ticketId") Long ticketId);
}
