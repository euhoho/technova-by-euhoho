// =============================================================
// app.js — TechNova Store · Tienda (tienda.html)
// Entregable 6: Checkout, Stock, Validaciones, Seguridad
// =============================================================

const API_URL = 'http://localhost:8080/api';

// Productos cargados de la API (los guardamos en memoria para filtrar sin volver a pedir)
let todosLosProductos = [];

// Carrito en memoria. precio/nombre solo sirven para la UI.
// Al pagar, NUNCA se envía el precio al backend (seguridad).
let carrito = [];

// SKUs de los 3 productos que aparecen como "destacados"
const DESTACADOS_SKUS = ['AUR-Z23C', 'REL-SF4O', 'PAN-EDXG'];

// =============================================================
// ARRANQUE — se ejecuta cuando el HTML está listo
// =============================================================
document.addEventListener('DOMContentLoaded', () => {
    cargarProductos();     // llama a la API y pinta el catálogo
    iniciarFiltros();      // botones de categoría
    iniciarBuscador();     // campo de búsqueda
    iniciarLogin();        // modal de login / logout
    iniciarNavbarScroll(); // navbar que se oculta al hacer scroll
    restaurarSesion();     // si el usuario ya estaba logueado, muestra su nombre
});

// =============================================================
// NAVBAR — se oculta al bajar y reaparece al subir
// =============================================================
function iniciarNavbarScroll() {
    const navbar = document.getElementById('navbar');
    let lastY    = window.scrollY;
    let ticking  = false;

    window.addEventListener('scroll', () => {
        if (!ticking) {
            requestAnimationFrame(() => {
                const currentY = window.scrollY;
                navbar.classList.toggle('scrolled',      currentY > 10);
                navbar.classList.toggle('navbar-hidden', currentY > lastY && currentY > 80);
                lastY   = currentY;
                ticking = false;
            });
            ticking = true;
        }
    }, { passive: true });
}

// =============================================================
// SESIÓN — restaura nombre en la navbar si ya estaba logueado
// =============================================================
function restaurarSesion() {
    const nombre = sessionStorage.getItem('nombre');
    const rol    = sessionStorage.getItem('rol');
    if (nombre) mostrarUsuarioEnNavbar(nombre, rol);
}

// =============================================================
// PRODUCTOS — GET /api/productos
// =============================================================
async function cargarProductos() {
    const spinner  = document.getElementById('loading-spinner');
    const errorMsg = document.getElementById('error-msg');

    spinner.classList.remove('d-none');
    errorMsg.classList.add('d-none');

    try {
        const respuesta = await fetch(`${API_URL}/productos`);
        if (!respuesta.ok) throw new Error(`HTTP ${respuesta.status}`);

        todosLosProductos = await respuesta.json();
        spinner.classList.add('d-none');

        renderizarDestacados();
        renderizarProductos(todosLosProductos);

        // Si se llegó con ?sku=XXX en la URL, abrimos el detalle directamente
        const sku = new URLSearchParams(window.location.search).get('sku');
        if (sku) abrirDetalle(sku);

    } catch (error) {
        spinner.classList.add('d-none');
        errorMsg.classList.remove('d-none');
        console.error('Error al cargar productos:', error);
    }
}

// =============================================================
// DESTACADOS — 3 productos fijos en la parte superior
// =============================================================
function renderizarDestacados() {
    const contenedor = document.getElementById('destacados-container');
    if (!contenedor) return;

    const destacados = DESTACADOS_SKUS
        .map(sku => todosLosProductos.find(p => p.sku === sku))
        .filter(Boolean); // descarta los SKUs que no existan en la API

    if (!destacados.length) { contenedor.innerHTML = ''; return; }

    contenedor.innerHTML =
        '<div class="destacados-grid">' +
        destacados.map(p => `
            <div class="card-producto card-grande ${p.stock === 0 ? 'agotado' : ''}"
                 onclick="abrirDetalle('${p.sku}')">
                <div class="card-img-area">
                    <span class="badge-featured">Featured</span>
                    <img src="http://localhost:8080/imagenes/${p.imagen}" alt="${escapeHtml(p.nombre)}"
                         onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                    <div class="card-img-fallback" style="display:none">${iconoCategoria(p.categoria)}</div>
                    ${p.stock === 0               ? '<span class="badge-agotado">Agotado</span>'         : ''}
                    ${p.stock > 0 && p.stock <= 5 ? '<span class="badge-poco-stock">¡Queda poco!</span>' : ''}
                </div>
                <div class="card-info">
                    <span class="card-nombre">${escapeHtml(p.nombre)}</span>
                    <span class="card-precio ${p.stock === 0 ? 'muted' : ''}">${formatearPrecio(p.precio)}</span>
                </div>
            </div>`
        ).join('') +
        '</div>';
}

