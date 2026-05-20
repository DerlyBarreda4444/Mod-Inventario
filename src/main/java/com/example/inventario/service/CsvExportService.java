package com.example.inventario.service;

import com.example.inventario.model.Producto;
import com.example.inventario.repository.ProductoRepository;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * EXPORTACIÓN BLINDADA — CsvExportService
 *
 * Genera archivos CSV dinámicos usando OpenCSV.
 * Ventaja clave: OpenCSV envuelve automáticamente en comillas cualquier
 * campo que contenga comas, saltos de línea o caracteres especiales.
 * Esto elimina el "Efecto Coma" y protege contra CSV Injection.
 */
@Service
public class CsvExportService {

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Genera el CSV en memoria como array de bytes.
     * Los parámetros son opcionales — si son null, exporta todo.
     *
     * @param sucursal  Filtra por sucursal (opcional)
     * @param soloAlertas  Si es true, exporta solo productos con stock bajo
     * @return Bytes del archivo CSV listo para descargar
     */
    public byte[] generarCsv(String sucursal, boolean soloAlertas) throws IOException {

        // ── 1. Obtener los datos según los filtros ────────────────────────────
        List<Producto> productos;

        if (soloAlertas && sucursal != null && !sucursal.isBlank()) {
            productos = productoRepository.findAlertasBySucursal(sucursal);
        } else if (soloAlertas) {
            productos = productoRepository.findProductosEnAlerta();
        } else if (sucursal != null && !sucursal.isBlank()) {
            productos = productoRepository.findBySucursalIgnoreCase(sucursal);
        } else {
            productos = productoRepository.findAll();
        }

        // ── 2. Escribir el CSV en memoria (ByteArrayOutputStream = no crea archivo en disco) ──
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // BOM UTF-8: hace que Excel abra el archivo sin problemas de tildes/ñ
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        try (CSVWriter csvWriter = new CSVWriter(
                writer,
                CSVWriter.DEFAULT_SEPARATOR,       // Coma como separador
                CSVWriter.DEFAULT_QUOTE_CHARACTER,  // Comillas dobles alrededor de campos con comas
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {

            // ── 3. Escribir encabezados ───────────────────────────────────────
            String[] encabezados = {
                "ID", "SKU", "Nombre del Producto", "Categoría",
                "Precio (S/.)", "Stock Actual", "Stock Mínimo",
                "Sucursal", "Estado"
            };
            csvWriter.writeNext(encabezados);

            // ── 4. Escribir una fila por producto ─────────────────────────────
            for (Producto p : productos) {
                String estado = (p.getStock() <= p.getStockMinimo()) ? "⚠ ALERTA" : "✓ OK";
                String[] fila = {
                    String.valueOf(p.getId()),
                    p.getSku(),
                    p.getNombre(),
                    p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                    String.format("%.2f", p.getPrecio()),
                    String.valueOf(p.getStock()),
                    String.valueOf(p.getStockMinimo()),
                    p.getSucursal(),
                    estado
                };
                csvWriter.writeNext(fila);
            }
        }

        return baos.toByteArray();
    }
}
