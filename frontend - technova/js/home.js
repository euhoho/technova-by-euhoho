// =============================================================
// home.js — TechNova Store · Página de inicio (index.html)
// =============================================================

const API_URL         = 'http://localhost:8080/api';
const DESTACADOS_SKUS = ['AUR-Z23C', 'REL-SF4O', 'PAN-EDXG'];

// =============================================================
// ARRANQUE
// =============================================================
document.addEventListener('DOMContentLoaded', () => {
    iniciarNavbarScroll();
    restaurarSesion();   // muestra el nombre si ya estaba logueado
    iniciarLogin();
    cargarDestacados();  // carga los 3 productos ancla desde la API
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
                const y = window.scrollY;
                navbar.classList.toggle('scrolled',      y > 10);
                navbar.classList.toggle('navbar-hidden', y > lastY && y > 80);
                lastY   = y;
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

function mostrarUsuarioEnNavbar(nombre, rol) {
    document.getElementById('nav-login-item').classList.add('d-none');
    document.getElementById('nav-user-item').classList.remove('d-none');
    document.getElementById('nav-avatar').textContent = nombre; // textContent → anti-XSS

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
            .filter(Boolean); // descarta los SKUs que no existan en la API

        if (!destacados.length) return;

        const iconos = { AURICULARES: '🎧', PANTALLAS: '🖥️', RELOJES: '⌚', MOVILES: '📱' };

        // SEGURIDAD: usamos escapeHtml() para los datos del servidor → anti-XSS
        contenedor.innerHTML =
            '<div class="destacados-grid">' +
            destacados.map(p => `
                <div class="card-producto card-grande ${p.stock === 0 ? 'agotado' : ''}"
                     onclick="abrirDetalleHome('${p.sku}')">
                    <div class="card-img-area">
                        <span class="badge-featured">Featured</span>
                        <img src="http://localhost:8080/imagenes/${p.imagen}"
                             alt="${escapeHtml(p.nombre)}"
                             onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                        <div class="card-img-fallback" style="display:none">
                            <span style="font-size:2.5rem">${iconos[p.categoria] || '📦'}</span>
                        </div>
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
        const emailInput    = document.getElementById('login-email');
        const passwordInput = document.getElementById('login-password');
        const email         = emailInput.value.trim();
        const password      = passwordInput.value;
        const errorDiv      = document.getElementById('login-error');

        errorDiv.classList.add('d-none');
        errorDiv.textContent = '';

        // Validación: campos obligatorios
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

            // 401 = credenciales incorrectas
            if (respuesta.status === 401) {
                errorDiv.textContent = 'Credenciales incorrectas. Inténtalo de nuevo.';
                errorDiv.classList.remove('d-none');
                return;
            }

            // Cualquier otro error HTTP (500, etc.)
            if (!respuesta.ok) throw new Error(`HTTP ${respuesta.status}`);

            const datos = await respuesta.json();

            sessionStorage.setItem('nombre', datos.nombre || datos.email || email);
            sessionStorage.setItem('rol',    datos.rol);
            sessionStorage.setItem('id',     datos.id);

            bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
            mostrarUsuarioEnNavbar(datos.nombre || datos.email || email, datos.rol);

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

    // Enter en los campos del formulario
    ['login-email', 'login-password'].forEach(id => {
        document.getElementById(id).addEventListener('keydown', e => {
            if (e.key === 'Enter') btnLogin.click();
        });
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
