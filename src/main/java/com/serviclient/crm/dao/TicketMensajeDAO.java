package com.serviclient.crm.dao;

import com.serviclient.crm.entity.TicketMensaje;

import java.util.List;
import java.util.Optional;

public interface TicketMensajeDAO {
    Optional<TicketMensaje> findById(Long id);
    List<TicketMensaje> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    List<TicketMensaje> findByTicketIdOrderByFechaCreacionAsc(Long ticketId);
    TicketMensaje save(TicketMensaje mensaje);
    List<TicketMensaje> findAll();
    void deleteById(Long id);
    void delete(TicketMensaje mensaje);
}
