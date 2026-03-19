// =============================================================
// app.js — TechNova Store Frontend
// Conecta con la API REST en localhost:8080
// =============================================================

const API_URL = 'http://localhost:8080/api';

// Productos en memoria para filtrar sin re-petición
let todosLosProductos = [];

// Carrito
let carrito = [];

// SKUs anclados arriba como destacados
const DESTACADOS_SKUS = ['AUR-Z23C', 'REL-SF4O', 'PAN-EDXG'];

// =============================================================
// INICIO
// =============================================================
document.addEventListener('DOMContentLoaded', () => {
    cargarProductos();
    iniciarFiltros();
    iniciarBuscador();
    iniciarLogin();
    iniciarNavbarScroll();
    restaurarSesion();
});

// =============================================================
// NAVBAR — ocultar al bajar, mostrar al subir
// =============================================================
function iniciarNavbarScroll() {
    const navbar = document.getElementById('navbar');
    let lastY   = window.scrollY;
    let ticking = false;

    window.addEventListener('scroll', () => {
        if (!ticking) {
            requestAnimationFrame(() => {
                const currentY = window.scrollY;
                navbar.classList.toggle('scrolled', currentY > 10);
                if (currentY > lastY && currentY > 80) {
                    navbar.classList.add('navbar-hidden');
                } else {
                    navbar.classList.remove('navbar-hidden');
                }
                lastY   = currentY;
                ticking = false;
            });
            ticking = true;
        }
    }, { passive: true });
}

// =============================================================
// SESIÓN — restaura si ya estaba logueado
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

        const productos   = await respuesta.json();
        todosLosProductos = productos;

        spinner.classList.add('d-none');
        renderizarDestacados();
        renderizarProductos(productos);

        // Abrir detalle si la URL trae ?sku=XXX
        const sku = new URLSearchParams(window.location.search).get('sku');
        if (sku) abrirDetalle(sku);

    } catch (error) {
        spinner.classList.add('d-none');
        errorMsg.classList.remove('d-none');
        console.error('Error al cargar productos:', error);
    }
}

// =============================================================
// DESTACADOS — 3 cards grandes arriba con badge "Destacado"
// =============================================================
function renderizarDestacados() {
    const contenedor = document.getElementById('destacados-container');
    if (!contenedor) return; // no está en tienda.html
    const destacados = DESTACADOS_SKUS
        .map(sku => todosLosProductos.find(p => p.sku === sku))
        .filter(Boolean);

    if (destacados.length === 0) {
        contenedor.innerHTML = '';
        return;
    }

    contenedor.innerHTML =
        '<div class="destacados-grid">' +
        destacados.map(p => `
            <div class="card-producto card-grande ${p.stock === 0 ? 'agotado' : ''}"
                 onclick="abrirDetalle('${p.sku}')">
                <div class="card-img-area">
                    <span class="badge-featured">Featured</span>
                    <img src="http://localhost:8080/imagenes/${p.imagen}"
                         alt="${p.nombre}"
                         onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                    <div class="card-img-fallback" style="display:none">${iconoCategoria(p.categoria)}</div>
                    ${p.stock === 0 ? '<span class="badge-agotado">Agotado</span>' : ''}
                    ${p.stock > 0 && p.stock <= 5 ? '<span class="badge-poco-stock">¡Queda poco!</span>' : ''}
                </div>
                <div class="card-info">
                    <span class="card-nombre">${p.nombre}</span>
                    <span class="card-precio ${p.stock === 0 ? 'muted' : ''}">${formatearPrecio(p.precio)}</span>
                </div>
            </div>`
        ).join('') +
        '</div>';
}

