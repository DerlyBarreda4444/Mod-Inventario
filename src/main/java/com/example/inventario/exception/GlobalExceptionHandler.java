package com.example.inventario.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * ESCUDO ANTI-BUGS — GlobalExceptionHandler
 *
 * Intercepta TODAS las excepciones antes de que se conviertan en Error 500.
 * Devuelve siempre un JSON limpio con HTTP 400 o 409, nunca un stack trace.
 * Esto frustra a los testers que intenten romper el sistema con Postman.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 1. Validaciones de @Valid (@NotBlank, @Min, @Positive, etc.) ──────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errores = new HashMap<>();

        // Extrae TODOS los errores de campos y los mapea: campo → mensaje
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        response.put("status", 400);
        response.put("error", "Datos de entrada inválidos");
        response.put("detalles", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ── 2. Violación de restricción única (SKU o Nombre duplicado) ────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEntry(
            DataIntegrityViolationException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 409);
        response.put("error", "Conflicto de datos");
        response.put("mensaje", "Ya existe un registro con ese SKU o nombre. Use un valor único.");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ── 3. JSON malformado (ej: enviar texto donde va un número) ─────────────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(
            HttpMessageNotReadableException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "JSON inválido");
        response.put("mensaje", "El cuerpo de la petición contiene datos con formato incorrecto. " +
                "Verifique que los tipos de datos sean correctos (números, texto, etc.).");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ── 4. Recurso no encontrado (ID inexistente) ─────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 404);
        response.put("error", "Recurso no encontrado");
        response.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ── 5. Cualquier otra excepción no controlada (última línea de defensa) ───
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("error", "Error interno del servidor");
        response.put("mensaje", "Ocurrió un error inesperado. Contacte al administrador.");

        // El stack trace va solo a los logs del servidor, NUNCA al cliente
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
