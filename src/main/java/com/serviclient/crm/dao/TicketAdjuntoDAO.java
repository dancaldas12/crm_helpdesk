package com.serviclient.crm.dao;

import com.serviclient.crm.entity.TicketAdjunto;

import java.util.List;
import java.util.Optional;

public interface TicketAdjuntoDAO {
    Optional<TicketAdjunto> findById(Long id);
    List<TicketAdjunto> findByTicketId(Long ticketId);
    TicketAdjunto save(TicketAdjunto adjunto);
    List<TicketAdjunto> findAll();
    void deleteById(Long id);
    void delete(TicketAdjunto adjunto);
}
