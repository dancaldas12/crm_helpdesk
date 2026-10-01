# ServiClient CRM Helpdesk

> Aplicacion web monolitica de gestion de clientes, mesa de ayuda y renovaciones.
> **Java 21 · Spring Boot 3.3.3 · Thymeleaf · Spring Security · Spring Data JPA · MySQL 8**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg)](https://www.mysql.com/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D.svg)](http://localhost:8080/swagger-ui.html)
[![License](https://img.shields.io/badge/License-MIT%20Academico-lightgrey.svg)](#-licencia)

---

## Descripcion

**ServiClient** es un CRM academico orientado a empresas de servicios B2B. Permite gestionar el ciclo de vida del cliente: desde el registro de empresa y onboarding, hasta el seguimiento de tickets de soporte, control de SLA, encuestas de satisfaccion (CSAT) y gestion de renovaciones de contratos.

### Modulos principales

| Modulo | Descripcion |
|---|---|
| Autenticacion | Login + Wizard de registro en 3 pasos (Empresa -> Admin -> Configuracion) |
| Clientes | Listado paginado con Health Score, filtros y Vista 360 por tabs |
| Helpdesk | Mesa de ayuda con KPIs, conversacion, notas internas y control de SLA |
| Renovaciones | Panel por rangos de vencimiento, alertas de riesgo y seguimientos |
| Health Score | Motor de calculo automatico (0-100) basado en CSAT, SLA y renovacion |
| Swagger UI | Documentacion interactiva de la API en /swagger-ui.html |

---

## Stack Tecnologico

| Capa | Tecnologia | Version |
|---|---|---|
| Lenguaje | Java | 21 |
| Framework | Spring Boot | 3.3.3 |
| Vista | Thymeleaf | 3.1 |
| Persistencia | Spring Data JPA + Hibernate | 6 |
| Seguridad | Spring Security | 6 |
| Base de Datos | MySQL | 8.0+ |
| BD alternativa | H2 in-memory | - |
| Build | Apache Maven | 3.9+ |
| Utilidades | Lombok | - |
| Documentacion API | SpringDoc OpenAPI (Swagger) | 2.6.0 |

---

## Arquitectura

Monolito en capas estrictas:

```
com.serviclient.crm
+-- config/
|   +-- DataInitializer.java          <- Datos demo en las 25 tablas al arrancar
|   +-- OpenApiConfig.java            <- Configuracion Swagger / OpenAPI 3.0
|   +-- security/
|       +-- SecurityConfig.java
|       +-- CustomUserDetails.java
|       +-- CustomUserDetailsService.java
+-- controller/                        <- Spring MVC (@Controller)
|   +-- AuthController.java
|   +-- ClienteController.java
|   +-- TicketController.java
|   +-- RenovacionController.java
|   +-- GlobalControllerAdvice.java
+-- service/                           <- Logica de negocio (@Service)
|   +-- EmpresaService.java
|   +-- UsuarioService.java
|   +-- ClienteService.java
|   +-- HealthScoreService.java
|   +-- TicketService.java
|   +-- RenovacionService.java
+-- repository/                        <- Spring Data JPA (25 repositorios)
+-- entity/                            <- Entidades JPA (25 entidades)
|   +-- enums/                         <- 15 enumeraciones de dominio
+-- ServiClientApplication.java
```

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

### Configuracion de conexion

```yaml
# application-mysql.yml (valores por defecto)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/serviclient?createDatabaseIfNotExist=true
    username: root
    password:
  jpa:
    hibernate:
      ddl-auto: update
```

Variables de entorno: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`

---

## Requisitos Previos

| Herramienta | Version minima | Descarga |
|---|---|---|
| Java JDK | 21 | https://learn.microsoft.com/es-es/java/openjdk/download |
| Apache Maven | 3.9+ | https://maven.apache.org/download.cgi |
| MySQL Server | 8.0+ | https://dev.mysql.com/downloads/mysql/ |

---

## Ejecucion

### Opcion 1 - Script rapido automatizado (Windows)

El repositorio incluye un script automatizado (`iniciar.bat`) que configura las variables de entorno de Java 21 y Maven, levanta el backend con el perfil MySQL y abre automaticamente el navegador en `http://localhost:8080`:

```bat
iniciar.bat
```

> **Nota:** Tambien puedes ejecutarlo desde PowerShell con:
> ```powershell
> .\iniciar.ps1
> ```

**Que hace el script internamente:**
1. Configura `JAVA_HOME` apuntando al JDK 21 (`C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot`) y anade Maven al `PATH`.
2. Lanza un temporizador en segundo plano para abrir `http://localhost:8080` tras el arranque.
3. Si existe `target\crm-helpdesk-1.0.0.jar`, ejecuta:
   ```cmd
   java -jar target\crm-helpdesk-1.0.0.jar --spring.profiles.active=mysql
   ```
4. Si no existe el empaquetado JAR, lo compila y ejecuta via Maven:
   ```cmd
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

### Opcion 2 - Maven directo

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Opcion 3 - JAR empaquetado

```bash
mvn clean package -DskipTests
java -jar target/crm-helpdesk-1.0.0.jar --spring.profiles.active=mysql
```

La aplicacion estara disponible en **http://localhost:8080**

> **Primer arranque:** El `DataInitializer` detecta que la BD esta vacia y carga
> automaticamente datos de demostracion en las 25 tablas.

---

## Credenciales Demo

| Email | Contrasena | Rol |
|---|---|---|
| admin@serviclient.com | admin123 | Administrador |
| carlos.ruiz@serviclient.com | admin123 | Agente de Soporte |
| ana.garcia@serviclient.com | admin123 | Agente de Soporte |
| laura.mendoza@serviclient.com | admin123 | Supervisor Customer Success |

---

## Documentacion API (Swagger / OpenAPI)

Con la aplicacion corriendo, accede a:

| URL | Descripcion |
|---|---|
| http://localhost:8080/swagger-ui.html | **Swagger UI interactivo** |
| http://localhost:8080/v3/api-docs | Spec OpenAPI 3.0 en JSON |
| http://localhost:8080/v3/api-docs.yaml | Spec OpenAPI 3.0 en YAML |

### Tags documentados

| Tag | Rutas cubiertas |
|---|---|
| Autenticacion | /login, /registro/paso-{1,2,3} |
| Clientes | /clientes/** |
| Helpdesk / Tickets | /tickets/** |
| Renovaciones | /renovaciones/** |

> **Nota:** Para probar endpoints protegidos desde Swagger UI, inicia sesion en /login
> en la misma pestaña del navegador. La cookie JSESSIONID se comparte automaticamente.

---

## Javadoc

El codigo fuente incluye Javadoc completo en:

- **Controllers**: descripcion de clase, rutas, parametros, retornos y excepciones
- **Services**: descripcion de la logica de negocio y sus invariantes
- **Entities**: descripcion de campos JPA y relaciones
- **Configuracion**: OpenApiConfig, SecurityConfig, DataInitializer

Para generar el sitio HTML de Javadoc:

```bash
mvn javadoc:javadoc
# Resultado en: target/site/apidocs/index.html
```

---

## Endpoints Disponibles

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | / | Redireccion a /clientes |
| GET | /login | Pantalla de inicio de sesion |
| POST | /login | Procesar login (Spring Security) |
| GET | /registro/paso-1 | Wizard - Datos de Empresa |
| POST | /registro/paso-1 | Validar y avanzar al paso 2 |
| GET | /registro/paso-2 | Wizard - Usuario Administrador |
| POST | /registro/paso-2 | Validar y avanzar al paso 3 |
| GET | /registro/paso-3 | Wizard - Configuracion SLA/CSAT/Health Score |
| POST | /registro/paso-3 | Persistir empresa completa |
| GET | /clientes | Listado paginado de clientes |
| GET | /clientes/nuevo | Formulario de nuevo cliente |
| POST | /clientes | Crear cliente |
| GET | /clientes/{id} | Vista 360 del cliente |
| POST | /clientes/{id}/contactos | Agregar contacto al cliente |
| GET | /tickets | Mesa de Ayuda - listado con KPIs |
| GET | /tickets/nuevo | Formulario de nuevo ticket |
| POST | /tickets | Crear ticket |
| GET | /tickets/{id} | Detalle de ticket (conversacion + SLA) |
| POST | /tickets/{id}/mensajes | Enviar mensaje o nota interna |
| POST | /tickets/{id}/asignar | Reasignar agente |
| POST | /tickets/{id}/cambiar-estado | Cambiar estado y/o prioridad |
| POST | /tickets/{id}/cerrar | Cerrar ticket (dispara CSAT) |
| GET | /renovaciones | Panel de renovaciones con alertas |
| POST | /renovaciones/seguimiento | Registrar seguimiento de renovacion |
| GET | /swagger-ui.html | Documentacion Swagger UI |
| GET | /v3/api-docs | Spec OpenAPI JSON |

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

- **SOLID** - Separacion clara de responsabilidades por capa
- **DRY** - Layout maestro Thymeleaf reutilizable en todas las vistas
- **Javadoc** - Documentacion en codigo para controllers, services y configuracion
- **OpenAPI 3.0** - Swagger UI auto-generado desde las anotaciones del codigo
- **Seguridad** - CSRF habilitado, contrasenas con BCrypt, rutas protegidas por rol

---

## Licencia

Proyecto academico - Libre uso educativo (MIT).
