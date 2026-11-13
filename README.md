# ServiClient CRM Helpdesk

> Aplicacion web monolitica de gestion de clientes, mesa de ayuda y renovaciones.
> **Java 21 · Spring Boot 3.3.3 · Thymeleaf · Spring Security · Spring Data JPA · MySQL 8**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg)](https://www.mysql.com/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D.svg)](http://localhost:8080/swagger-ui.html)
[![License](https://img.shields.io/badge/License-MIT%20Academico-lightgrey.svg)](#-licencia)
[![Guava](https://img.shields.io/badge/Google%20Guava-33%2B-yellow.svg)](https://github.com/google/guava)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-5%2B-red.svg)](https://poi.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?logo=docker&logoColor=white)](docker-compose.yml)

---

## Propósito del Sistema

**ServiClient CRM Helpdesk** es una plataforma web monolítica empresarial orientada a compañías de servicios **B2B** (*Business-to-Business*). Su objetivo central es gestionar de extremo a extremo el ciclo de vida del cliente corporativo: desde el registro multitenancy y onboarding de empresas, hasta el seguimiento de contratos, directorio de contactos, mesa de ayuda multicanal con acuerdos de nivel de servicio (SLA), encuestas de satisfacción (CSAT), motor analítico de salud del cliente (*Health Score*) y control de retención de contratos y ARR (*Annual Recurring Revenue*), complementado con un portal de autoservicio para clientes finales.

### Módulos Principales

| Módulo | Descripción Funcional |
|---|---|
| **Autenticación & Onboarding** | Inicio/cierre de sesión seguro y Wizard de registro en 3 pasos: Empresa ➔ Usuario Administrador ➔ Configuración de SLA, CSAT y Health Score. Incluye control de contraseña temporal obligatoria. |
| **Dashboard Interactivo** | Panel de control ejecutivo en tiempo real con filtros temporales (7d, 30d, 90d, 1y, todo), gráficos interactivos (Chart.js), balance de tickets, satisfacción CSAT y panel de contratos por vencer. |
| **Gestión de Clientes B2B** | Padrón de clientes con búsqueda predictiva, filtros por estado de salud, alta/edición/baja, exportación de reportes a Excel (.xlsx) y **Vista 360°** organizada en pestañas temáticas. |
| **Directorio de Contactos** | Gestión de interlocutores comerciales y técnicos, designación de contacto principal y generación/restablecimiento de credenciales de autoservicio. |
| **Mesa de Ayuda (Helpdesk)** | Gestión del ciclo de vida de tickets con numeración correlativa (`TK-XXXX`), cálculo dinámico de tiempos límite de primera respuesta y resolución (SLA), mensajería dual (pública y notas internas) y reasignación de agentes. |
| **Satisfacción del Cliente (CSAT)** | Generación automática de encuestas al resolver o cerrar tickets de soporte y registro de métricas de satisfacción por cliente. |
| **Panel de Renovaciones & ARR** | Monitoreo de contratos clasificados por urgencia de expiración (<15 días, 16-30 días, 31-60 días, >60 días), cálculo de ARR en riesgo y bitácora de seguimientos comerciales. |
| **Motor de Health Score** | Algoritmo determinista continuo (0 a 100) que califica la salud de cada cuenta evaluando casos críticos, violaciones de SLA, promedio de CSAT y cercanía a la renovación. |
| **Gestión de Equipo & Agentes** | Administración del equipo de atención y supervisión con roles (`ADMIN`, `SUPERVISOR`, `AGENTE`), activación/desactivación y reseteo de claves temporales. |
| **Portal de Autoservicio** | Acceso para clientes externos para creación de incidencias (`/portal/tickets/nuevo`) y seguimiento en línea de sus casos (`/portal/tickets/{id}`). |
| **Documentación OpenAPI 3.0** | Swagger UI interactivo (`/swagger-ui.html`) y especificación en JSON/YAML sincronizada con las anotaciones del controlador. |

---

## Stack Tecnológico

| Capa / Componente | Tecnología | Versión | Propósito en el Proyecto |
|---|---|---|---|
| **Lenguaje Base** | Java (OpenJDK / LTS) | 21 | Plataforma de ejecución moderna con Records, Pattern Matching y Switch Expressions |
| **Framework Principal** | Spring Boot | 3.3.3 | Núcleo de la aplicación web, inyección de dependencias y autoconfiguración |
| **Motor de Plantillas** | Thymeleaf + Extras | 3.1.2 / 3.3.3 | Renderizado del lado del servidor (SSR) con integración de directivas Spring Security 6 |
| **Persistencia & ORM** | Spring Data JPA + Hibernate | 3.3.3 / 6.5 | Mapeo objeto-relacional y consultas optimizadas sobre 26 entidades y 26 repositorios |
| **Seguridad Web** | Spring Security | 6.3.3 | Autenticación basada en sesiones, filtro de contraseñas temporales, roles y CSRF |
| **Base de Datos** | MySQL Server | 8.0+ | Base de datos relacional principal con motor InnoDB y juego de caracteres UTF-8 |
| **Base de Datos de Pruebas** | H2 Database | 2.2.x | Base de datos relacional en memoria para desarrollo local y pruebas de integración |
| **Contenedores & Orquestación** | Docker & Docker Compose | Multi-Stage / Compose v2+ | Dockerfile multi-stage (builder/runtime), MySQL 8 aislado, volúmenes persistentes y sincronización por healthcheck |
| **Documentación OpenAPI** | Springdoc OpenAPI UI | 2.6.0 | Generación automática de Swagger UI (`/swagger-ui.html`) y esquemas OpenAPI 3 |
| **Gestión de Construcción** | Apache Maven | 3.9+ | Ciclo de vida de compilación, empaquetado y generación de Javadoc |
| **Documentación de Código** | Maven Javadoc Plugin | 3.6.3 | Compilación del sitio Javadoc HTML con soporte nativo de Java 21 y UTF-8 |
| **Hojas de Cálculo** | Apache POI (poi-ooxml) | 5.2.5 | Generación en memoria y descarga de reportes Excel (.xlsx) de clientes |
| **Validación Defensiva** | Google Guava | 33.0.0-jre | Validación de invariantes con `Preconditions` (`checkNotNull`, `checkArgument`) |
| **Utilidades de Cadenas** | Apache Commons Lang3 | 3.14.0 | Operaciones auxiliares de validación y formateo de texto |
| **Generación de Código** | Project Lombok | 1.18.30 | Reducción de boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`) |
| **Registro de Eventos** | SLF4J con Logback | 1.5.12 | Logging estructurado para auditoría operativa y seguimiento de excepciones |

---

## Arquitectura de Software

El sistema sigue una arquitectura monolítica en capas estrictas (**N-Tier + Spring MVC**) desacoplada mediante interfaces de servicio:

```
com.serviclient.crm
├── config/                                # Configuración global del sistema
│   ├── DataInitializer.java               # Carga inicial de datos semilla en las 26 tablas
│   ├── OpenApiConfig.java                 # Configuración de Swagger UI / OpenAPI 3.0
│   └── security/                          # Seguridad Spring Security
│       ├── CustomUserDetails.java         # Principal autenticado con empresaId y rol
│       ├── CustomUserDetailsService.java  # Carga de credenciales desde UsuarioRepository
│       ├── CambioPasswordObligatorioFilter.java # Interceptor de cambio de clave temporal
│       └── SecurityConfig.java            # Reglas de rutas, sesiones, login y CSRF
├── controller/                            # Controladores Spring MVC (@Controller)
│   ├── AgenteController.java              # Gestión del equipo de soporte
│   ├── AuthController.java                # Login y Wizard de registro en 3 pasos
│   ├── CambioPasswordController.java      # Flujo de cambio de clave obligatoria
│   ├── ClienteController.java             # Padrón de clientes, Vista 360 y exportación
│   ├── DashboardController.java           # Tablero ejecutivo interactivo
│   ├── GlobalControllerAdvice.java        # Inyección de usuario y empresa a vistas
│   ├── PortalTicketController.java        # Creación y consulta de tickets por clientes
│   ├── RenovacionController.java          # Tablero de vencimientos y ARR en riesgo
│   ├── TicketController.java              # Mesa de ayuda, SLA, notas y cierre CSAT
│   └── portalusuario/
│       └── PortalUsuarioController.java   # Autenticación al portal de clientes
├── service/                               # Capa de negocio e interfaces públicas
│   ├── IClienteService.java / ClienteService.java
│   ├── ICuentaPortalService.java / CuentaPortalService.java
│   ├── IDashboardService.java / DashboardService.java
│   ├── IEmpresaService.java / EmpresaService.java
│   ├── IHealthScoreService.java / HealthScoreService.java
│   ├── IPortalTicketService.java / PortalTicketService.java
│   ├── IRenovacionService.java / RenovacionService.java
│   ├── ITicketService.java / TicketService.java
│   └── IUsuarioService.java / UsuarioService.java
├── repository/                            # 26 Repositorios Spring Data JPA
│   ├── ClienteRepository.java / TicketRepository.java / RenovacionRepository.java
│   ├── EmpresaRepository.java / UsuarioRepository.java / CuentaPortalRepository.java
│   └── ... (20 repositorios adicionales)
├── entity/                                # 26 Entidades JPA mapeadas a MySQL
│   ├── Cliente.java / Contacto.java / Ticket.java / Renovacion.java / etc.
│   └── enums/                             # 15 Enumeraciones de dominio (EstadoTicket, etc.)
├── dto/                                   # Objetos de transferencia y validación de formularios
└── ServiClientApplication.java            # Clase de inicio Spring Boot
```

### Patron de capas: Controller -> Interface -> Service -> Repository

Los controllers **nunca inyectan la clase concreta del servicio**, sino su interfaz:

```java
// CORRECTO - Inyeccion por interfaz (bajo acoplamiento)
@Autowired
private IClienteService clienteService;

// INCORRECTO - Inyeccion de la implementacion directa (alto acoplamiento)
// private ClienteService clienteService;
```

Esto aplica el principio de inversion de dependencias (SOLID - D) y permite usar mocks en pruebas sin modificar el controller.

---

## Librerias de Utilidad

### Google Guava

**Uso en el proyecto:** Validacion defensiva de argumentos en la capa de servicio.

```java
// En EmpresaService.java
import com.google.common.base.Preconditions;

Preconditions.checkNotNull(empresa, "La empresa no puede ser nula");
Preconditions.checkArgument(empresa.getId() != null, "El ID de empresa es requerido");
```

Se usa `Preconditions` de Guava para lanzar excepciones descriptivas de forma temprana (fail-fast) antes de ejecutar logica de negocio, evitando `NullPointerException` silenciosas.

---

### Logback (SLF4J)

**Uso en el proyecto:** Logging estructurado en servicios y controladores mediante `@Slf4j` de Lombok.

```java
// En servicios y controladores
@Slf4j
@Service
public class TicketService implements ITicketService {

    public void cerrarTicket(Long ticketId) {
        log.info("Cerrando ticket ID: {}", ticketId);
        log.warn("Ticket {} cerrado sin encuesta CSAT activa", ticketId);
        log.error("Error al cerrar ticket {}: {}", ticketId, e.getMessage());
    }
}
```

Logback es el backend de logging por defecto de Spring Boot. Registra eventos de negocio (creacion de tickets, cambios de estado, errores de SLA) de manera estructurada, sin `System.out.println`.

---

### Apache POI

**Uso en el proyecto:** Generacion dinamica de archivos Excel (.xlsx) para exportacion de datos de clientes.

```java
// En ClienteService.java
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public byte[] exportarClientesExcel(Long empresaId) {
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Clientes");
    // Se construyen filas con nombre, Health Score, estado, servicios activos...
    return baos.toByteArray(); // devuelto como descarga desde el controller
}
```

Permite descargar el listado completo de clientes a un archivo Excel desde `GET /clientes/exportar-excel`.

---

### Apache Commons Lang3

**Uso en el proyecto:** Utilidades genericas de manipulacion de Strings y objetos.

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
```

Proporciona metodos como `StringUtils.isBlank()`, `StringUtils.capitalize()` y `ObjectUtils.defaultIfNull()` para complementar las utilidades estandar de Java en validaciones y transformaciones dentro de los servicios.

---

## Base de Datos (MySQL)

El proyecto usa **MySQL 8** como base de datos principal (esquema `serviclient`).
El esquema completo esta en `CRM_MESA_AYUDA.sql` e incluye **25 tablas**.

### Tablas del esquema

| # | Tabla | Entidad JPA | Descripcion |
|---|---|---|---|
| 1 | empresas | Empresa | Empresa proveedora del CRM |
| 2 | roles | RolEntity | Roles personalizables por empresa |
| 3 | permisos | Permiso | Permisos granulares por modulo/accion |
| 4 | rol_permisos | (tabla join) | Relacion N:M entre roles y permisos |
| 5 | usuarios | Usuario | Agentes y administradores |
| 6 | servicios | Servicio | Catalogo de productos/servicios |
| 7 | clientes | Cliente | Clientes B2B con Health Score |
| 8 | contactos | Contacto | Contactos por cliente |
| 9 | clientes_servicios | ClienteServicio | Contratos activos de servicios |
| 10 | categorias_tickets | CategoriaTicketEntity | Categorias de tickets por empresa |
| 11 | configuracion_sla | ConfiguracionSla | Tiempos de SLA por prioridad |
| 12 | configuracion_csat | ConfiguracionCsat | Escala y mensaje CSAT |
| 13 | configuracion_health_score | ConfiguracionHealthScore | Pesos y rangos del Health Score |
| 14 | tickets | Ticket | Tickets de soporte (Mesa de ayuda) |
| 15 | ticket_mensajes | TicketMensaje | Conversacion del ticket |
| 16 | ticket_historial | TicketHistorial | Log de eventos del ticket |
| 17 | ticket_adjuntos | TicketAdjunto | Archivos adjuntos |
| 18 | renovaciones | Renovacion | Renovaciones de contrato |
| 19 | renovacion_seguimientos | RenovacionSeguimiento | Historial de seguimientos |
| 20 | encuestas_csat | EncuestaCsat | Encuestas CSAT enviadas |
| 21 | respuestas_csat | RespuestaCsat | Respuestas a encuestas CSAT |
| 22 | encuestas_satisfaccion | EncuestaSatisfaccion | Satisfaccion por ticket |
| 23 | health_score_historial | HealthScoreHistorial | Historico de puntaje de salud |
| 24 | interacciones | Interaccion | Reuniones, llamadas y seguimientos |
| 25 | auditoria | Auditoria | Log de acciones del sistema |

### Configuración de conexión y perfiles

La aplicación soporta dos perfiles principales de base de datos:

1. **Perfil `mysql` (Contenedores y Producción / Por Defecto):**
   Conexión a MySQL 8 mediante `src/main/resources/application-mysql.yml`. Los valores son configurables a través de variables de entorno y cuentan con valores predeterminados listos para usar:

   ```yaml
   spring:
     datasource:
       url: ${SPRING_DATASOURCE_URL:${DB_URL:jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:serviclient}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima&characterEncoding=UTF-8}}
       username: ${SPRING_DATASOURCE_USERNAME:${DB_USER:serviclient_user}}
       password: ${SPRING_DATASOURCE_PASSWORD:${DB_PASSWORD:serviclient_pass}}
     jpa:
       hibernate:
         ddl-auto: update
   ```

   **Variables de entorno soportadas:** `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_ROOT_PASSWORD`, `APP_PORT`, `SPRING_PROFILES_ACTIVE`.

2. **Perfil `dev` (Desarrollo Local en Memoria con H2):**
   Configurado en `src/main/resources/application-dev.yml`. Levanta una base de datos H2 en memoria (`jdbc:h2:mem:crm_helpdesk_db`) con consola web activa en `/h2-console`, ideal para probar o desarrollar sin requerir un servidor MySQL en ejecución.

---

## Requisitos Previos

### Opción A: Ejecución con Docker (Recomendada / Zero-Setup)
No requiere instalar Java, Maven ni MySQL en la máquina anfitriona:
| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Docker Desktop o Docker Engine + Compose | 20.10+ / Compose v2+ | https://www.docker.com/products/docker-desktop/ |

### Opción B: Ejecución Local Tradicional (Sin Docker)
| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Java JDK | 21 (LTS) | https://learn.microsoft.com/es-es/java/openjdk/download |
| Apache Maven | 3.9+ (o usar `./mvnw` incluido) | https://maven.apache.org/download.cgi |
| MySQL Server (opcional si usas H2) | 8.0+ | https://dev.mysql.com/downloads/mysql/ |

---

## Instalación y Ejecución

### Opción 1 - Despliegue con Docker & Docker Compose (Recomendada)

Permite clonar el repositorio y tener toda la arquitectura (Spring Boot 3 + Java 21 y MySQL 8) funcionando de inmediato sin instalar herramientas locales:

```bash
# 1. Clonar el repositorio y acceder a la carpeta
git clone <url-del-repositorio>
cd crm_helpdesk

# 2. (Opcional) Copiar y personalizar variables de entorno
cp .env.example .env

# 3. Construir y levantar los contenedores en segundo plano
docker compose up -d --build
```

**Características de la dockerización:**
- **Build Multi-stage:** Compila la aplicación en un contenedor efímero (`maven:3.9.9-eclipse-temurin-21-alpine`) aprovechando el caché de capas de dependencias, y empaqueta el artefacto final en una imagen JRE mínima y segura (`eclipse-temurin:21-jre-alpine`).
- **Seguridad:** La aplicación se ejecuta bajo un usuario no root (`appuser`).
- **Sincronización por Healthcheck:** Spring Boot espera a que MySQL 8 pase exitosamente su `healthcheck` (`mysqladmin ping`) antes de arrancar (`condition: service_healthy`), evitando errores de conexión al inicio.
- **Persistencia de Datos:** Los datos de MySQL se almacenan en el volumen administrado `crm_helpdesk_mysql_data`.
- **Compatibilidad con Apache POI:** Incluye librerías de fuentes en Alpine (`fontconfig`, `freetype`, `ttf-dejavu`) para la exportación de reportes Excel.

**Comandos útiles de Docker Compose:**
```bash
# Ver estado de los contenedores y healthchecks
docker compose ps

# Ver logs en tiempo real de la aplicación
docker compose logs -f app

# Ver logs de MySQL
docker compose logs -f mysql

# Detener los servicios conservando los datos
docker compose down

# Detener los servicios y eliminar los volúmenes (reinicio limpio de base de datos)
docker compose down -v
```

La aplicación queda disponible en **http://localhost:8080**

---

### Opción 2 - Desarrollo Local con H2 (Base de Datos en Memoria)

Si deseas desarrollar o probar cambios en tu máquina local sin levantar contenedores ni servidores MySQL externos, utiliza el perfil `dev`:

Con **Maven Wrapper**:
```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows (PowerShell / CMD)
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Con **Maven instalado**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- Aplicación: **http://localhost:8080**
- Consola H2: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:crm_helpdesk_db`, Usuario: `sa`, Contraseña: en blanco)

---

### Opción 3 - Script Rápido Automatizado (Windows con MySQL Local)

El proyecto cuenta con scripts de arranque en la carpeta `bin/` que configuran el entorno Java 21 y Maven, levantan el perfil MySQL y abren automáticamente el navegador en `http://localhost:8080`:

Desde **CMD**:
```cmd
bin\iniciar.bat
```

Desde **PowerShell**:
```powershell
.\bin\iniciar.ps1
```

**Flujo interno del script:**
1. Configura `JAVA_HOME` apuntando al JDK 21 y añade Maven al `PATH`.
2. Si existe el empaquetado `target\crm-helpdesk-1.0.0.jar`, ejecuta:
   ```cmd
   java -jar target\crm-helpdesk-1.0.0.jar --spring.profiles.active=mysql
   ```
3. En caso contrario, inicia mediante el plugin de Spring Boot:
   ```cmd
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

### Opción 4 - Ejecución Directa con Maven (MySQL Local)

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

### Opción 5 - Compilación y Ejecución del JAR Empaquetado

```bash
# Compilar y empaquetar
mvn clean package -DskipTests

# Ejecutar el artefacto generado con perfil MySQL
java -jar target/crm-helpdesk-1.0.0.jar --spring.profiles.active=mysql

# O ejecutar con perfil H2 (dev)
java -jar target/crm-helpdesk-1.0.0.jar --spring.profiles.active=dev
```

La aplicación quedará disponible en **http://localhost:8080**

> **Poblado Automático de Datos:** Al iniciar por primera vez, `DataInitializer` detecta si la base de datos se encuentra vacía y carga automáticamente la información semilla de prueba en las 26 tablas del sistema. Los scripts SQL de respaldo se encuentran disponibles en `sql/CRM_MESA_AYUDA.sql` y `sql/CRM_MESA_AYUDA_DATOS_DEMO.sql`.

---

## Credenciales de Demostración

| Correo Electrónico | Contraseña | Rol Asignado |
|---|---|---|
| `admin@serviclient.com` | `admin123` | Administrador General |
| `carlos.ruiz@serviclient.com` | `admin123` | Agente de Soporte |
| `ana.garcia@serviclient.com` | `admin123` | Agente de Soporte |
| `laura.mendoza@serviclient.com` | `admin123` | Supervisor Customer Success |

---

## Documentación de APIs (Swagger / OpenAPI 3.0)

Con la aplicación en ejecución:

| Recurso | URL Local | Descripción |
|---|---|---|
| **Swagger UI Interactivo** | http://localhost:8080/swagger-ui.html | Exploración interactiva y prueba de endpoints |
| **OpenAPI Spec (JSON)** | http://localhost:8080/v3/api-docs | Especificación OpenAPI 3.0 en formato JSON |
| **OpenAPI Spec (YAML)** | http://localhost:8080/v3/api-docs.yaml | Especificación OpenAPI 3.0 en formato YAML |

### Tags de la API Documentados:
- **Autenticación:** Login, logout y wizard de registro en 3 pasos.
- **Dashboard:** Panel interactivo general con métricas, gráficos y KPIs.
- **Clientes:** Padrón, filtros, creación, edición, vista 360 y exportación Excel.
- **Helpdesk / Tickets:** Bandeja de soporte, mensajería, notas privadas y control de SLA.
- **Renovaciones:** Panel de vencimientos, ARR y registro de seguimientos.
- **Agentes:** Administración del equipo de agentes y supervisores.
- **Perfil de Usuario:** Cambio obligatorio de contraseña temporal.
- **Portal de Clientes:** Autoservicio de reporte y consulta externa de incidencias.

---

## Javadoc del Proyecto

El código fuente cuenta con comentarios Javadoc completos en español utilizando `@param`, `@return` y `@throws`:

- **Controllers:** Descripción de flujo MVC, modelos Thymeleaf, parámetros de ruta y redirecciones.
- **Service Interfaces (`IXxxService`) & Implementaciones:** Reglas de negocio, invariantes y precondiciones.
- **Entities & DTOs:** Mapeo relacional, validaciones declarativas Bean Validation (JSR-380) y constructores.
- **Configuración:** OpenAPI, filtros de seguridad y carga de datos semilla.

Para generar el sitio web HTML de Javadoc:
```bash
mvn javadoc:javadoc -DskipTests
# Resultado en: target/site/apidocs/index.html
```

---

## Endpoints Disponibles en el Sistema

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/` | Redirección a `/dashboard` | Autenticado |
| `GET` | `/login` | Pantalla de inicio de sesión | Público |
| `POST` | `/login` | Procesamiento de credenciales (Spring Security) | Público |
| `GET` | `/registro/paso-1` | Wizard de Empresa: datos corporativos | Público |
| `POST` | `/registro/paso-1` | Validación y avance al paso 2 | Público |
| `GET` | `/registro/paso-2` | Wizard de Administrador: credenciales de acceso | Público |
| `POST` | `/registro/paso-2` | Validación y avance al paso 3 | Público |
| `GET` | `/registro/paso-3` | Wizard de Configuración: umbrales SLA, CSAT y salud | Público |
| `POST` | `/registro/paso-3` | Persistencia atómica de la empresa y administrador | Público |
| `GET` | `/dashboard` | Tablero ejecutivo interactivo con KPIs y gráficos | Autenticado |
| `GET` | `/cambiar-password` | Redirección o acceso al cambio de contraseña temporal | Autenticado |
| `POST` | `/cambiar-password` | Procesar cambio de clave obligatoria | Autenticado |
| `GET` | `/clientes` | Listado paginado de clientes con filtros y KPIs de salud | Autenticado |
| `GET` | `/clientes/nuevo` | Formulario de alta de cliente B2B | `ADMIN` / `SUPERVISOR` |
| `POST` | `/clientes` | Persistir nuevo cliente y credenciales de portal | `ADMIN` / `SUPERVISOR` |
| `GET` | `/clientes/{id}` | Vista 360° del cliente con tabs temáticas | Autenticado |
| `GET` | `/clientes/{id}/editar` | Formulario de edición de cliente | `ADMIN` / `SUPERVISOR` |
| `POST` | `/clientes/{id}/editar` | Actualizar datos del cliente | `ADMIN` / `SUPERVISOR` |
| `POST` | `/clientes/{id}/eliminar` | Eliminación de cliente del sistema | `ADMIN` |
| `GET` | `/clientes/exportar` | Descarga de clientes en formato Excel (.xlsx) | Autenticado |
| `POST` | `/clientes/{clienteId}/contactos` | Agregar nuevo contacto al cliente | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/editar` | Modificar datos del contacto | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/principal` | Designar contacto principal y generar credenciales | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/desactivar` | Desactivar acceso del contacto | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/reactivar` | Reactivar contacto y regenerar clave temporal | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/portal/restablecer` | Restablecer contraseña temporal del portal | `ADMIN` |
| `GET` | `/tickets` | Bandeja de tickets con KPIs de SLA y filtros combinados | Autenticado |
| `GET` | `/tickets/nuevo` | Formulario para registrar nuevo ticket de soporte | Autenticado |
| `POST` | `/tickets` | Crear ticket con cálculo automático de SLA | Autenticado |
| `GET` | `/tickets/{id}` | Detalle del ticket con conversación, notas y tiempos | Autenticado |
| `POST` | `/tickets/{id}/mensajes` | Enviar respuesta visible o nota interna privada | Autenticado |
| `POST` | `/tickets/{id}/asignar` | Reasignar agente responsable | `ADMIN` / `SUPERVISOR` |
| `POST` | `/tickets/{id}/cambiar-estado` | Modificar estado y/o severidad del ticket | Autenticado |
| `POST` | `/tickets/{id}/cerrar` | Cerrar ticket y disparar encuesta CSAT si aplica | Autenticado |
| `GET` | `/renovaciones` | Tablero de contratos próximos a vencer y ARR en riesgo | Autenticado |
| `POST` | `/renovaciones/seguimiento` | Registrar avance comercial de renovación | Autenticado |
| `GET` | `/agentes` | Listado y gestión del equipo de agentes y supervisores | `ADMIN` |
| `POST` | `/agentes` | Crear agente con generación de clave temporal | `ADMIN` |
| `POST` | `/agentes/{id}/editar` | Modificar nombre, email o rol del agente | `ADMIN` |
| `POST` | `/agentes/{id}/estado` | Alternar estado activo / inactivo de un agente | `ADMIN` |
| `POST` | `/agentes/{id}/restablecer-password` | Generar y entregar nueva clave temporal al agente | `ADMIN` |
| `GET` | `/portal/tickets/nuevo` | Formulario público para creación de tickets por clientes | Público |
| `GET` | `/portal/tickets/{id}` | Consulta del estado y conversación de un ticket | Público / Contacto |
| `GET` | `/portalusuario/login` | Pantalla de inicio de sesión en portal de usuarios | Público |
| `GET` | `/portalusuario/cambiar-password` | Cambio de contraseña de usuario de portal | Público |
| `GET` | `/swagger-ui.html` | Interfaz interactiva de documentación Swagger | Autenticado |
| `GET` | `/v3/api-docs` | Especificación de esquema OpenAPI 3 en formato JSON | Autenticado |

---

## Pruebas

```bash
mvn clean test
```

| Clase de Test | Tipo | Cobertura |
|---|---|---|
| ServiClientApplicationTests | Integracion | Contexto Spring Boot + DataInitializer |
| SecurityAndAuthControllerTest | MockMvc | Login, Wizard 3 pasos, autorizacion |
| JpaRepositoriesTest | @DataJpaTest | Repositorios, filtros JPA, consultas |
| HealthScoreServiceTest | Unitario (Mockito) | Calculo de puntaje y factores de riesgo |
| TicketServiceTest | Unitario (Mockito) | SLA, mensajeria, cierre con CSAT y auditoria |

---

## Análisis de calidad con SonarQube

El proyecto incluye SonarScanner for Maven y JaCoCo para publicar en SonarQube el análisis estático y la cobertura generada al ejecutar las pruebas.

### Levantar SonarQube local

Docker Compose puede iniciar SonarQube Community Build y su base PostgreSQL sin alterar el arranque normal de la aplicación:

```powershell
docker compose --profile sonar up -d sonar-db sonarqube
```

Abre **http://localhost:9000**. En el primer inicio, entra con `admin` / `admin`, cambia la contraseña y crea un token desde **My Account → Security**. La primera puesta en marcha puede tardar unos minutos.

### Analizar el proyecto

En PowerShell, define el token para la sesión actual y lanza el análisis desde la raíz del repositorio:

```powershell
$env:SONAR_TOKEN = "tu_token_de_sonarqube"
.\mvnw.cmd clean test jacoco:report org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.host.url=http://localhost:9000
```

El comando ejecuta las pruebas y genera `target/site/jacoco/jacoco.xml`, que SonarQube importa para mostrar cobertura. Usa `test` y `jacoco:report` para evitar ejecutar el análisis independiente de OWASP Dependency-Check que está enlazado a la fase `verify`. El proyecto aparecerá con la clave `crm_helpdesk`. No guardes el token en `.env`, en el código ni en archivos versionados.

Para detener SonarQube sin borrar sus datos:

```powershell
docker compose --profile sonar stop sonarqube sonar-db
```

Los datos de análisis persisten en volúmenes Docker dedicados. No uses `docker compose down -v` si quieres conservarlos.

## Análisis de Vulnerabilidades (OWASP Dependency-Check)

El proyecto integra [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/) como plugin Maven. Se ejecuta en la fase `verify` y **falla el build** si detecta alguna dependencia con CVSS ≥ 7.0. Los reportes se generan en `target/dependency-check-report.html` (HTML) y `target/dependency-check-report.json` (JSON).

### 1. Obtener la NVD API key (gratuita, ~5 min)

> Desde 2023, la NVD (National Vulnerability Database) requiere autenticación para actualizar su base de CVEs. Sin clave el análisis no puede completarse.

1. Ve a **https://nvd.nist.gov/developers/request-an-api-key**
2. Completa el formulario con tu nombre y correo → recibirás el enlace de activación por email
3. Copia tu clave (formato `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)

### 2. Guardar la clave en `.env` (nunca en el repositorio)

```bash
# Edita tu .env local — ya está cubierto por .gitignore
NVD_API_KEY=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

> ⚠️ **No pongas la clave real en `.env.example` ni en `pom.xml`.** El archivo `.env` está en `.gitignore`.

### 3. Ejecutar el análisis

**PowerShell:**
```powershell
$env:NVD_API_KEY = (Get-Content .env | Select-String "^NVD_API_KEY=(.+)").Matches.Groups[1].Value
.\mvnw.cmd verify -DskipTests=true "-Dnvd.api.key=$env:NVD_API_KEY"
```

**Bash / Git Bash:**
```bash
export NVD_API_KEY=$(grep "^NVD_API_KEY=" .env | cut -d '=' -f2)
./mvnw verify -DskipTests=true -Dnvd.api.key=$NVD_API_KEY
```

> **Primera ejecución:** ~10-15 min (descarga la base de datos NVD completa).
> **Ejecuciones posteriores:** ~2-5 min (solo actualiza cambios recientes, caché en `~/.owasp/`).

### 4. Ver el reporte

```
target/dependency-check-report.html   ← Abrir en el navegador
target/dependency-check-report.json   ← Para integración con CI/CD
```

### Comportamiento del build

| Escenario | Resultado |
|---|---|
| Sin vulnerabilidades CVSS ≥ 7 | `BUILD SUCCESS` |
| Con vulnerabilidades CVSS ≥ 7 | `BUILD FAILURE` + reporte HTML con detalles |
| NVD no responde (`failOnError=false`) | `BUILD SUCCESS` + advertencia en log |

### Falsos positivos — Archivo de supresiones

Si el reporte incluye CVEs que no aplican a tu uso, crea `dependency-check-suppressions.xml` en la raíz del proyecto:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <!--
    <suppress>
        <notes>Falso positivo: CVE-XXXX-YYYY no aplica porque...</notes>
        <cve>CVE-XXXX-YYYY</cve>
    </suppress>
    -->
</suppressions>
```

Este archivo **sí se versiona** para documentar las decisiones de supresión del equipo.

### Integración en CI/CD (GitHub Actions)

```yaml
- name: OWASP Dependency-Check
  run: ./mvnw verify -DskipTests=true -Dnvd.api.key=${{ secrets.NVD_API_KEY }}
- name: Subir reporte
  uses: actions/upload-artifact@v4
  with:
    name: dependency-check-report
    path: target/dependency-check-report.html
```

---

## Modelo de Dominio

```
Empresa --< RolEntity --< Permiso (N:M via rol_permisos)
Empresa --< Usuario (rol_id -> RolEntity)
Empresa --< Cliente --< Contacto
                   --< ClienteServicio --> Servicio
                   --< Ticket --< TicketMensaje
                              --< TicketHistorial
                              --< TicketAdjunto
                              --- EncuestaCsat --< RespuestaCsat
                   --< Renovacion --< RenovacionSeguimiento
                   --< Interaccion
                   --< HealthScoreHistorial
Empresa --< CategoriaTicketEntity
Empresa --< ConfiguracionSla
Empresa --< ConfiguracionCsat
Empresa --< ConfiguracionHealthScore
Empresa --< Auditoria
Empresa --< Notificacion
```

### Enums del dominio (15 enumeraciones)

| Enum | Valores |
|---|---|
| Rol | ADMIN, AGENTE, SUPERVISOR |
| EstadoCliente | SALUDABLE, OBSERVACION, EN_RIESGO |
| EstadoSaludCliente | SALUDABLE, EN_OBSERVACION, EN_RIESGO, CRITICO |
| EstadoTicket | ABIERTO, EN_PROCESO, PENDIENTE, RESUELTO, CERRADO |
| PrioridadTicket | BAJA, MEDIA, ALTA, CRITICA |
| CategoriaTicket | INFRAESTRUCTURA, SOFTWARE, FACTURACION, ADMINISTRACION, SOPORTE_TECNICO |
| EstadoRenovacion | PENDIENTE, EN_NEGOCIACION, RENOVADO, NO_RENOVADO, CANCELADO |
| EstadoEncuestaCsat | PENDIENTE, ENVIADA, RESPONDIDA, VENCIDA |
| EstadoServicioCliente | ACTIVO, INACTIVO, SUSPENDIDO, CANCELADO |
| EstadoGeneral | ACTIVO, INACTIVO |
| TipoMensajeTicket | CLIENTE, AGENTE, NOTA_INTERNA, SISTEMA |
| TipoInteraccion | LLAMADA, EMAIL, REUNION, SEGUIMIENTO, OTRO |
| TipoNotificacion | TICKET_ASIGNADO, TICKET_VENCIDO, RENOVACION_PROXIMA, CLIENTE_EN_RIESGO, CSAT_RECIBIDO |
| CanalOrigen | PORTAL_WEB, EMAIL, TELEFONO, CHAT, API |
| AccionAuditoria | LOGIN, LOGOUT, CREAR, EDITAR, ELIMINAR, VER |

---

## Principios Aplicados

- **SOLID** - Interfaces de servicio (`IXxxService`) aplican SRP y DIP; cada capa tiene una sola razon de cambio
- **DRY** - Layout maestro Thymeleaf reutilizable en todas las vistas
- **Bajo acoplamiento** - Controllers dependen de interfaces, no de implementaciones concretas
- **Fail-fast** - Guava `Preconditions` valida argumentos antes de ejecutar logica de negocio
- **Javadoc** - Documentacion en codigo para controllers, interfaces, services y configuracion
- **OpenAPI 3.0** - Swagger UI auto-generado desde las anotaciones del codigo
- **Seguridad** - CSRF habilitado, contrasenas con BCrypt, rutas protegidas por rol

---

## Licencia

Proyecto academico - Libre uso educativo (MIT).
