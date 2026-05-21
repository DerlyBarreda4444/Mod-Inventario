# 📦 PaperStock — Módulo de Inventarios

> Sistema web de control de stock centralizado con soporte para sucursales distribuidas, alertas de reposición automáticas y exportación dinámica de reportes CSV.

---

## ¿Qué es PaperStock?

PaperStock es el módulo de inventarios desarrollado para el laboratorio **Guerra de los Testers (Sesión 08)**. El sistema permite a una tienda distribuidora de papelería gestionar su inventario desde múltiples sucursales a través de un navegador web, con la base de datos centralizada en la nube.

La filosofía de diseño es **"Minimalismo Blindado"**: cero errores HTTP 500, validación estricta de datos en cada capa y utilidad de negocio real.

---

## 🛠️ Stack Tecnológico

### Backend
- Java 17
- Spring Boot 3.5
- Spring Data JPA + Hibernate
- Hibernate Validator (`@NotBlank`, `@Min`, `@Positive`)
- OpenCSV 5.9 (exportación blindada)
- Maven

### Frontend
- HTML5 + CSS3
- Bootstrap 5
- JavaScript nativo (`fetch()`)

### Base de Datos
- MySQL en Clever Cloud (acceso centralizado desde todas las sucursales)

### Testing
- JUnit 5 (pruebas unitarias sin BD ni servidor)

### Control de Versiones
- Git + GitHub

---

## ✅ Funcionalidades Implementadas

### Gestión de Inventario (CRUD)
- Registrar nuevos productos con validación estricta de todos los campos
- Listar productos con filtrado dinámico por sucursal
- Editar productos y registrar entradas/salidas de stock
- Eliminar productos del sistema

### Gestión de Categorías (CRUD)
- Crear, listar, actualizar y eliminar categorías
- Prevención de duplicados a nivel de modelo y base de datos

### Alertas de Stock
- Detección automática de productos con stock ≤ stock mínimo
- Resaltado visual en la tabla (fila amarilla + etiqueta ⚠ ALERTA)
- Endpoint dedicado para filtrar solo productos en alerta

### Exportación Dinámica a CSV
- Exporta únicamente los datos visibles según el filtro activo en pantalla
- Compatible con Excel sin errores de formato gracias a OpenCSV
- Blindaje automático contra el "Efecto Coma" (comas en nombres de productos)
- Nombre del archivo refleja el filtro aplicado (`inventario_Norte_alertas.csv`)

### Auditoría sin Base de Datos
- Cada operación exitosa (crear, actualizar, eliminar, exportar) queda registrada en `logs.txt`
- Sin dependencia de tablas ni llaves foráneas → nunca puede fallar por integridad referencial

### Mecanismo Anti-Bugs
- `GlobalExceptionHandler`: intercepta todas las excepciones y devuelve JSON con HTTP 400/404/409, nunca un stack trace
- Validaciones en el modelo: bloquean datos inválidos antes de tocar la base de datos
- Menús cerrados en el frontend: la sucursal siempre viene de una lista predefinida

---