// =============================================================
// CATÁLOGO COMPLETO
// =============================================================
function renderizarProductos(productos) {
    const contenedor = document.getElementById('catalogo-container');
    const destCont   = document.getElementById('destacados-container');
    const hayFiltro  = !!document.querySelector('.filtro-pill.active')?.dataset.categoria;

    contenedor.innerHTML = '';
    // Ocultamos los destacados cuando hay un filtro activo
    if (destCont) destCont.style.display = hayFiltro ? 'none' : '';

    if (!productos.length) {
        contenedor.innerHTML = `
            <div class="col-12 text-center py-5">
                <p style="font-family:var(--font-mono);color:var(--gray-text);font-size:0.85rem;">
                    No hay productos en esta categoría.
                </p>
            </div>`;
        return;
    }

    productos.forEach(p => {
        const agotado   = p.stock === 0;
        const pocoStock = p.stock > 0 && p.stock <= 5;
        const col       = document.createElement('div');
        col.className   = 'col-6 col-md-4 col-lg-3';
        col.innerHTML   = `
            <div class="card-producto ${agotado ? 'agotado' : ''}" onclick="abrirDetalle('${p.sku}')">
                <div class="card-img-area">
                    <img src="http://localhost:8080/imagenes/${p.imagen}" alt="${escapeHtml(p.nombre)}"
                         onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                    <div class="card-img-fallback" style="display:none">${iconoCategoria(p.categoria)}</div>
                    ${agotado   ? '<span class="badge-agotado">Agotado</span>'         : ''}
                    ${pocoStock ? '<span class="badge-poco-stock">¡Queda poco!</span>' : ''}
                </div>
                <div class="card-info">
                    <span class="card-nombre">${escapeHtml(p.nombre)}</span>
                    <span class="card-precio ${agotado ? 'muted' : ''}">${formatearPrecio(p.precio)}</span>
                </div>
            </div>`;
        contenedor.appendChild(col);
    });
}

function iconoCategoria(cat) {
    const iconos = { AURICULARES: '🎧', PANTALLAS: '🖥️', RELOJES: '⌚', MOVILES: '📱' };
    return `<span style="font-size:2.5rem">${iconos[cat] || '📦'}</span>`;
}

// =============================================================
// FILTROS — pills de categoría
// =============================================================
function iniciarFiltros() {
    const pills = document.querySelectorAll('.filtro-pill');
    pills.forEach(pill => {
        pill.addEventListener('click', () => {
            pills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            const categoria = pill.dataset.categoria;
            renderizarProductos(
                categoria
                    ? todosLosProductos.filter(p => p.categoria === categoria)
                    : todosLosProductos
            );
        });
    });
}

// =============================================================
// BUSCADOR — filtra en tiempo real por nombre
// =============================================================
function iniciarBuscador() {
    const wrapper = document.getElementById('buscador-wrapper');
    const btn     = document.getElementById('buscador-btn');
    const input   = document.getElementById('buscador');

    btn.addEventListener('click', () => {
        const abierto = wrapper.classList.toggle('activo');
        if (abierto) { input.focus(); }
        else         { input.value = ''; filtrarYRenderizar(''); }
    });

    input.addEventListener('blur',  () => { if (!input.value) wrapper.classList.remove('activo'); });
    input.addEventListener('input', () => filtrarYRenderizar(input.value.trim().toLowerCase()));
}

function filtrarYRenderizar(texto) {
    const categoria = document.querySelector('.filtro-pill.active').dataset.categoria;
    const filtrados = todosLosProductos.filter(p =>
        (!categoria || p.categoria === categoria) &&
        p.nombre.toLowerCase().includes(texto)
    );
    renderizarProductos(filtrados);
}

// =============================================================
// CARRITO
//
// SEGURIDAD: precio y nombre se guardan SOLO para mostrarlos
// en la UI. Al hacer checkout enviamos ÚNICAMENTE id y cantidad.
// El backend consulta el precio real en su propia base de datos.
// =============================================================

