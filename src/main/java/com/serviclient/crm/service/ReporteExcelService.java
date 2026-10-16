package com.serviclient.crm.service;

import com.serviclient.crm.entity.Cliente;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReporteExcelService {

  private final ClienteService clienteService;

  public byte[] generarReporteClientes(Long empresaId) throws IOException {

    List<Cliente> clientes = clienteService.listarTodosPorEmpresa(empresaId);

    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.createSheet("Clientes");

      // Estilo para encabezados
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);

      CellStyle headerStyle = workbook.createCellStyle();
      headerStyle.setFont(headerFont);

      // Encabezados
      Row header = sheet.createRow(0);

      String[] columnas = {
        "Cliente", "RUC", "Industria", "Pais", "Ciudad", "Health Score", "Estado de Salud"
      };

      for (int i = 0; i < columnas.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(columnas[i]);
        cell.setCellStyle(headerStyle);
      }

      // Datos
      int fila = 1;

      for (Cliente cliente : clientes) {

        Row row = sheet.createRow(fila++);

        row.createCell(0).setCellValue(valorSeguro(cliente.getNombreComercial()));

        row.createCell(1).setCellValue(valorSeguro(cliente.getRuc()));

        row.createCell(2).setCellValue(valorSeguro(cliente.getIndustria()));

        row.createCell(3).setCellValue(valorSeguro(cliente.getPais()));

        row.createCell(4).setCellValue(valorSeguro(cliente.getCiudad()));

        row.createCell(5).setCellValue(cliente.getHealthScore());

        row.createCell(6).setCellValue(cliente.getEstadoSalud().getEtiqueta());
      }

      // Ajustar ancho automáticamente
      for (int i = 0; i < columnas.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(outputStream);

      return outputStream.toByteArray();
    }
  }

  private String valorSeguro(String valor) {
    return valor != null ? valor : "";
  }
}
