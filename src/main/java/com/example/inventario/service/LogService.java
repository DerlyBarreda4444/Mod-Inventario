package com.example.inventario.service;

import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TRAZABILIDAD SIN BASE DE DATOS — LogService
 *
 * Registra cada operación crítica (POST, PUT, DELETE) en logs.txt.
 * Al no depender de tablas ni llaves foráneas, NUNCA puede fallar
 * por problemas de integridad referencial.
 *
 * Formato de cada línea:
 * [2026-05-19 14:00:35] [ACCIÓN] Descripción del evento
 */
@Service
public class LogService {

    private static final String LOG_FILE = "logs.txt";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Escribe una línea de log al archivo logs.txt.
     * Si el archivo no existe, lo crea automáticamente.
     * El 'true' en FileWriter activa el modo APPEND (no sobreescribe).
     *
     * @param accion  Tipo de operación: CREAR, ACTUALIZAR, ELIMINAR
     * @param detalle Descripción legible del evento
     */
    public void registrar(String accion, String detalle) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String linea = String.format("[%s] [%s] %s%n", timestamp, accion, detalle);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.print(linea);
        } catch (IOException e) {
            // Si el log falla (ej: disco lleno), el sistema NO debe caerse.
            // Solo imprimimos en consola como respaldo.
            System.err.println("⚠️  No se pudo escribir en logs.txt: " + e.getMessage());
        }
    }
}