function anadirAlCarrito(id, sku, nombre, precio) {
    const existente = carrito.find(i => i.id === id);
    if (existente) {
        // No dejamos superar el stock disponible
        const producto = todosLosProductos.find(p => p.id_producto === id);
        if (producto && existente.cantidad >= producto.stock) return;
        existente.cantidad++;
    } else {
        carrito.push({ id, sku, nombre, precio, cantidad: 1 });
    }
    actualizarCarritoUI();
    mostrarToast(`"${nombre}" añadido al carrito`);
}

function quitarDelCarrito(id) {
    carrito = carrito.filter(i => i.id !== id);
    actualizarCarritoUI();
}

function cambiarCantidad(id, delta) {
    const item = carrito.find(i => i.id === id);
    if (!item) return;

    const nuevaCantidad = item.cantidad + delta;
    if (nuevaCantidad < 1) { quitarDelCarrito(id); return; }

    // No dejamos poner más cantidad que stock hay
    const producto = todosLosProductos.find(p => p.id_producto === id);
    if (producto && nuevaCantidad > producto.stock) return;

    item.cantidad = nuevaCantidad;
    actualizarCarritoUI();
}

function actualizarCarritoUI() {
    const total   = carrito.reduce((s, i) => s + i.cantidad, 0);
    const countEl = document.getElementById('carrito-count');
    countEl.textContent = total;
    countEl.classList.toggle('d-none', total === 0);
    renderizarCarritoModal();
}

function renderizarCarritoModal() {
    const body       = document.getElementById('carrito-body');
    const totalEl    = document.getElementById('carrito-total');
    const btnComprar = document.getElementById('btn-comprar');
    const pagoSeguro = document.getElementById('carrito-pago-seguro');

    if (!carrito.length) {
        body.innerHTML = '<p class="carrito-vacio">Tu carrito está vacío.</p>';
        totalEl.textContent = '';
        if (btnComprar) btnComprar.style.display = 'none';
        if (pagoSeguro) pagoSeguro.style.display = 'none';
        return;
    }

    body.innerHTML = carrito.map(item => {
        const producto = todosLosProductos.find(p => p.id_producto === item.id);
        const enLimite = producto && item.cantidad >= producto.stock;
        const imgSrc   = producto ? `http://localhost:8080/imagenes/${producto.imagen}` : '';
        return `
        <div class="carrito-item">
            <div class="carrito-item-thumb">
                ${imgSrc ? `<img src="${imgSrc}" alt="${escapeHtml(item.nombre)}" onerror="this.style.display='none'">` : ''}
            </div>
            <div class="carrito-item-nombre">${escapeHtml(item.nombre)}</div>
            <div class="carrito-item-controles">
                <button onclick="cambiarCantidad(${item.id}, -1)">−</button>
                <span>${item.cantidad}</span>
                <button onclick="cambiarCantidad(${item.id}, 1)" ${enLimite ? 'disabled style="opacity:0.3;cursor:not-allowed"' : ''}>+</button>
            </div>
            <div class="carrito-item-precio">${formatearPrecio(item.precio * item.cantidad)}</div>
            <button class="carrito-item-borrar" onclick="quitarDelCarrito(${item.id})">×</button>
        </div>`;
    }).join('');

    // "Total estimado" porque el precio definitivo lo confirma el backend
    totalEl.innerHTML = `Total estimado: <strong>${formatearPrecio(carrito.reduce((s, i) => s + i.precio * i.cantidad, 0))}</strong>`;
    if (btnComprar) btnComprar.style.display = '';
    if (pagoSeguro) pagoSeguro.style.display = '';
}

function mostrarToast(mensaje) {
    const toast   = document.getElementById('carrito-toast');
    const msgSpan = document.getElementById('carrito-toast-msg');
    msgSpan.textContent = mensaje;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2600);
}