## 🗺️ API REST

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/productos` | Lista todos los productos |
| `GET` | `/api/productos?sucursal=Norte` | Filtra por sucursal |
| `GET` | `/api/productos/alertas` | Solo productos con stock bajo |
| `GET` | `/api/productos/{id}` | Un producto por ID |
| `POST` | `/api/productos` | Crear producto |
| `PUT` | `/api/productos/{id}` | Actualizar producto |
| `DELETE` | `/api/productos/{id}` | Eliminar producto |
| `GET` | `/api/productos/exportar/csv` | CSV completo |
| `GET` | `/api/productos/exportar/csv?sucursal=Sur` | CSV filtrado por sucursal |
| `GET` | `/api/productos/exportar/csv?soloAlertas=true` | CSV solo alertas |
| `GET` | `/api/categorias` | Lista categorías |
| `POST` | `/api/categorias` | Crear categoría |
| `PUT` | `/api/categorias/{id}` | Actualizar categoría |
| `DELETE` | `/api/categorias/{id}` | Eliminar categoría |

---

## 📁 Estructura del Proyecto

```
modulo-inventario/
├── src/main/java/com/example/inventario/
│   ├── controller/
│   │   ├── CategoriaController.java    → CRUD de categorías
│   │   └── ProductoController.java     → CRUD + alertas + exportación CSV
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java → Escudo anti-HTTP500
│   │   └── ResourceNotFoundException.java
│   ├── model/
│   │   ├── Categoria.java              → Entidad con @NotBlank, @Column(unique)
│   │   └── Producto.java               → Entidad con todas las validaciones
│   ├── repository/
│   │   ├── CategoriaRepository.java
│   │   └── ProductoRepository.java     → Consultas JPQL personalizadas
│   ├── service/
│   │   ├── LogService.java             → Auditoría en texto plano
│   │   └── CsvExportService.java       → Exportación blindada con OpenCSV
│   └── InventarioApplication.java
├── src/main/resources/
│   └── application.properties          → Conexión a Clever Cloud
├── src/test/java/com/example/inventario/
│   └── ProductoValidationTests.java    → 10 casos de prueba JUnit 5
├── frontend/
│   ├── index.html                      → UI con Bootstrap 5
│   └── app.js                          → Lógica con fetch() nativo
├── logs.txt                            → Auditoría generada automáticamente
├── README.md
└── pom.xml                             → 5 dependencias core + OpenCSV
```

---

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos
- Java 17 o superior
- Git
- Acceso a internet (la BD está en la nube)

> No es necesario instalar Maven. El proyecto usa Maven Wrapper (`mvnw.cmd`).

### 1. Clonar el repositorio
```bash
git clone https://github.com/DerlyBarreda4444/Mod-Inventario.git
cd Mod-Inventario
```

### 2. Ejecutar el backend
```bash
.\mvnw.cmd spring-boot:run
```
El servidor arranca en `http://localhost:8080`. Las tablas se crean automáticamente en Clever Cloud.

### 3. Abrir el frontend
Abrir el archivo `frontend/index.html` directamente en el navegador.

### 4. Ejecutar pruebas unitarias
```bash
.\mvnw.cmd test
```

---

## 🧪 Pruebas Unitarias (JUnit 5)

Las pruebas están en `ProductoValidationTests.java` y no requieren base de datos ni servidor:

| Caso de Prueba | Resultado Esperado |
|---|---|
| Producto con todos los campos válidos | ✅ Sin errores |
| SKU vacío o solo espacios | ❌ Violación @NotBlank |
| Precio negativo (-5.00) | ❌ Violación @Positive |
| Precio igual a cero (0.0) | ❌ Violación @Positive |
| Precio null | ❌ Violación @NotNull |
| Stock negativo (-1) | ❌ Violación @Min(0) |
| Stock igual a cero (agotado) | ✅ Válido |
| Nombre o sucursal vacíos | ❌ Violación @NotBlank |

---

## 📋 Auditoría — logs.txt

Todas las operaciones críticas quedan registradas:

```
[2026-05-19 14:00:35] [CREAR] Producto 'Lapiz HB' (SKU: LAPIZ-HB-001) creado en sucursal Norte
[2026-05-19 14:05:12] [ACTUALIZAR] Producto 'Lapiz HB' actualizado. Stock: 200 → 180 (salida de 20 unidades)
[2026-05-19 14:10:00] [ELIMINAR] Producto 'Borrador Pelikan' (ID: 7) eliminado del sistema
[2026-05-19 14:15:22] [EXPORTAR] CSV exportado: inventario_Norte.csv (1024 bytes)
```

---

## 🏗️ Arquitectura

El proyecto aplica el patrón MVC con separación estricta de responsabilidades:

```
Navegador (fetch)
    ↓
Controller (@RestController + @CrossOrigin)
    ↓
Service (LogService / CsvExportService)
    ↓
Repository (Spring Data JPA)
    ↓
MySQL en Clever Cloud
```

**Capa de protección:** `GlobalExceptionHandler` intercepta cualquier excepción en el camino y devuelve siempre un JSON controlado, nunca un HTTP 500 con stack trace.

---

## 👥 Integrantes

- Alagon Cutiri Daniel Alejandro
- Barreda Contreras Derly Harold
- Salinas Salas Santiago Alonso
- Valeriano Arapa Allison Antonella
  
---

## 📚 Referencias

- https://spring.io/projects/spring-boot
- https://hibernate.org/validator/
- http://opencsv.sourceforge.net/
- https://getbootstrap.com/
- https://www.clever-cloud.com/
