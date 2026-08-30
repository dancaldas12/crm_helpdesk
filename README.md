# ServiClient CRM Helpdesk

> Aplicación web monolítica de gestión de clientes, mesa de ayuda y renovaciones, desarrollada con **Java 21 + Spring Boot 3 + Thymeleaf + Spring Security + Spring Data JPA**.

---

## 📋 Descripción

**ServiClient** es un CRM académico orientado a empresas de servicios B2B. Permite gestionar el ciclo de vida del cliente: desde el registro de la empresa y onboarding, hasta el seguimiento de tickets de soporte, control de SLA, encuestas de satisfacción (CSAT) y gestión de renovaciones de contratos.

### Módulos principales

| Módulo | Descripción |
|---|---|
| 🔐 Autenticación | Login + Wizard de registro en 3 pasos (Empresa → Admin → Configuración) |
| 👥 Clientes | Listado con Health Score, filtros y Vista 360° por tabs |
| 🎫 Helpdesk | Mesa de ayuda con KPIs, conversación, notas internas y control de SLA |
| 🔄 Renovaciones | Panel por rangos de vencimiento, alertas de riesgo y seguimientos |
| 📊 Health Score | Motor de cálculo automático (0–100) basado en CSAT, SLA y renovación |

---

## 🛠 Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3.3 |
| Vista | Thymeleaf 3.1 |
| Persistencia | Spring Data JPA + Hibernate 6 |
| Seguridad | Spring Security 6 |
| BD Desarrollo | H2 in-memory (compatible MySQL) |
| BD Producción | MySQL 8+ |
| Build | Apache Maven 3.9+ |
| Utilidades | Lombok |

---

## 🏗 Arquitectura

Monolito en capas estrictas, sin microservicios ni frameworks de arquitectura adicionales:

```
com.serviclient.crm
├── config/
│   ├── DataInitializer.java          ← Datos demo al arrancar
│   └── security/
│       ├── SecurityConfig.java
│       ├── CustomUserDetails.java
│       └── CustomUserDetailsService.java
├── controller/                        ← Spring MVC (@Controller)
│   ├── AuthController.java
│   ├── ClienteController.java
│   ├── TicketController.java
│   ├── RenovacionController.java
│   └── GlobalControllerAdvice.java
├── service/                           ← Lógica de negocio (@Service)
│   ├── EmpresaService.java
│   ├── UsuarioService.java
│   ├── ClienteService.java
│   ├── HealthScoreService.java
│   ├── TicketService.java
│   └── RenovacionService.java
├── repository/                        ← Spring Data JPA (@Repository)
├── entity/                            ← Entidades JPA (@Entity)
│   └── enums/
├── dto/                               ← Objetos de transferencia de datos
└── ServiClientApplication.java
```

### Flujo de una petición

```
Browser → Controller → Service → Repository → H2 / MySQL
                   ↑
           Thymeleaf (vista)
```

---

## ⚙️ Requisitos Previos

