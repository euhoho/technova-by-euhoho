// =============================================================
// home.js — TechNova Store · Página de inicio
// =============================================================

const API_URL          = 'http://localhost:8080/api';
const DESTACADOS_SKUS  = ['AUR-Z23C', 'REL-SF4O', 'PAN-EDXG'];

let carrito = [];

// =============================================================
// INICIO
// =============================================================
document.addEventListener('DOMContentLoaded', () => {
    iniciarNavbarScroll();
    restaurarSesion();
    iniciarLogin();
    cargarDestacados();
});

// =============================================================
// NAVBAR — ocultar al bajar, mostrar al subir
// =============================================================
function iniciarNavbarScroll() {
    const navbar = document.getElementById('navbar');
    let lastY    = window.scrollY;
    let ticking  = false;

    window.addEventListener('scroll', () => {
        if (!ticking) {
            requestAnimationFrame(() => {
                const y = window.scrollY;
                navbar.classList.toggle('scrolled', y > 10);
                if (y > lastY && y > 80) {
                    navbar.classList.add('navbar-hidden');
                } else {
                    navbar.classList.remove('navbar-hidden');
                }
                lastY   = y;
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

function mostrarUsuarioEnNavbar(nombre, rol) {
    document.getElementById('nav-login-item').classList.add('d-none');
    document.getElementById('nav-user-item').classList.remove('d-none');
    document.getElementById('nav-avatar').textContent = nombre;

    if (rol === 'ADMINISTRADOR') {
        document.getElementById('nav-admin-item').classList.remove('d-none');
    }
}

// =============================================================
// DESTACADOS — carga los 3 productos ancla desde la API
// =============================================================
async function cargarDestacados() {
    const spinner    = document.getElementById('loading-spinner-home');
    const contenedor = document.getElementById('destacados-container');

    try {
        const respuesta = await fetch(`${API_URL}/productos`);
        if (!respuesta.ok) throw new Error(`HTTP ${respuesta.status}`);

        const todos = await respuesta.json();
        spinner.classList.add('d-none');

        const destacados = DESTACADOS_SKUS
            .map(sku => todos.find(p => p.sku === sku))
            .filter(Boolean);

        if (destacados.length === 0) return;

        const iconos = { AURICULARES: '🎧', PANTALLAS: '🖥️', RELOJES: '⌚', MOVILES: '📱' };

        contenedor.innerHTML =
            '<div class="destacados-grid">' +
            destacados.map(p => `
                <div class="card-producto card-grande ${p.stock === 0 ? 'agotado' : ''}"
                     onclick="abrirDetalleHome('${p.sku}')">
                    <div class="card-img-area">
                        <span class="badge-featured">Featured</span>
                        <img src="http://localhost:8080/imagenes/${p.imagen}"
                             alt="${p.nombre}"
                             onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                        <div class="card-img-fallback" style="display:none">
                            <span style="font-size:2.5rem">${iconos[p.categoria] || '📦'}</span>
                        </div>
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

    } catch (error) {
        spinner.classList.add('d-none');
        console.error('Error al cargar destacados:', error);
    }
}

// Al hacer clic en un producto de la home, redirige a la tienda con el SKU
function abrirDetalleHome(sku) {
    window.location.href = `tienda.html?sku=${sku}`;
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

    // Enter en los campos del formulario
    ['login-email', 'login-password'].forEach(id => {
        document.getElementById(id).addEventListener('keydown', e => {
            if (e.key === 'Enter') btnLogin.click();
        });
    });

    // Cerrar sesión
    btnLogout.addEventListener('click', () => {
        sessionStorage.clear();
        document.getElementById('nav-user-item').classList.add('d-none');
        document.getElementById('nav-admin-item').classList.add('d-none');
        document.getElementById('nav-login-item').classList.remove('d-none');
    });
}

// =============================================================
// UTILIDADES
// =============================================================
function formatearPrecio(precio) {
    return new Intl.NumberFormat('es-ES', {
        style: 'currency', currency: 'EUR'
    }).format(precio);
}
