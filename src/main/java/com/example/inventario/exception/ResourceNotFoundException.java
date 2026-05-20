package com.example.inventario.exception;

/**
 * Excepción personalizada para cuando se busca un ID que no existe.
 * Lanzarla produce un HTTP 404 limpio en vez de un NullPointerException (500).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " con ID " + id + " no fue encontrado en el sistema.");
    }
}
