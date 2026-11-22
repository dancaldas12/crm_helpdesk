package com.serviclient.crm.service;

import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.Servicio;
import com.serviclient.crm.repository.ServicioRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioService {

  private final ServicioRepository servicioRepository;
  private final EmpresaService empresaService;

  @Transactional(readOnly = true)
  public List<Servicio> listar(Long empresaId) {
    return servicioRepository.findByEmpresaIdOrderByNombreAsc(empresaId);
  }

  @Transactional(readOnly = true)
  public List<Servicio> listarActivos(Long empresaId) {
    return servicioRepository.findByEmpresaIdAndEstadoOrderByNombreAsc(empresaId, "ACTIVO");
  }

  @Transactional(readOnly = true)
  public Servicio obtenerPorId(Long id, Long empresaId) {
    return servicioRepository
        .findByIdAndEmpresaId(id, empresaId)
        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado."));
  }

  @Transactional
  public Servicio crear(Long empresaId, String codigo, String nombre, String descripcion) {

    codigo = normalizarCodigo(codigo);
    nombre = validarNombre(nombre);

    validarCodigoDuplicado(empresaId, codigo, null);

    Empresa empresa = empresaService.obtenerPorId(empresaId);

    Servicio servicio =
        Servicio.builder()
            .empresa(empresa)
            .codigo(codigo)
            .nombre(nombre)
            .descripcion(descripcion)
            .estado("ACTIVO")
            .build();

    return servicioRepository.save(servicio);
  }

  @Transactional
  public Servicio actualizar(
      Long id, Long empresaId, String codigo, String nombre, String descripcion) {

    Servicio servicio = obtenerPorId(id, empresaId);

    codigo = normalizarCodigo(codigo);
    nombre = validarNombre(nombre);

    validarCodigoDuplicado(empresaId, codigo, id);

    servicio.setCodigo(codigo);
    servicio.setNombre(nombre);
    servicio.setDescripcion(descripcion);

    return servicioRepository.save(servicio);
  }

  @Transactional
  public Servicio cambiarEstado(Long id, Long empresaId, boolean activo) {

    Servicio servicio = obtenerPorId(id, empresaId);

    servicio.setEstado(activo ? "ACTIVO" : "INACTIVO");

    return servicioRepository.save(servicio);
  }

  private void validarCodigoDuplicado(Long empresaId, String codigo, Long idActual) {

    servicioRepository
        .findByEmpresaIdAndCodigoIgnoreCase(empresaId, codigo)
        .ifPresent(servicio -> {
          if (!Objects.equals(servicio.getId(), idActual)) {
            throw new IllegalArgumentException("Ya existe un servicio con ese código.");
          }
        });
  }

  private String normalizarCodigo(String codigo) {

    if (codigo == null || codigo.isBlank()) {
      throw new IllegalArgumentException("El código del servicio es obligatorio.");
    }

    String resultado = codigo.strip().toUpperCase(Locale.ROOT);

    if (resultado.length() > 50) {
      throw new IllegalArgumentException("El código no puede superar los 50 caracteres.");
    }

    return resultado;
  }

  private String validarNombre(String nombre) {

    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre del servicio es obligatorio.");
    }

    String resultado = nombre.strip();

    if (resultado.length() > 150) {
      throw new IllegalArgumentException("El nombre no puede superar los 150 caracteres.");
    }

    return resultado;
  }
}
