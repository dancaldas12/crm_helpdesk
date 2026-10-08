package com.serviclient.crm.dao;

import com.serviclient.crm.entity.TicketHistorial;

import java.util.List;
import java.util.Optional;

public interface TicketHistorialDAO {
    Optional<TicketHistorial> findById(Long id);
    List<TicketHistorial> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    List<TicketHistorial> findByTicketIdOrderByFechaCreacionDesc(Long ticketId);
    TicketHistorial save(TicketHistorial historial);
    List<TicketHistorial> findAll();
    void deleteById(Long id);
    void delete(TicketHistorial historial);
}
