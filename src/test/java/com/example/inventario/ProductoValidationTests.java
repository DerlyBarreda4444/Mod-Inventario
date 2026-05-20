package com.example.inventario;

import com.example.inventario.model.Categoria;
import com.example.inventario.model.Producto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PRUEBAS DE VALIDACIÓN — ProductoValidationTests
 *
 * Estas pruebas NO requieren base de datos ni servidor levantado.
 * Verifican que las anotaciones de validación del modelo funcionen correctamente.
 * Ejecutar con: mvn test
 */
class ProductoValidationTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ── Utilidad: crea un producto válido como base para los tests ────────────
    private Producto productoValido() {
        Producto p = new Producto();
        p.setSku("LAPIZ-HB-001");
        p.setNombre("Lápiz HB Staedtler");
        p.setPrecio(1.50);
        p.setStock(100);
        p.setStockMinimo(10);
        p.setSucursal("Norte");

        Categoria cat = new Categoria("Escritura");
        cat.setId(1L);
        p.setCategoria(cat);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTS DE SKU
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Producto con todos los campos válidos no debe tener errores")
    void productoValido_noDebeGenerarViolaciones() {
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(productoValido());
        assertTrue(violaciones.isEmpty(), "No debe haber errores de validación");
    }

    @Test
    @DisplayName("❌ SKU vacío debe ser rechazado")
    void skuVacio_debeGenerarViolacion() {
        Producto p = productoValido();
        p.setSku("");
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertFalse(violaciones.isEmpty());
        assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("sku")));
    }

    @Test
    @DisplayName("❌ SKU con solo espacios debe ser rechazado")
    void skuSoloEspacios_debeGenerarViolacion() {
        Producto p = productoValido();
        p.setSku("   ");
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertFalse(violaciones.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTS DE PRECIO
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("❌ Precio negativo debe ser rechazado")
    void precioNegativo_debeGenerarViolacion() {
        Producto p = productoValido();
        p.setPrecio(-5.00);
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertFalse(violaciones.isEmpty());
        assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("precio")));
    }

    @Test
    @DisplayName("❌ Precio cero debe ser rechazado (debe ser POSITIVO)")
    void precioCero_debeGenerarViolacion() {
        Producto p = productoValido();
        p.setPrecio(0.0);
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertFalse(violaciones.isEmpty());
    }

    @Test
    @DisplayName("❌ Precio null debe ser rechazado")
    void precioNull_debeGenerarViolacion() {
        Producto p = productoValido();
        p.setPrecio(null);
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertFalse(violaciones.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTS DE STOCK
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("❌ Stock negativo debe ser rechazado")
    void stockNegativo_debeGenerarViolacion() {
        Producto p = productoValido();
        p.setStock(-1);
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertFalse(violaciones.isEmpty());
        assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("stock")));
    }

    @Test
    @DisplayName("✅ Stock igual a cero debe ser aceptado (puede estar agotado)")
    void stockCero_debeSerValido() {
        Producto p = productoValido();
        p.setStock(0);
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertTrue(violaciones.isEmpty(), "Stock 0 debe ser válido (producto agotado)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTS DE NOMBRE
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("❌ Nombre vacío debe ser rechazado")
    void nombreVacio_debeGenerarViolacion() {
        Producto p = productoValido();
        p.setNombre("");
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertFalse(violaciones.isEmpty());
    }

    @Test
    @DisplayName("❌ Sucursal vacía debe ser rechazada")
    void sucursalVacia_debeGenerarViolacion() {
        Producto p = productoValido();
        p.setSucursal("");
        Set<ConstraintViolation<Producto>> violaciones = validator.validate(p);
        assertFalse(violaciones.isEmpty());
    }
}
