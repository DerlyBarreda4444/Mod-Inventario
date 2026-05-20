# 📦 Operación PaperStock — Módulo de Inventarios

> Sistema de control de stock centralizado con soporte para sucursales distribuidas, auditoría en texto plano y exportación dinámica de reportes CSV.

---

## 🏗️ Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 17 + Spring Boot 3.5 |
| Base de Datos | MySQL (Clever Cloud) |
| ORM | Spring Data JPA + Hibernate |
| Validaciones | Hibernate Validator |
| Exportación | OpenCSV 5.9 |
| Frontend | HTML5 + Bootstrap 5 + JavaScript (fetch nativo) |

---

## 🚀 Instalación y Configuración

### 1. Prerequisitos
- Java 17 o superior instalado
- Maven 3.8+
- IntelliJ IDEA (recomendado) o cualquier IDE Java
- Cuenta en [Clever Cloud](https://www.clever-cloud.com/) con base de datos MySQL creada

### 2. Configurar la base de datos
Edita `src/main/resources/application.properties` y reemplaza con tus credenciales de Clever Cloud:

```properties
spring.datasource.url=jdbc:mysql://<HOST>:<PUERTO>/<BD>?useSSL=true&serverTimezone=UTC
spring.datasource.username=<USUARIO>
spring.datasource.password=<CONTRASEÑA>
```

### 3. Ejecutar el backend
```bash
# Desde la raíz del proyecto
mvn spring-boot:run
```
El servidor arranca en `http://localhost:8080`

### 4. Ejecutar el frontend
Abre `frontend/index.html` directamente en el navegador.
> **Nota:** Si usas VS Code, instala la extensión "Live Server" para evitar problemas de CORS con `file://`.

### 5. Ejecutar las pruebas unitarias
```bash
mvn test
```

---

## 🗺️ Endpoints de la API REST

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/productos` | Lista todos los productos |
| `GET` | `/api/productos?sucursal=Norte` | Filtra por sucursal |
| `GET` | `/api/productos/alertas` | Productos con stock bajo |
| `GET` | `/api/productos/{id}` | Un producto por ID |
| `POST` | `/api/productos` | Crea un nuevo producto |
| `PUT` | `/api/productos/{id}` | Actualiza un producto |
| `DELETE` | `/api/productos/{id}` | Elimina un producto |
| `GET` | `/api/productos/exportar/csv` | Descarga CSV completo |
| `GET` | `/api/productos/exportar/csv?sucursal=Norte` | CSV de una sucursal |
| `GET` | `/api/productos/exportar/csv?soloAlertas=true` | CSV solo alertas |
| `GET` | `/api/categorias` | Lista categorías |
| `POST` | `/api/categorias` | Crea categoría |
| `PUT` | `/api/categorias/{id}` | Actualiza categoría |
| `DELETE` | `/api/categorias/{id}` | Elimina categoría |

### Ejemplo: Crear un producto (POST)
```json
{
    "sku": "LAPIZ-HB-001",
    "nombre": "Lápiz HB Staedtler x12",
    "precio": 4.50,
    "stock": 200,
    "stockMinimo": 20,
    "sucursal": "Norte",
    "categoria": { "id": 1 }
}
```

---

## 🛡️ Mecanismo Anti-Bugs

- **`GlobalExceptionHandler`**: Intercepta todo error antes de que se convierta en HTTP 500. Devuelve siempre un JSON con `status` y `mensaje`.
- **`@Valid` en controllers**: Activa las anotaciones del modelo (`@NotBlank`, `@Min`, `@Positive`) antes de tocar la base de datos.
- **`@Column(unique=true)`**: El SKU y el nombre de categoría son únicos a nivel de BD.
- **OpenCSV**: Envuelve automáticamente en comillas cualquier campo con comas, evitando el "Efecto Coma" en Excel.

---

## 📋 Auditoría

Cada operación exitosa (crear, actualizar, eliminar, exportar) queda registrada en `logs.txt`:

```
[2026-05-19 14:00:35] [CREAR] Producto 'Lápiz HB' (SKU: LAPIZ-HB-001) creado en sucursal Norte
[2026-05-19 14:05:12] [ACTUALIZAR] Producto 'Lápiz HB' actualizado. Stock: 200 → 180 (salida de 20 unidades)
[2026-05-19 14:10:00] [EXPORTAR] CSV exportado: inventario_Norte.csv (1024 bytes)
```

---

## 📁 Estructura del Proyecto

```
modulo-inventario/
├── src/main/java/com/example/inventario/
│   ├── controller/     # CategoriaController, ProductoController
│   ├── exception/      # GlobalExceptionHandler, ResourceNotFoundException
│   ├── model/          # Categoria, Producto (con validaciones)
│   ├── repository/     # CategoriaRepository, ProductoRepository
│   ├── service/        # LogService, CsvExportService
│   └── InventarioApplication.java
├── src/main/resources/
│   └── application.properties
├── src/test/java/
│   └── ProductoValidationTests.java
├── frontend/
│   ├── index.html
│   └── app.js
├── logs.txt            (generado automáticamente)
├── README.md
└── pom.xml
```

---

## 👥 Equipo
Proyecto académico — Operación PaperStock
