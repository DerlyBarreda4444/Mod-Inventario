package com.example.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    @Column(unique = true, nullable = false, length = 100)
    private String nombre;

    // ── Constructores ──────────────────────────────────────────────────────────
    public Categoria() {}

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
