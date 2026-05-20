/**
 * OPERACIÓN PAPERSTOCK — app.js
 * Toda la lógica del frontend usando fetch() nativo (sin jQuery).
 * Se comunica con la API REST de Spring Boot vía CORS.
 */

// ── Configuración ────────────────────────────────────────────────────────────
// 🔧 CAMBIAR esta URL cuando despliegues en Clever Cloud:
const API_BASE = "http://localhost:8080";

// ── Referencias a modales de Bootstrap ──────────────────────────────────────
let modalProducto, modalCategoria, toastOk;

// ── Estado global del filtro activo ─────────────────────────────────────────
let filtroActual = { sucursal: "", soloAlertas: false };

// ══════════════════════════════════════════════════════════════════════════════
// INICIALIZACIÓN AL CARGAR LA PÁGINA
// ══════════════════════════════════════════════════════════════════════════════
document.addEventListener("DOMContentLoaded", () => {
    // Inicializar componentes Bootstrap
    modalProducto = new bootstrap.Modal(document.getElementById("modalProducto"));
    modalCategoria = new bootstrap.Modal(document.getElementById("modalCategoria"));
    toastOk = new bootstrap.Toast(document.getElementById("toastOk"), { delay: 3000 });

    // Reloj en la navbar
    actualizarReloj();
    setInterval(actualizarReloj, 1000);

    // Cargar datos iniciales
    cargarProductos();
    cargarCategorias();
});