// =============================================================
// CHECKOUT — POST /api/pedidos
//
// REGLA DE SEGURIDAD CRÍTICA:
//   ✅ Enviamos:  { id_usuario, items: [{ id_producto, cantidad }] }
//   ❌ NUNCA enviamos el precio — el usuario podría manipularlo.
//      El backend busca el precio en su propia base de datos.
// =============================================================
async function realizarCompra() {
    if (!carrito.length) return;

    // Si no está logueado, cerramos el carrito y abrimos el login
    const idUsuario = sessionStorage.getItem('id');
    if (!idUsuario) {
        const carritoEl   = document.getElementById('carritoModal');
        const carritoInst = bootstrap.Modal.getInstance(carritoEl);
        if (carritoInst) {
            carritoEl.addEventListener('hidden.bs.modal', () => {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('loginModal')).show();
            }, { once: true });
            carritoInst.hide();
        } else {
            bootstrap.Modal.getOrCreateInstance(document.getElementById('loginModal')).show();
        }
        return;
    }

    // Validación frontend: todas las cantidades deben ser enteros positivos
    for (const item of carrito) {
        if (!Number.isInteger(item.cantidad) || item.cantidad < 1) {
            mostrarAlerta('danger', `La cantidad de "${item.nombre}" no es válida.`);
            return;
        }
    }

    const btnComprar = document.getElementById('btn-comprar');
    if (btnComprar) { btnComprar.disabled = true; btnComprar.textContent = 'Procesando…'; }

    try {
        const respuesta = await fetch(`${API_URL}/pedidos`, {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                id_usuario: parseInt(idUsuario, 10),
                // Solo id y cantidad — NUNCA el precio
                items: carrito.map(i => ({ id_producto: i.id, cantidad: i.cantidad }))
            })
        });

        const datos = await respuesta.json();

        // 409 = stock insuficiente (el backend lo detectó antes de guardar)
        if (respuesta.status === 409) {
            mostrarAlerta('warning', datos.mensaje || 'No hay stock suficiente de algún producto.');
            return;
        }

        // Cualquier otro error del servidor
        if (!respuesta.ok) {
            mostrarAlerta('danger', 'Hubo un problema con tu pedido. Inténtalo de nuevo.');
            console.error('Error pedido:', datos.mensaje);
            return;
        }

        // ÉXITO: vaciamos el carrito y mostramos la confirmación
        carrito = [];
        actualizarCarritoUI();

        const carritoModal = bootstrap.Modal.getInstance(document.getElementById('carritoModal'));
        if (carritoModal) {
            document.getElementById('carritoModal').addEventListener('hidden.bs.modal', () => {
                mostrarModalExito(datos.id_pedido);
            }, { once: true });
            carritoModal.hide();
        } else {
            mostrarModalExito(datos.id_pedido);
        }

        // Recargamos el catálogo para que el stock se actualice en pantalla
        cargarProductos();

    } catch (error) {
        // Error de red (servidor caído, sin internet, etc.)
        console.error('Error en checkout:', error);
        mostrarAlerta('danger', 'No se pudo conectar con el servidor. Inténtalo de nuevo más tarde.');
    } finally {
        if (btnComprar) { btnComprar.disabled = false; btnComprar.textContent = 'Comprar ahora'; }
    }
}

