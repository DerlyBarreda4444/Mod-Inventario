package com.example.inventario.controller;

import com.example.inventario.exception.ResourceNotFoundException;
import com.example.inventario.model.Categoria;
import com.example.inventario.repository.CategoriaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier navegador
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // ── GET /api/categorias — Lista todas las categorías ──────────────────────
    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        return ResponseEntity.ok(categoriaRepository.findAll());
    }

    // ── GET /api/categorias/{id} — Obtiene una categoría por ID ──────────────
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
        return ResponseEntity.ok(categoria);
    }

    // ── POST /api/categorias — Crea una nueva categoría ──────────────────────
    @PostMapping
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria categoria) {
        Categoria guardada = categoriaRepository.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // ── PUT /api/categorias/{id} — Actualiza una categoría existente ──────────
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Categoria datosNuevos) {

        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));

        existente.setNombre(datosNuevos.getNombre());
        return ResponseEntity.ok(categoriaRepository.save(existente));
    }

    // ── DELETE /api/categorias/{id} — Elimina una categoría ──────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría", id);
        }
        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // HTTP 204
    }
}