// =============================================================
// CATÁLOGO — grid de 4 por fila, estilo limpio
// =============================================================
function renderizarProductos(productos) {
    const contenedor = document.getElementById('catalogo-container');
    contenedor.innerHTML = '';

    // Si hay filtro activo: ocultar destacados
    const categoriaActiva = document.querySelector('.filtro-pill.active')?.dataset.categoria;
    const hayFiltro = !!categoriaActiva;
    const destCont = document.getElementById('destacados-container');
    if (destCont) destCont.style.display = hayFiltro ? 'none' : '';
    if (productos.length === 0) {
        contenedor.innerHTML = `
            <div class="col-12 text-center py-5">
                <p style="font-family:var(--font-mono);color:var(--gray-text);font-size:0.85rem;">
                    No hay productos en esta categoría.
                </p>
            </div>`;
        return;
    }

    productos.forEach(producto => {
        const agotado = producto.stock === 0;
        const pocoStock = producto.stock > 0 && producto.stock <= 5;
        const col = document.createElement('div');
        col.className = 'col-6 col-md-4 col-lg-3';

        col.innerHTML = `
            <div class="card-producto ${agotado ? 'agotado' : ''}" onclick="abrirDetalle('${producto.sku}')">
                <div class="card-img-area">
                    <img src="http://localhost:8080/imagenes/${producto.imagen}"
                         alt="${producto.nombre}"
                         onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                    <div class="card-img-fallback" style="display:none">${iconoCategoria(producto.categoria)}</div>
                    ${agotado ? '<span class="badge-agotado">Agotado</span>' : ''}
                    ${pocoStock ? '<span class="badge-poco-stock">¡Queda poco!</span>' : ''}
                </div>
                <div class="card-info">
                    <span class="card-nombre">${producto.nombre}</span>
                    <span class="card-precio ${agotado ? 'muted' : ''}">${formatearPrecio(producto.precio)}</span>
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
// FILTROS — en memoria, sin re-petición
// =============================================================
function iniciarFiltros() {
    const pills = document.querySelectorAll('.filtro-pill');
    pills.forEach(pill => {
        pill.addEventListener('click', () => {
            pills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            const categoria = pill.dataset.categoria;
            const filtrados = categoria
                ? todosLosProductos.filter(p => p.categoria === categoria)
                : todosLosProductos;
            renderizarProductos(filtrados);
        });
    });
}

// =============================================================
// BUSCADOR — filtra en tiempo real
// =============================================================
function iniciarBuscador() {
    const wrapper = document.getElementById('buscador-wrapper');
    const btn     = document.getElementById('buscador-btn');
    const input   = document.getElementById('buscador');

    btn.addEventListener('click', () => {
        const abierto = wrapper.classList.toggle('activo');
        if (abierto) {
            input.focus();
        } else {
            input.value = '';
            filtrarYRenderizar('');
        }
    });

    input.addEventListener('blur', () => {
        if (input.value === '') wrapper.classList.remove('activo');
    });

    input.addEventListener('input', () => {
        filtrarYRenderizar(input.value.trim().toLowerCase());
    });
}

function filtrarYRenderizar(texto) {
    const categoriaActiva = document.querySelector('.filtro-pill.active').dataset.categoria;
    const filtrados = todosLosProductos.filter(p => {
        const coincideCategoria = categoriaActiva ? p.categoria === categoriaActiva : true;
        const coincideTexto     = p.nombre.toLowerCase().includes(texto);
        return coincideCategoria && coincideTexto;
    });
    renderizarProductos(filtrados);
}

// =============================================================
// CARRITO
// =============================================================
function anadirAlCarrito(sku, nombre, precio) {
    const existente = carrito.find(i => i.sku === sku);
    if (existente) {
        existente.cantidad++;
    } else {
        carrito.push({ sku, nombre, precio, cantidad: 1 });
    }
    actualizarCarritoUI();
    mostrarToast(`"${nombre}" añadido al carrito`);
}

function quitarDelCarrito(sku) {
    carrito = carrito.filter(i => i.sku !== sku);
    actualizarCarritoUI();
}

function cambiarCantidad(sku, delta) {
    const item = carrito.find(i => i.sku === sku);
    if (!item) return;
    item.cantidad += delta;
    if (item.cantidad <= 0) quitarDelCarrito(sku);
    else actualizarCarritoUI();
}

function actualizarCarritoUI() {
    const total   = carrito.reduce((s, i) => s + i.cantidad, 0);
    const countEl = document.getElementById('carrito-count');
    countEl.textContent = total;
    countEl.classList.toggle('d-none', total === 0);
    renderizarCarritoModal();
}

function renderizarCarritoModal() {
    const body    = document.getElementById('carrito-body');
    const totalEl = document.getElementById('carrito-total');

    if (carrito.length === 0) {
        body.innerHTML = '<p class="carrito-vacio">Tu carrito está vacío.</p>';
        totalEl.textContent = '';
        return;
    }

    body.innerHTML = carrito.map(item => `
        <div class="carrito-item">
            <div class="carrito-item-nombre">${item.nombre}</div>
            <div class="carrito-item-controles">
                <button onclick="cambiarCantidad('${item.sku}', -1)">−</button>
                <span>${item.cantidad}</span>
                <button onclick="cambiarCantidad('${item.sku}', 1)">+</button>
            </div>
            <div class="carrito-item-precio">${formatearPrecio(item.precio * item.cantidad)}</div>
            <button class="carrito-item-borrar" onclick="quitarDelCarrito('${item.sku}')">×</button>
        </div>`).join('');

    const totalImporte = carrito.reduce((s, i) => s + i.precio * i.cantidad, 0);
    totalEl.innerHTML = `Total: <strong>${formatearPrecio(totalImporte)}</strong>`;
}

function mostrarToast(mensaje) {
    const toast   = document.getElementById('carrito-toast');
    const msgSpan = document.getElementById('carrito-toast-msg');
    msgSpan.textContent = mensaje;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2600);
}

// =============================================================
// LOGIN — POST /api/login
// =============================================================
function iniciarLogin() {
    const btnLogin  = document.getElementById('btn-login');
    const btnLogout = document.getElementById('btn-logout');

    btnLogin.addEventListener('click', async () => {
        const email    = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;
        const errorDiv = document.getElementById('login-error');

        errorDiv.classList.add('d-none');
        errorDiv.textContent = '';

        if (!email || !password) {
            errorDiv.textContent = 'Rellena todos los campos.';
            errorDiv.classList.remove('d-none');
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

            if (!respuesta.ok) throw new Error(`HTTP ${respuesta.status}`);

            const datos = await respuesta.json();

            sessionStorage.setItem('nombre', datos.nombre || datos.email || email);
            sessionStorage.setItem('rol',    datos.rol);
            sessionStorage.setItem('id',     datos.id);

            bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
            mostrarUsuarioEnNavbar(datos.nombre || datos.email || email, datos.rol);

        } catch (error) {
            errorDiv.textContent = 'Credenciales incorrectas. Inténtalo de nuevo.';
            errorDiv.classList.remove('d-none');
            console.error('Error en login:', error);
        } finally {
            btnLogin.textContent = 'Entrar';
            btnLogin.disabled    = false;
        }
    });

    ['login-email', 'login-password'].forEach(id => {
        document.getElementById(id).addEventListener('keydown', e => {
            if (e.key === 'Enter') btnLogin.click();
        });
    });

    btnLogout.addEventListener('click', () => {
        sessionStorage.clear();
        document.getElementById('nav-user-item').classList.add('d-none');
        document.getElementById('nav-admin-item').classList.add('d-none');
        document.getElementById('nav-login-item').classList.remove('d-none');
    });
}

// =============================================================
// NAVBAR — actualiza tras login
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
// DETALLE — modal con info completa del producto
// =============================================================
function abrirDetalle(sku) {
    const producto = todosLosProductos.find(p => p.sku === sku);
    if (!producto) return;

    const agotado   = producto.stock === 0;
    const stockBajo = producto.stock > 0 && producto.stock <= 5;

    const img      = document.getElementById('detalle-img');
    const fallback = document.getElementById('detalle-img-fallback');
    img.src = `http://localhost:8080/imagenes/${producto.imagen}`;
    img.style.display    = 'block';
    fallback.style.display = 'none';
    img.onerror = () => {
        img.style.display      = 'none';
        fallback.style.display = 'flex';
        fallback.innerHTML     = iconoCategoria(producto.categoria);
    };

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

    document.getElementById('detalle-badge-agotado').classList.toggle('d-none', !agotado);
    document.getElementById('detalle-badge-poco-stock').classList.toggle('d-none', !stockBajo);

    const btn = document.getElementById('detalle-btn-carrito');
    btn.disabled    = agotado;
    btn.textContent = agotado ? 'Sin stock' : '+ Añadir al carrito';
    btn.replaceWith(btn.cloneNode(true));
    const btnNuevo = document.getElementById('detalle-btn-carrito');
    if (!agotado) {
        btnNuevo.addEventListener('click', () => {
            anadirAlCarrito(producto.sku, producto.nombre, producto.precio);
        });
    }

    new bootstrap.Modal(document.getElementById('detalleModal')).show();
}

// =============================================================
// UTILIDADES
// =============================================================
function formatearPrecio(precio) {
    return new Intl.NumberFormat('es-ES', {
        style: 'currency', currency: 'EUR'
    }).format(precio);
}