function actualizarReloj() {
    const ahora = new Date();
    document.getElementById("fechaHora").textContent = ahora.toLocaleString("es-PE", {
        weekday: "long", year: "numeric", month: "long", day: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

// ══════════════════════════════════════════════════════════════════════════════
// CARGA Y RENDERIZADO DE PRODUCTOS
// ══════════════════════════════════════════════════════════════════════════════
async function cargarProductos(sucursal = "", soloAlertas = false) {
    mostrarSpinner(true);

    let url = `${API_BASE}/api/productos`;
    const params = new URLSearchParams();

    if (soloAlertas) {
        url = `${API_BASE}/api/productos/alertas`;
    }
    if (sucursal) {
        params.append("sucursal", sucursal);
    }
    if (params.toString()) {
        url += "?" + params.toString();
    }

    try {
        const resp = await fetch(url);
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        const productos = await resp.json();
        renderizarTabla(productos);
        actualizarEstadisticas(productos);
    } catch (err) {
        mostrarError("No se pudo conectar con el servidor. Verifica que Spring Boot esté corriendo.");
        console.error(err);
    } finally {
        mostrarSpinner(false);
    }
}

function renderizarTabla(productos) {
    const tbody = document.getElementById("cuerpoTabla");
    document.getElementById("contadorFilas").textContent = productos.length;

    if (productos.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="10" class="text-center text-muted py-5">
                    <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                    No hay productos que coincidan con el filtro.
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = productos.map(p => {
        const enAlerta = p.stock <= p.stockMinimo;
        const badgeEstado = enAlerta
            ? `<span class="badge badge-alerta"><i class="bi bi-exclamation-triangle me-1"></i>ALERTA</span>`
            : `<span class="badge bg-success">✓ OK</span>`;
        const stockColor = enAlerta ? "text-danger fw-bold" : "";

        return `
        <tr class="${enAlerta ? "table-warning" : ""}">
            <td class="text-muted small">#${p.id}</td>
            <td><code class="small">${escaparHtml(p.sku)}</code></td>
            <td class="fw-semibold">${escaparHtml(p.nombre)}</td>
            <td>
                <span class="badge bg-info text-dark">
                    ${p.categoria ? escaparHtml(p.categoria.nombre) : "–"}
                </span>
            </td>
            <td>S/. ${Number(p.precio).toFixed(2)}</td>
            <td class="${stockColor}">${p.stock}</td>
            <td class="text-muted">${p.stockMinimo}</td>
            <td><span class="badge bg-secondary">${escaparHtml(p.sucursal)}</span></td>
            <td>${badgeEstado}</td>
            <td class="text-center">
                <button class="btn btn-outline-primary btn-sm py-0 me-1"
                        onclick="abrirModalEditar(${p.id})"
                        title="Editar producto">
                    <i class="bi bi-pencil-fill"></i>
                </button>
                <button class="btn btn-outline-danger btn-sm py-0"
                        onclick="confirmarEliminar(${p.id}, '${escaparHtml(p.nombre)}')"
                        title="Eliminar producto">
                    <i class="bi bi-trash-fill"></i>
                </button>
            </td>
        </tr>`;
    }).join("");
}

function actualizarEstadisticas(productos) {
    const alertas = productos.filter(p => p.stock <= p.stockMinimo).length;
    const sucursales = new Set(productos.map(p => p.sucursal)).size;

    document.getElementById("statTotal").textContent = productos.length;
    document.getElementById("statAlertas").textContent = alertas;
    document.getElementById("statSucursales").textContent = sucursales;
}

// ══════════════════════════════════════════════════════════════════════════════
// CATEGORÍAS
// ══════════════════════════════════════════════════════════════════════════════
async function cargarCategorias() {
    try {
        const resp = await fetch(`${API_BASE}/api/categorias`);
        const categorias = await resp.json();

        // Llenar el select del modal de producto
        const selectCat = document.getElementById("inputCategoria");
        const currentVal = selectCat.value;
        selectCat.innerHTML = '<option value="">-- Seleccionar --</option>';
        categorias.forEach(c => {
            selectCat.innerHTML += `<option value="${c.id}">${escaparHtml(c.nombre)}</option>`;
        });
        if (currentVal) selectCat.value = currentVal;

        // Actualizar tarjeta de estadísticas
        document.getElementById("statCategorias").textContent = categorias.length;

        // Llenar lista del modal de categorías
        const lista = document.getElementById("listaCategorias");
        if (lista) {
            lista.innerHTML = categorias.length === 0
                ? '<li class="list-group-item text-muted text-center">No hay categorías todavía</li>'
                : categorias.map(c => `
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <span><i class="bi bi-tag me-2 text-info"></i>${escaparHtml(c.nombre)}</span>
                        <button class="btn btn-outline-danger btn-sm py-0"
                                onclick="eliminarCategoria(${c.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </li>`).join("");
        }
    } catch (err) {
        console.error("Error cargando categorías:", err);
    }
}

async function crearCategoria() {
    const input = document.getElementById("inputNuevaCat");
    const nombre = input.value.trim();
    if (!nombre) { input.focus(); return; }

    try {
        const resp = await fetch(`${API_BASE}/api/categorias`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nombre })
        });

        if (resp.ok) {
            input.value = "";
            await cargarCategorias();
            mostrarToast(`Categoría "${nombre}" creada exitosamente`);
        } else {
            const error = await resp.json();
            alert("Error: " + (error.mensaje || JSON.stringify(error)));
        }
    } catch (err) {
        alert("Error de conexión al crear categoría.");
    }
}

async function eliminarCategoria(id) {
    if (!confirm("¿Eliminar esta categoría? Solo es posible si no tiene productos asociados.")) return;

    try {
        const resp = await fetch(`${API_BASE}/api/categorias/${id}`, { method: "DELETE" });
        if (resp.ok || resp.status === 204) {
            await cargarCategorias();
            mostrarToast("Categoría eliminada");
        } else {
            const error = await resp.json();
            alert("No se puede eliminar: " + (error.mensaje || "tiene productos asociados"));
        }
    } catch (err) {
        alert("Error de conexión.");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CREAR / EDITAR PRODUCTO
// ══════════════════════════════════════════════════════════════════════════════
function abrirModalCrear() {
    document.getElementById("tituloModal").innerHTML =
        '<i class="bi bi-plus-circle me-2"></i>Nuevo Producto';
    limpiarFormulario();
    ocultarAlertaError();
    cargarCategorias();
    modalProducto.show();
}

async function abrirModalEditar(id) {
    document.getElementById("tituloModal").innerHTML =
        '<i class="bi bi-pencil-fill me-2"></i>Editar Producto';
    ocultarAlertaError();
    await cargarCategorias();

    try {
        const resp = await fetch(`${API_BASE}/api/productos/${id}`);
        const p = await resp.json();

        document.getElementById("productoId").value = p.id;
        document.getElementById("inputSku").value = p.sku;
        document.getElementById("inputNombre").value = p.nombre;
        document.getElementById("inputPrecio").value = p.precio;
        document.getElementById("inputStock").value = p.stock;
        document.getElementById("inputStockMinimo").value = p.stockMinimo;
        document.getElementById("inputSucursal").value = p.sucursal;
        document.getElementById("inputCategoria").value = p.categoria ? p.categoria.id : "";

        modalProducto.show();
    } catch (err) {
        alert("No se pudo cargar el producto para editar.");
    }
}

async function guardarProducto() {
    ocultarAlertaError();

    const id = document.getElementById("productoId").value;
    const categoriaId = document.getElementById("inputCategoria").value;

    const payload = {
        sku: document.getElementById("inputSku").value.trim(),
        nombre: document.getElementById("inputNombre").value.trim(),
        precio: parseFloat(document.getElementById("inputPrecio").value),
        stock: parseInt(document.getElementById("inputStock").value),
        stockMinimo: parseInt(document.getElementById("inputStockMinimo").value),
        sucursal: document.getElementById("inputSucursal").value,
        categoria: categoriaId ? { id: parseInt(categoriaId) } : null
    };

    // Validación básica del lado del cliente
    if (!payload.sku || !payload.nombre || !payload.sucursal || !categoriaId) {
        mostrarAlertaError("Por favor completa todos los campos obligatorios.");
        return;
    }

    const esEdicion = id !== "";
    const url = esEdicion
        ? `${API_BASE}/api/productos/${id}`
        : `${API_BASE}/api/productos`;
    const method = esEdicion ? "PUT" : "POST";

    try {
        const resp = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (resp.ok || resp.status === 201) {
            modalProducto.hide();
            mostrarToast(esEdicion ? "Producto actualizado correctamente" : "Producto creado exitosamente");
            aplicarFiltros(); // Recargar con los filtros activos
        } else {
            const error = await resp.json();
            // Mostrar el detalle de los errores de validación
            let mensaje = error.error || "Error al guardar";
            if (error.detalles) {
                mensaje += ":\n" + Object.entries(error.detalles)
                    .map(([campo, msg]) => `• ${campo}: ${msg}`)
                    .join("\n");
            } else if (error.mensaje) {
                mensaje += ": " + error.mensaje;
            }
            mostrarAlertaError(mensaje);
        }
    } catch (err) {
        mostrarAlertaError("Error de conexión con el servidor.");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ELIMINAR PRODUCTO
// ══════════════════════════════════════════════════════════════════════════════
async function confirmarEliminar(id, nombre) {
    if (!confirm(`¿Estás seguro de eliminar el producto "${nombre}"?\nEsta acción no se puede deshacer.`)) return;

    try {
        const resp = await fetch(`${API_BASE}/api/productos/${id}`, { method: "DELETE" });
        if (resp.ok || resp.status === 204) {
            mostrarToast(`Producto "${nombre}" eliminado`);
            aplicarFiltros();
        } else {
            alert("No se pudo eliminar el producto.");
        }
    } catch (err) {
        alert("Error de conexión.");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// FILTROS
// ══════════════════════════════════════════════════════════════════════════════
function aplicarFiltros() {
    const sucursal = document.getElementById("filtroSucursal").value;
    const alerta = document.getElementById("filtroAlerta").value;
    filtroActual = { sucursal, soloAlertas: alerta === "alertas" };
    cargarProductos(filtroActual.sucursal, filtroActual.soloAlertas);
}

function limpiarFiltros() {
    document.getElementById("filtroSucursal").value = "";
    document.getElementById("filtroAlerta").value = "";
    filtroActual = { sucursal: "", soloAlertas: false };
    cargarProductos();
}

// ══════════════════════════════════════════════════════════════════════════════
// EXPORTAR CSV DINÁMICO
// El CSV respeta los filtros activos en pantalla.
// ══════════════════════════════════════════════════════════════════════════════
function exportarCsv() {
    const params = new URLSearchParams();
    if (filtroActual.sucursal) params.append("sucursal", filtroActual.sucursal);
    if (filtroActual.soloAlertas) params.append("soloAlertas", "true");

    const url = `${API_BASE}/api/productos/exportar/csv?${params.toString()}`;

    // Crear un enlace temporal para forzar la descarga
    const enlace = document.createElement("a");
    enlace.href = url;
    enlace.download = ""; // El nombre lo decide el servidor (Content-Disposition)
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);

    mostrarToast("📥 Descargando CSV con los filtros actuales...");
}

// ══════════════════════════════════════════════════════════════════════════════
// MODAL CATEGORÍAS
// ══════════════════════════════════════════════════════════════════════════════
function abrirModalCategoria() {
    cargarCategorias();
    modalCategoria.show();
}

// ══════════════════════════════════════════════════════════════════════════════
// UTILIDADES
// ══════════════════════════════════════════════════════════════════════════════
function limpiarFormulario() {
    ["productoId", "inputSku", "inputNombre", "inputPrecio",
     "inputStock", "inputStockMinimo"].forEach(id => {
        document.getElementById(id).value = "";
    });
    document.getElementById("inputSucursal").value = "";
    document.getElementById("inputCategoria").value = "";
}

function mostrarAlertaError(mensaje) {
    const alerta = document.getElementById("alertaError");
    alerta.innerHTML = `<i class="bi bi-exclamation-triangle-fill me-2"></i>${mensaje.replace(/\n/g, "<br>")}`;
    alerta.classList.remove("d-none");
}

function ocultarAlertaError() {
    document.getElementById("alertaError").classList.add("d-none");
}

function mostrarToast(mensaje) {
    document.getElementById("toastMensaje").textContent = mensaje;
    toastOk.show();
}

function mostrarSpinner(visible) {
    document.getElementById("loadingSpinner").style.display = visible ? "block" : "none";
}

function mostrarError(mensaje) {
    document.getElementById("cuerpoTabla").innerHTML = `
        <tr>
            <td colspan="10" class="text-center text-danger py-4">
                <i class="bi bi-wifi-off fs-3 d-block mb-2"></i>
                ${mensaje}
            </td>
        </tr>`;
}

// Previene XSS: nunca insertar HTML del servidor directamente
function escaparHtml(texto) {
    if (!texto) return "";
    return String(texto)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