// =============================================================
// LOGIN — POST /api/login
// =============================================================
function iniciarLogin() {
    const btnLogin  = document.getElementById('btn-login');
    const btnLogout = document.getElementById('btn-logout');

    btnLogin.addEventListener('click', async () => {
        const emailInput    = document.getElementById('login-email');
        const passwordInput = document.getElementById('login-password');
        const email         = emailInput.value.trim();
        const password      = passwordInput.value;
        const errorDiv      = document.getElementById('login-error');

        // Limpiamos errores anteriores
        errorDiv.classList.add('d-none');
        errorDiv.textContent = '';
        emailInput.classList.remove('is-invalid');
        passwordInput.classList.remove('is-invalid');

        // Validación 1: campos obligatorios
        if (!email || !password) {
            errorDiv.textContent = 'Rellena todos los campos.';
            errorDiv.classList.remove('d-none');
            if (!email)    emailInput.classList.add('is-invalid');
            if (!password) passwordInput.classList.add('is-invalid');
            return;
        }

        // Validación 2: formato de email con regex
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            errorDiv.textContent = 'Introduce un email con formato válido.';
            errorDiv.classList.remove('d-none');
            emailInput.classList.add('is-invalid');
            return;
        }

        btnLogin.textContent = 'Entrando…';
        btnLogin.disabled    = true;

        try {
            const respuesta = await fetch(`${API_URL}/login`, {
                method:  'POST',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify({ email, password })
            });

            // 401 = credenciales incorrectas (usuario o contraseña mal)
            if (respuesta.status === 401) {
                errorDiv.textContent = 'Credenciales incorrectas. Inténtalo de nuevo.';
                errorDiv.classList.remove('d-none');
                emailInput.classList.add('is-invalid');
                passwordInput.classList.add('is-invalid');
                return;
            }

            // Cualquier otro error HTTP (500, etc.)
            if (!respuesta.ok) throw new Error(`HTTP ${respuesta.status}`);

            const datos = await respuesta.json();

            // Guardamos la sesión (sessionStorage se borra al cerrar la pestaña)
            sessionStorage.setItem('nombre', datos.nombre || datos.email || email);
            sessionStorage.setItem('rol',    datos.rol);
            sessionStorage.setItem('id',     datos.id);

            bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
            mostrarUsuarioEnNavbar(datos.nombre || datos.email || email, datos.rol);

            emailInput.value    = '';
            passwordInput.value = '';

        } catch (error) {
            // Llegamos aquí si el servidor no responde (error de red)
            errorDiv.textContent = 'No se pudo conectar con el servidor. Inténtalo de nuevo.';
            errorDiv.classList.remove('d-none');
            console.error('Error en login:', error);
        } finally {
            btnLogin.textContent = 'Entrar';
            btnLogin.disabled    = false;
        }
    });

    // Quitar el borde rojo al volver a escribir
    ['login-email', 'login-password'].forEach(id => {
        const el = document.getElementById(id);
        el.addEventListener('input',   () => el.classList.remove('is-invalid'));
        el.addEventListener('keydown', e  => { if (e.key === 'Enter') btnLogin.click(); });
    });

    // Logout: borrar sesión y restaurar la navbar
    btnLogout.addEventListener('click', () => {
        sessionStorage.clear();
        document.getElementById('nav-user-item').classList.add('d-none');
        document.getElementById('nav-admin-item').classList.add('d-none');
        document.getElementById('nav-login-item').classList.remove('d-none');
    });
}

// =============================================================
// NAVBAR — muestra nombre de usuario o enlace de administrador
// =============================================================
function mostrarUsuarioEnNavbar(nombre, rol) {
    document.getElementById('nav-login-item').classList.add('d-none');
    document.getElementById('nav-user-item').classList.remove('d-none');
    document.getElementById('nav-avatar').textContent = nombre;
    if (rol === 'ADMINISTRADOR') {
        document.getElementById('nav-admin-item').classList.remove('d-none');
    }
}

// =============================================================
// DETALLE DE PRODUCTO — modal con toda la info
// =============================================================
function abrirDetalle(sku) {
    const producto = todosLosProductos.find(p => p.sku === sku);
    if (!producto) return;

    const agotado   = producto.stock === 0;
    const stockBajo = producto.stock > 0 && producto.stock <= 5;

    // Imagen con fallback al icono si no carga
    const img      = document.getElementById('detalle-img');
    const fallback = document.getElementById('detalle-img-fallback');
    img.src                = `http://localhost:8080/imagenes/${producto.imagen}`;
    img.style.display      = 'block';
    fallback.style.display = 'none';
    img.onerror = () => {
        img.style.display      = 'none';
        fallback.style.display = 'flex';
        fallback.innerHTML     = iconoCategoria(producto.categoria);
    };

    // Usamos textContent (nunca innerHTML) para evitar XSS
    document.getElementById('detalle-nombre').textContent      = producto.nombre;
    document.getElementById('detalle-categoria').textContent   = producto.categoria;
    document.getElementById('detalle-descripcion').textContent = producto.descripcion || '';
    document.getElementById('detalle-precio').textContent      = formatearPrecio(producto.precio);
    document.getElementById('detalle-sku').textContent         = producto.sku;

    const stockEl = document.getElementById('detalle-stock');
    if (agotado) {
        stockEl.textContent = 'Agotado';
        stockEl.className   = 'detalle-stock-texto';
    } else if (stockBajo) {
        stockEl.textContent = `¡Solo ${producto.stock} en stock!`;
        stockEl.className   = 'detalle-stock-texto bajo';
    } else {
        stockEl.textContent = `${producto.stock} en stock`;
        stockEl.className   = 'detalle-stock-texto';
    }

    document.getElementById('detalle-badge-agotado').classList.toggle('d-none',    !agotado);
    document.getElementById('detalle-badge-poco-stock').classList.toggle('d-none', !stockBajo);

    // Reemplazamos el botón para eliminar listeners anteriores (evita añadir duplicados)
    const btn = document.getElementById('detalle-btn-carrito');
    btn.disabled    = agotado;
    btn.textContent = agotado ? 'Sin stock' : '+ Añadir al carrito';
    btn.replaceWith(btn.cloneNode(true));

    if (!agotado) {
        document.getElementById('detalle-btn-carrito').addEventListener('click', () => {
            anadirAlCarrito(producto.id_producto, producto.sku, producto.nombre, producto.precio);
        });
    }

    // Limpiamos el ?sku= de la URL al cerrar el modal
    const detalleModalEl = document.getElementById('detalleModal');
    detalleModalEl.addEventListener('hidden.bs.modal', () => {
        history.replaceState(null, '', window.location.pathname);
    }, { once: true });

    new bootstrap.Modal(detalleModalEl).show();
}

