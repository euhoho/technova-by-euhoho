<div align="center">

![TechNova Banner](banner.png)

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.8-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![Gson](https://img.shields.io/badge/Gson-2.10.1-yellow?style=flat-square)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple?style=flat-square&logo=bootstrap)
![Estado](https://img.shields.io/badge/Estado-En%20desarrollo-orange?style=flat-square)

</div>

---

## ¿QUÉ ES TECHNOVA?

TechNova es un **proyecto académico** que simula una tienda online de productos tecnológicos.

Este repositorio contiene tanto el **backend** como el **frontend** de la aplicación, que gestiona productos, usuarios, pedidos e inventario.

El objetivo final es una tienda completamente funcional con panel de administración, sistema de pedidos en tiempo real y control de stock automatizado.

---

## VISTA PREVIA

https://github.com/user-attachments/assets/48506096-eaf5-442e-b4d6-9a808b4a13d2

*Próximamente*

---

## STACK

| Capa | Tecnología |
|---|---|
| Lenguaje backend | Java 21 |
| Framework backend | Spring Boot 3.3.8 |
| Base de datos | MySQL 8.0 |
| Serialización JSON | Gson 2.10.1 |
| Gestión de dependencias | Maven |
| Frontend | HTML5 + Bootstrap 5.3 + Vanilla JS |

---

## ESTRUCTURA
##### *(actualizada en entrega 5)*
```
backend - technova/
├── src/
│   ├── main/
│   │   ├── java/com/grupo3/technova/
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── ProductoController.java
│   │   │   │   ├── UsuarioController.java
│   │   │   │   └── PedidoController.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── ProductoRequest.java
│   │   │   │   └── PedidoRequest.java
│   │   │   ├── model/
│   │   │   │   ├── enums/
│   │   │   │   │   ├── EnumRol.java
│   │   │   │   │   └── EnumCategoria.java
│   │   │   │   ├── Jsonable.java
│   │   │   │   ├── Producto.java
│   │   │   │   ├── Usuario.java
│   │   │   │   └── Pedido.java
│   │   │   ├── repository/
│   │   │   │   ├── ProductoRepository.java
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   └── PedidoRepository.java
│   │   │   └── DemoApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/imagenes/
│   └── test/
├── pom.xml
└── mvnw

frontend - technova/
└── src/
    ├── index.html
    ├── tienda.html
    ├── css/
    │   └── styles.css
    ├── js/
    │   ├── app.js
    │   └── home.js
    └── images/

sql - technova/
├── db_technova.sql
└── procedures_technova.sql
```

---

## Endpoints de la API

### Productos

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/productos` | Lista todos los productos |
| `GET` | `/api/productos?categoria=PERIFERICOS` | Filtra por categoría |
| `POST` | `/api/productos` | Crea un producto nuevo (solo ADMINISTRADOR) |

**Categorías disponibles:** `PERIFERICOS`, `COMPONENTES`, `REDES`, `SOFTWARE`

**Body para crear producto** (requiere cabecera `user-role: ADMINISTRADOR`):
```json
{
  "sku": "PER-TEST01",
  "nombre": "Nombre del producto",
  "descripcion": "Descripción del producto",
  "precio": 99.99,
  "stock": 10,
  "categoria": "PERIFERICOS",
  "imagen": "imagen.jpg"
}
```

---

### Usuarios

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/usuarios` | Lista todos los usuarios |
| `POST` | `/api/login` | Autenticación de usuario |

**Body del login:**
```json
{
  "email": "javiervs@gmail.com",
  "password": "uyuyuyuy124.S"
}
```

**Respuesta correcta:**
```json
{
  "status": "ok",
  "id": 7,
  "email": "javiervs@gmail.com",
  "rol": "CLIENTE"
}
```

---

### Pedidos

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/pedidos` | Lista todos los pedidos |
| `GET` | `/api/pedidos?estado=ENVIADO` | Filtra por estado |
| `GET` | `/api/pedidos?fechaIni=2026-01-01&fechaFin=2026-12-31` | Filtra por fechas |
| `POST` | `/api/pedidos` | Crea un pedido nuevo |

**Body para crear pedido:**
```json
{
  "id_usuario": 7,
  "items": [
    { "id_producto": 1, "cantidad": 2 },
    { "id_producto": 5, "cantidad": 1 }
  ]
}
```

**Estados disponibles:** `CONFIRMADO`, `PREPARADO`, `ENVIADO`, `ENTREGADO`

---

## CÓMO EJECUTARLO

### Requisitos previos

- Java 21
- MySQL 8.0
- Maven
- VS Code con extensión Live Server (para el frontend)

### Pasos

**1. Clona el repositorio**
```bash
git clone https://github.com/euhoho/technova-by-euhoho.git
cd technova-by-euhoho
```

**2. Crea la base de datos**

Ejecuta en MySQL en este orden:
```
1. db_technova.sql           → crea las tablas e inserta los datos de prueba
2. procedures_technova.sql   → crea los procedimientos almacenados
```

**3. Configura la conexión**

Edita `backend - technova/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_technova
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
```

**4. Arranca el servidor**
```bash
cd backend\ -\ technova
mvn spring-boot:run
```

Si todo va bien verás en consola:
```
Conexión a MySQL establecida correctamente
```

La API estará disponible en `http://localhost:8080`

**5. Abre el frontend**

Abre `frontend - technova/src/index.html` con Live Server desde VS Code. La tienda estará disponible en `http://127.0.0.1:5500`

---

## Características técnicas destacadas

- **Transacciones completas** en la creación de pedidos — si algo falla, se hace rollback automático y la BD queda intacta
- **Bloqueo FOR UPDATE** para evitar condiciones de carrera al comprar el mismo producto simultáneamente
- **Stored procedures** para todas las consultas GET, sin SQL embebido en el código Java
- **CORS configurado** para integración con el frontend
- **Gson** para serialización JSON explícita, sin depender de la magia de Spring
- **PreparedStatement** en todas las consultas (sin riesgo de SQL injection)
- **Frontend dinámico** — ningún producto está escrito a mano en el HTML, todo se renderiza desde la API
- **Filtrado en memoria** — los filtros por categoría y el buscador operan sin nuevas peticiones al servidor
- **Sesión con sessionStorage** — el login persiste mientras el usuario no cierre el tab

---

## ROADMAP

#### Entregable 1 — Análisis
- [x] Definición de requisitos y funcionalidades
- [x] Identificación de roles (cliente / oficina / administración)
- [x] Casos de uso

#### Entregable 2 — Diseño
- [x] Modelo entidad–relación
- [x] Esquema MySQL
- [x] Arquitectura Frontend → API → BD

#### Entregable 3 — API REST
- [x] API con Spring Boot
- [x] Conexión segura a MySQL
- [x] Endpoints de productos (listado + filtro)
- [x] Endpoints de usuarios (listado + login)
- [x] Endpoints de pedidos (listado + filtros + creación)
- [x] Control automático de stock
- [x] Respuestas JSON

#### Entregable 4 — Autenticación y Roles
- [x] Hash de contraseñas (BCrypt)
- [x] Protección de endpoints por rol
- [x] Autenticación por cabecera (preparado para JWT en entregables futuros)
- [x] Evidencias de acceso por rol

#### Entregable 5 — Frontend
- [x] Frontend HTML/CSS/JS consumiendo la API
- [x] Catálogo de productos dinámico con Bootstrap 5
- [x] Filtrado por categoría y buscador en tiempo real
- [x] Login desde interfaz con actualización de navbar
- [x] Carrito de compra visual
- [x] Modal de detalle de producto

#### Entregable 6 — Integración y Seguridad
- [ ] Validaciones de entrada
- [ ] Control global de errores
- [ ] Lógica de negocio completa integrada
- [ ] Creación y consulta de pedidos desde el frontend

#### Entregable 7 — Preparación despliegue
- [ ] Documento de requisitos técnicos
- [ ] Guía de instalación
- [ ] Checklist post-despliegue

#### Entregable 8 — Cierre
- [ ] Pruebas finales
- [ ] Memoria técnica
- [ ] Manual de uso
- [ ] Aplicación estable para defensa

---

## LICENCIA

Distribuido bajo licencia MIT.
Consulta el archivo `LICENSE` para más información.

---

<div align="center">
  <sub>
    <strong>TechNova</strong> — Aplicación web de tienda online · DAM/DAW 2025/2026
    <br>
    Full-stack project (API REST + Frontend web)
    <br>
    Made with ❤️ by <a href="https://github.com/euhoho">euhoho</a>
  </sub>
</div>
