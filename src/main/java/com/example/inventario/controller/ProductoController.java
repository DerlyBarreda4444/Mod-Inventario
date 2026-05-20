package com.example.inventario.controller;

import com.example.inventario.exception.ResourceNotFoundException;
import com.example.inventario.model.Categoria;
import com.example.inventario.model.Producto;
import com.example.inventario.repository.CategoriaRepository;
import com.example.inventario.repository.ProductoRepository;
import com.example.inventario.service.CsvExportService;
import com.example.inventario.service.LogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Permite fetch desde cualquier HTML sin bloqueos CORS
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private CsvExportService csvExportService;

    // ══════════════════════════════════════════════════════════════════════════
    // GET /api/productos — Lista todos (o filtra por sucursal)
    // Ejemplo: /api/productos?sucursal=Norte
    // ══════════════════════════════════════════════════════════════════════════
    @GetMapping
    public ResponseEntity<List<Producto>> listar(
            @RequestParam(required = false) String sucursal) {

        List<Producto> productos;
        if (sucursal != null && !sucursal.isBlank()) {
            productos = productoRepository.findBySucursalIgnoreCase(sucursal);
        } else {
            productos = productoRepository.findAll();
        }
        return ResponseEntity.ok(productos);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /api/productos/alertas — Productos con stock <= stockMinimo
    // Ejemplo: /api/productos/alertas?sucursal=Sur
    // ══════════════════════════════════════════════════════════════════════════
    @GetMapping("/alertas")
    public ResponseEntity<List<Producto>> obtenerAlertas(
            @RequestParam(required = false) String sucursal) {

        List<Producto> alertas;
        if (sucursal != null && !sucursal.isBlank()) {
            alertas = productoRepository.findAlertasBySucursal(sucursal);
        } else {
            alertas = productoRepository.findProductosEnAlerta();
        }
        return ResponseEntity.ok(alertas);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /api/productos/{id} — Obtiene un producto por ID
    // ══════════════════════════════════════════════════════════════════════════
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        return ResponseEntity.ok(producto);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // POST /api/productos — Crea un nuevo producto
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {

        // Verificamos que la categoría referenciada exista en la BD
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new IllegalArgumentException("Debe especificar una categoría válida");
        }
        Categoria categoria = categoriaRepository.findById(producto.getCategoria().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría",
                        producto.getCategoria().getId()));
        producto.setCategoria(categoria);

        Producto guardado = productoRepository.save(producto);

        // Registro de auditoría en logs.txt
        logService.registrar("CREAR",
                "Producto '" + guardado.getNombre() +
                "' (SKU: " + guardado.getSku() + ") creado en sucursal " + guardado.getSucursal());

        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PUT /api/productos/{id} — Actualiza un producto (entradas/salidas de stock)
    // ══════════════════════════════════════════════════════════════════════════
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Producto datosNuevos) {

        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));

        // Guardamos el stock anterior para el log de auditoría
        int stockAnterior = existente.getStock();

        // Actualizamos todos los campos editables
        existente.setSku(datosNuevos.getSku());
        existente.setNombre(datosNuevos.getNombre());
        existente.setPrecio(datosNuevos.getPrecio());
        existente.setStock(datosNuevos.getStock());
        existente.setStockMinimo(datosNuevos.getStockMinimo());
        existente.setSucursal(datosNuevos.getSucursal());

        // Actualizamos la categoría si cambió
        if (datosNuevos.getCategoria() != null && datosNuevos.getCategoria().getId() != null) {
            Categoria nuevaCategoria = categoriaRepository
                    .findById(datosNuevos.getCategoria().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría",
                            datosNuevos.getCategoria().getId()));
            existente.setCategoria(nuevaCategoria);
        }

        Producto actualizado = productoRepository.save(existente);

        // Log con cambio de stock visible
        String cambioStock = (actualizado.getStock() > stockAnterior)
                ? "entrada de " + (actualizado.getStock() - stockAnterior) + " unidades"
                : "salida de " + (stockAnterior - actualizado.getStock()) + " unidades";

        logService.registrar("ACTUALIZAR",
                "Producto '" + actualizado.getNombre() +
                "' actualizado. Stock: " + stockAnterior + " → " +
                actualizado.getStock() + " (" + cambioStock + ")");

        return ResponseEntity.ok(actualizado);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DELETE /api/productos/{id} — Elimina un producto
    // ══════════════════════════════════════════════════════════════════════════
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));

        String nombreProducto = producto.getNombre();
        productoRepository.deleteById(id);

        // Auditoría del borrado
        logService.registrar("ELIMINAR",
                "Producto '" + nombreProducto + "' (ID: " + id + ") eliminado del sistema");

        return ResponseEntity.noContent().build(); // HTTP 204
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /api/productos/exportar/csv — Descarga CSV con filtros dinámicos
    // Ejemplos:
    //   /api/productos/exportar/csv                        → Todo el inventario
    //   /api/productos/exportar/csv?sucursal=Norte         → Solo sucursal Norte
    //   /api/productos/exportar/csv?soloAlertas=true       → Solo alertas de stock
    //   /api/productos/exportar/csv?sucursal=Sur&soloAlertas=true → Alertas de Sur
    // ══════════════════════════════════════════════════════════════════════════
    @GetMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) String sucursal,
            @RequestParam(defaultValue = "false") boolean soloAlertas) throws IOException {

        byte[] csvBytes = csvExportService.generarCsv(sucursal, soloAlertas);

        // Nombre del archivo dinámico según el filtro aplicado
        String nombreArchivo = "inventario";
        if (sucursal != null && !sucursal.isBlank()) {
            nombreArchivo += "_" + sucursal.replaceAll("\\s+", "_");
        }
        if (soloAlertas) {
            nombreArchivo += "_alertas";
        }
        nombreArchivo += ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        // Content-Disposition: attachment → fuerza la descarga en el navegador
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        headers.setContentLength(csvBytes.length);

        // Log de la exportación
        logService.registrar("EXPORTAR",
                "CSV exportado: " + nombreArchivo + " (" + csvBytes.length + " bytes)");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
}