// =============================================================
// ALERTAS — Bootstrap Alert (nunca alert() nativo del navegador)
// Usamos textContent para insertar el mensaje → anti-XSS
// =============================================================
function mostrarAlerta(tipo, mensaje) {
    const contenedor = obtenerContenedorAlertas();
    const alerta     = document.createElement('div');
    alerta.className = `alert alert-${tipo} alert-dismissible fade show alerta-tn`;
    alerta.setAttribute('role', 'alert');

    const span       = document.createElement('span');
    span.textContent = mensaje; // textContent, nunca innerHTML con datos externos

    const btnClose = document.createElement('button');
    btnClose.type      = 'button';
    btnClose.className = 'btn-close';
    btnClose.setAttribute('data-bs-dismiss', 'alert');
    btnClose.setAttribute('aria-label',      'Cerrar');

    alerta.append(span, btnClose);
    contenedor.appendChild(alerta);

    // Se cierra solo después de 5 segundos
    setTimeout(() => {
        alerta.classList.remove('show');
        alerta.addEventListener('transitionend', () => alerta.remove(), { once: true });
    }, 5000);
}

function obtenerContenedorAlertas() {
    let c = document.getElementById('alertas-globales');
    if (!c) {
        c = document.createElement('div');
        c.id        = 'alertas-globales';
        c.className = 'alertas-globales-wrapper';
        document.body.appendChild(c);
    }
    return c;
}

// =============================================================
// MODAL DE ÉXITO — aparece tras una compra correcta
// =============================================================
function mostrarModalExito(idPedido) {
    // Eliminamos si ya existía uno anterior
    document.getElementById('modalExitoCompra')?.remove();

    const wrapper = document.createElement('div');
    wrapper.id    = 'modalExitoCompra';
    wrapper.innerHTML = `
        <div class="modal fade" id="modalExitoBS" tabindex="-1">
            <div class="modal-dialog modal-dialog-centered" style="max-width:360px">
                <div class="modal-content" style="border-radius:16px;border:0.5px solid var(--bs-border-color,#e5e5e5);text-align:center;padding:2rem 2rem 1.5rem;">
                    <div style="width:56px;height:56px;border-radius:50%;background:#d1fae5;display:flex;align-items:center;justify-content:center;margin:0 auto 1.25rem;">
                        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#059669" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                    </div>
                    <h5 style="font-size:17px;font-weight:500;margin:0 0 8px;">¡Pedido confirmado!</h5>
                    <p style="font-size:13px;color:#6b7280;margin:0 0 4px;">Número de pedido</p>
                    <p style="font-size:22px;font-weight:500;margin:0 0 1.25rem;letter-spacing:0.02em;">#${idPedido}</p>
                    <p style="font-size:13px;color:#6b7280;margin:0 0 1.5rem;">En breve recibirás confirmación por email.</p>
                    <button class="btn btn-dark w-100" style="border-radius:10px;" data-bs-dismiss="modal">Seguir comprando</button>
                </div>
            </div>
        </div>`;
    document.body.appendChild(wrapper);

    const modal = new bootstrap.Modal(document.getElementById('modalExitoBS'));
    modal.show();

    document.getElementById('modalExitoBS').addEventListener('hidden.bs.modal', () => {
        wrapper.remove();
    }, { once: true });
}

// =============================================================
// UTILIDADES
// =============================================================

// Formatea un número como precio en euros: 1234.5 → "1.234,50 €"
function formatearPrecio(precio) {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precio);
}

// Previene XSS: convierte caracteres peligrosos (<, >, ", &) en entidades HTML.
// Úsala siempre que insertes datos del servidor dentro de HTML.
function escapeHtml(texto) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(String(texto)));
    return div.innerHTML;
}
