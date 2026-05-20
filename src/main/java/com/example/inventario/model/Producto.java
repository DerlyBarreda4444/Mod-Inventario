package com.example.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SKU único — bloquea duplicados a nivel de BD y de validación
    @NotBlank(message = "El SKU no puede estar vacío")
    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Column(nullable = false, length = 150)
    private String nombre;

    // @Positive rechaza cualquier precio <= 0
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private Double precio;

    // @Min(0) rechaza stock negativo
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    // Sucursal enviada desde un menú cerrado en el frontend → valor siempre válido
    @NotBlank(message = "La sucursal no puede estar vacía")
    @Column(nullable = false, length = 80)
    private String sucursal;

    // Relación N:1 con Categoria
    // @JsonIgnoreProperties evita el bucle infinito JSON (Categoria → Productos → Categoria…)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @NotNull(message = "La categoría es obligatoria")
    private Categoria categoria;

    // ── Constructores ──────────────────────────────────────────────────────────
    public Producto() {}

    // ── Getters y Setters ──────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getSucursal() { return sucursal; }
    public void setSucursal(String sucursal) { this.sucursal = sucursal; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
