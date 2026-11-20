package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Notificacion;
import com.serviclient.crm.entity.enums.TipoNotificacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
  List<Notificacion> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

  List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByCreatedAtDesc(Long usuarioId);

  long countByUsuarioIdAndLeidaFalse(Long usuarioId);

  Optional<Notificacion> findByIdAndUsuarioId(Long id, Long usuarioId);

  @Query(
      """
      SELECT n FROM Notificacion n
      WHERE n.usuario.id = :usuarioId
        AND n.resuelta = false
        AND (
            n.leida = false
            OR n.tipo IN :tiposActivos
        )
      ORDER BY n.createdAt DESC
      """)
  List<Notificacion> listarVisibles(
      @Param("usuarioId") Long usuarioId,
      @Param("tiposActivos") List<TipoNotificacion> tiposActivos,
      Pageable pageable);

  @Query(
      """
      SELECT COUNT(n) FROM Notificacion n
      WHERE n.usuario.id = :usuarioId
        AND n.resuelta = false
        AND (
            n.leida = false
            OR n.tipo IN :tiposActivos
        )
      """)
  long contarVisibles(
      @Param("usuarioId") Long usuarioId,
      @Param("tiposActivos") List<TipoNotificacion> tiposActivos);

  List<Notificacion> findByEmpresaIdAndTipoAndEntidadTipoAndEntidadIdAndResueltaFalse(
      Long empresaId, TipoNotificacion tipo, String entidadTipo, Long entidadId);
}