- **Java 21** — [Microsoft OpenJDK 21](https://learn.microsoft.com/es-es/java/openjdk/download) o cualquier distribución JDK 21+
- **Apache Maven 3.9+** — [Descargar](https://maven.apache.org/download.cgi)
- **MySQL 8+** *(solo para perfil de producción — en desarrollo usa H2)*

---

## 🚀 Ejecución Rápida (Desarrollo con H2)

```bash
# Clonar o ubicarse en el directorio del proyecto
cd crm_helpdesk

# Ejecutar con perfil dev (H2 in-memory, no requiere MySQL)
mvn spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

### En Windows (si Maven no está en el PATH global)

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\DANNY\apache-maven-3.9.9\bin;$env:PATH"
mvn spring-boot:run
```

---

## 🗄️ Base de Datos

### Perfil `dev` (por defecto)

Usa **H2 in-memory** con modo de compatibilidad MySQL. La base de datos se recrea automáticamente en cada arranque.

- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:crm_helpdesk_db`
  - Usuario: `SA` | Contraseña: *(vacío)*

### Perfil `mysql` (producción / local con MySQL)

1. Crear la base de datos en MySQL:

```sql
CREATE DATABASE crm_helpdesk CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Editar `src/main/resources/application-mysql.yml` con tus credenciales.

3. Ejecutar con el perfil activo:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

## 🔑 Credenciales Demo

Al arrancar en perfil `dev`, el `DataInitializer` carga datos de demostración automáticamente.

| Email | Contraseña | Rol |
|---|---|---|
| `admin@serviclient.com` | `admin123` | Administrador |

---

## 🗺 Endpoints Disponibles

| Ruta | Descripción |
|---|---|
| `GET /login` | Pantalla de inicio de sesión |
| `GET /registro/paso-1` | Wizard Paso 1 — Datos de Empresa |
| `GET /registro/paso-2` | Wizard Paso 2 — Usuario Administrador |
| `GET /registro/paso-3` | Wizard Paso 3 — Configuración SLA/CSAT/Health Score |
| `GET /clientes` | Listado de clientes con Health Score |
| `GET /clientes/{id}` | Vista 360° del cliente |
| `GET /clientes/nuevo` | Formulario de nuevo cliente |
| `POST /clientes/{id}/contactos` | Agregar contacto al cliente |
| `GET /tickets` | Mesa de Ayuda — listado con KPIs |
| `GET /tickets/{id}` | Detalle de ticket con conversación y SLA |
| `GET /tickets/nuevo` | Formulario de nuevo ticket |
| `POST /tickets/{id}/mensajes` | Enviar mensaje o nota interna |
| `POST /tickets/{id}/asignar` | Reasignar agente |
| `POST /tickets/{id}/cambiar-estado` | Cambiar estado/prioridad |
| `POST /tickets/{id}/cerrar` | Cerrar ticket (dispara CSAT) |
| `GET /renovaciones` | Panel de renovaciones con alertas |
| `POST /renovaciones/seguimiento` | Registrar seguimiento de renovación |

---

## 🧪 Pruebas

```bash
# Ejecutar toda la suite de pruebas
mvn clean test
```

Resultado esperado: **20 tests — 0 fallos — 0 errores**

| Clase de Test | Tipo | Pruebas |
|---|---|---|
| `ServiClientApplicationTests` | Integración | Contexto Spring Boot + DataInitializer |
| `SecurityAndAuthControllerTest` | Integración (MockMvc) | Login, Wizard 3 pasos, autorización por roles |
| `JpaRepositoriesTest` | Persistencia (`@DataJpaTest`) | Repositorios, filtros JPA, ordenamientos y agregaciones |
| `HealthScoreServiceTest` | Unitario (Mockito) | Cálculo de puntaje y factores de riesgo |
| `TicketServiceTest` | Unitario (Mockito) | SLA, mensajería, notas internas, cierre con CSAT y auditoría |

---

## 🧩 Modelo de Dominio

```
Empresa ──< Usuario (Rol: ADMIN | AGENTE)
Empresa ──< Cliente ──< Contacto
Cliente ──< Ticket ──< TicketMensaje
                    └─< TicketHistorial
                    └── EncuestaSatisfaccion
Cliente ──< Renovacion ──< RenovacionSeguimiento
```

### Enums del dominio

| Enum | Valores |
|---|---|
| `Rol` | `ADMIN`, `AGENTE` |
| `EstadoCliente` | `SALUDABLE`, `OBSERVACION`, `EN_RIESGO` |
| `EstadoTicket` | `ABIERTO`, `EN_PROCESO`, `PENDIENTE`, `RESUELTO`, `CERRADO` |
| `PrioridadTicket` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` |
| `CategoriaTicket` | `INFRAESTRUCTURA`, `SOFTWARE`, `FACTURACION`, `ADMINISTRACION`, `SOPORTE_TECNICO` |
| `EstadoRenovacion` | `PENDIENTE`, `EN_NEGOCIACION`, `RENOVADO`, `NO_REALIZADO` |

---

## 📁 Estructura del Proyecto

```
crm_helpdesk/
├── pom.xml
├── README.md
├── prototipo/                         ← Diseños de referencia visual
│   ├── login/
│   ├── clientes/
│   ├── cliente360/
│   ├── helpdesk/
│   └── renovaciones/
└── src/
    ├── main/
    │   ├── java/com/serviclient/crm/
    │   └── resources/
    │       ├── application.yml        ← Perfil dev activo por defecto
    │       ├── application-dev.yml    ← H2 in-memory
    │       ├── application-mysql.yml  ← MySQL 8
    │       ├── static/
    │       │   ├── css/styles.css
    │       │   └── js/app.js
    │       └── templates/
    │           ├── layout/base.html
    │           ├── auth/
    │           ├── clientes/
    │           ├── helpdesk/
    │           └── renovaciones/
    └── test/
        └── java/com/serviclient/crm/
```

---

## 📐 Principios Aplicados

- **SOLID** — Separación clara de responsabilidades por capa
- **DRY** — Layout maestro Thymeleaf reutilizable en todas las vistas
- **Simplicidad académica** — Sin microservicios, Clean Architecture ni dependencias innecesarias
- **Seguridad** — CSRF habilitado, contraseñas con BCrypt, rutas protegidas por rol

---

## 📄 Licencia

Proyecto académico — Libre uso educativo.
