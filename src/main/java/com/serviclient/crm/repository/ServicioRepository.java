package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Servicio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

  List<Servicio> findByEmpresaId(Long empresaId);
  Optional<Servicio> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
  Optional<Servicio> findByCodigo(String codigo);
  // Listar servicios ordenados por nombre
  List<Servicio> findByEmpresaIdOrderByNombreAsc(Long empresaId);
  // Buscar un servicio dentro de su propia empresa
  Optional<Servicio> findByIdAndEmpresaId(Long id, Long empresaId);
  // Validar códigos duplicados
  Optional<Servicio> findByEmpresaIdAndCodigoIgnoreCase(Long empresaId, String codigo);
  // Listar servicios disponibles para contratar
  List<Servicio> findByEmpresaIdAndEstadoOrderByNombreAsc(Long empresaId, String estado);
}
