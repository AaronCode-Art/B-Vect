# VECT Backend

API REST para la plataforma VECT de gestión de incidencias, soporte técnico, activos, inventario, conversaciones y aprobaciones. Está implementada con Java y Spring Boot, persiste los datos en PostgreSQL/Neon y documenta sus endpoints con SpringDoc OpenAPI (Swagger UI).

## Contenido

- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Configuración](#configuración)
- [Base de datos](#base-de-datos)
- [Ejecución local](#ejecución-local)
- [API y Swagger](#api-y-swagger)
- [Autenticación y permisos](#autenticación-y-permisos)
- [Módulos de la API](#módulos-de-la-api)
- [Pruebas y empaquetado](#pruebas-y-empaquetado)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Comportamientos y límites conocidos](#comportamientos-y-límites-conocidos)

## Tecnologías

- Java 21 o superior.
- Spring Boot 4.1.1: MVC, validación, seguridad y acceso a datos.
- Spring Data JPA / Hibernate.
- PostgreSQL, con esquema `vect` (la configuración del proyecto apunta a Neon).
- JWT para autenticación, con JJWT.
- MapStruct para convertir entidades a DTOs; Lombok para reducir código repetitivo.
- Cloudinary para archivos y adjuntos.
- Apache POI para generar reportes Excel.
- SpringDoc OpenAPI 3.1.0 para documentación interactiva.
- Maven Wrapper, incluido en el repositorio.

## Requisitos

1. JDK 21 o superior disponible en `PATH`.
2. Acceso a una base PostgreSQL configurada con el esquema VECT.
3. Credenciales de Cloudinary para las operaciones que suben archivos.
4. Windows PowerShell para los comandos de esta guía. El proyecto también puede compilarse con Maven en otros sistemas.

## Configuración

La configuración principal se encuentra en `src/main/resources/application.properties`. Los valores de entorno son la forma recomendada de configurar secretos y diferencias entre entornos. Ejemplo en PowerShell (valores ilustrativos; reemplázalos por los tuyos):

```powershell
$env:DATABASE_URL = "jdbc:postgresql://HOST:5432/BASE?sslmode=require"
$env:DATABASE_USERNAME = "USUARIO"
$env:DATABASE_PASSWORD = "CONTRASENA"
$env:JWT_SECRET = "CLAVE_ALEATORIA_LARGA_Y_PRIVADA"
$env:CLOUDINARY_CLOUD_NAME = "CLOUD_NAME"
$env:CLOUDINARY_API_KEY = "API_KEY"
$env:CLOUDINARY_API_SECRET = "API_SECRET"
$env:CLOUDINARY_FOLDER = "vect/evidencias"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173,http://localhost:5174"
```

Estas variables solo permanecen en la sesión actual de PowerShell. No guardes contraseñas, tokens, claves JWT ni secretos de Cloudinary en el repositorio.

| Variable | Uso |
| --- | --- |
| `DATABASE_URL` | URL JDBC de PostgreSQL. |
| `DATABASE_USERNAME` | Rol de la base de datos. |
| `DATABASE_PASSWORD` | Contraseña del rol de base de datos. |
| `JWT_SECRET` | Clave privada para firmar y validar tokens. En producción debe ser aleatoria, suficientemente larga y gestionada fuera del código. |
| `JWT_ISSUER` | Emisor de los JWT; por defecto `vect`. |
| `JWT_ACCESS_EXPIRATION` | Duración del token de acceso en milisegundos; por defecto 900000 (15 minutos). |
| `JWT_REFRESH_EXPIRATION` | Duración del token de renovación en milisegundos; por defecto 2592000000 (30 días). |
| `CLOUDINARY_CLOUD_NAME` | Cloud de Cloudinary. |
| `CLOUDINARY_API_KEY` | Clave de API de Cloudinary. |
| `CLOUDINARY_API_SECRET` | Secreto de API de Cloudinary. |
| `CLOUDINARY_FOLDER` | Carpeta predeterminada para evidencias; por defecto `vect/evidencias`. |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos separados por coma. Por defecto incluye los puertos locales 5173, 5174 y 4200. |

El servidor escucha en el puerto `8080`. Los adjuntos tienen un máximo configurado de 8 MB por archivo y 9 MB por petición multipart.

> **Nota:** `application.properties` no contiene secretos por defecto; configura `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` y `JWT_SECRET` antes de iniciar la aplicacion.
> En despliegue, configura `CORS_ALLOWED_ORIGINS` con el origen HTTPS exacto del frontend; no conserves los puertos locales.

## Base de datos

- La aplicación usa el esquema PostgreSQL `vect`.
- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` y `JWT_SECRET` son obligatorios. `JWT_SECRET` debe tener al menos 32 bytes; genera un valor aleatorio y guárdalo solo en el entorno local o en el gestor de secretos del servicio de despliegue.
- `.env.example` es una plantilla de referencia: Spring Boot no carga archivos `.env` automáticamente. Define las variables en la terminal o en la plataforma de despliegue antes de iniciar la aplicación. Las credenciales de Cloudinary son necesarias para habilitar cargas de archivos.
- Hibernate está configurado con `spring.jpa.hibernate.ddl-auto=validate`: valida el esquema al iniciar, pero **no crea ni actualiza tablas automáticamente**.
- `db/vect.sql` es el volcado del esquema y datos de referencia. Es una base inicial para restauración en una base nueva; no debe ejecutarse como actualización sobre una base que ya contiene datos.
- El volcado no incluye cuentas de usuario ni hashes de contraseñas. Antes de usar la autenticación en un entorno nuevo, aprovisiona la cuenta ADMIN mediante un procedimiento seguro fuera del repositorio.
- `db/migrations/` contiene cambios incrementales. Revisa y aplica solo las migraciones que aún no existan en la base de destino, siguiendo el proceso controlado del proyecto.
- El esquema emplea tipos enum de PostgreSQL, `jsonb`, claves foráneas y la extensión `pgcrypto`.
- La migración `20261001_conversation_attachments.sql` añade metadatos de archivos a `mensajes_conversacion`. La migración `20261001_restore_waiting_parts_transitions.sql` añade transiciones de estado asociadas a la espera de repuestos.

Configura `DATABASE_URL`, usuario y contraseña antes de iniciar la aplicación. Si Hibernate informa que faltan columnas, tablas o tipos, compara la base con `db/vect.sql` y las migraciones aplicables; no desactives `validate` para ocultar la diferencia.

## Ejecución local

Desde la raíz del backend:

```powershell
.\mvnw.cmd spring-boot:run
```

La API estará disponible en `http://localhost:8080`. Para detenerla, usa `Ctrl+C` en la terminal donde está ejecutándose.

## API y Swagger

Con el backend iniciado:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificación OpenAPI JSON: <http://localhost:8080/api-docs>

La ruta `/swagger-ui.html` redirige a la interfaz de Swagger UI. La documentación y sus recursos están permitidos sin autenticación. Los endpoints funcionales, salvo los de inicio y renovación de sesión, requieren autenticación.

Usa Swagger UI para explorar rutas, parámetros, DTOs y respuestas. Para llamar endpoints protegidos, inicia sesión y envía el token de acceso como `Authorization: Bearer <accessToken>`. La forma más confiable de consultar el contrato actualizado de cada endpoint es la especificación OpenAPI servida por la aplicación.

## Autenticación y permisos

### Sesión JWT

1. `POST /api/auth/login` recibe correo y contraseña.
2. La respuesta contiene `accessToken`, `refreshToken`, `tokenType`, duración del token de acceso y datos básicos del usuario.
3. Envía el token de acceso como `Authorization: Bearer <accessToken>` en endpoints protegidos.
4. Cuando expire, `POST /api/auth/refresh` recibe el refresh token y devuelve un nuevo par de tokens.
5. `GET /api/auth/me` devuelve el usuario autenticado.

El acceso a rutas administrativas se controla en el backend, tanto a nivel de rutas como de método. Ocultar un enlace en el frontend no sustituye estos controles.

### Roles

El sistema utiliza los roles `ADMIN`, `GERENCIA`, `SUPERVISOR`, `TECNICO` y `EMPLEADO`. El acceso concreto depende de cada operación; por ejemplo, las reglas de automatización y parámetros del sistema son exclusivos de ADMIN, mientras que la gestión de ubicaciones permite ADMIN y GERENCIA. Revisa las anotaciones de seguridad y Swagger antes de integrar una operación específica.

## Módulos de la API

Todos los endpoints REST se encuentran bajo `/api`, salvo la documentación OpenAPI. Los controladores definen los detalles y permisos de cada operación.

| Área | Rutas principales | Funcionalidad |
| --- | --- | --- |
| Autenticación | `/api/auth` | Inicio de sesión, renovación de tokens y usuario actual. |
| Incidencias | `/api/incidencias` | Listado y consulta según identidad/rol, creación, asignación, cambios de estado, actividad e historial. |
| Chat de incidencias | `/api/incidencias/{id}/chat` | Mensajes asociados a una incidencia. |
| Evidencias | `/api/incidencias/{id}/evidencias`, `/api/evidencias` | Evidencias de una incidencia y consulta global autorizada. |
| Conversaciones | `/api/conversaciones` | Conversaciones, participantes, mensajes y envío de archivos adjuntos. |
| Supervisión de chat | `/api/chat-supervision` | Consulta supervisada de chats para roles autorizados. |
| Solicitudes de cambio | `/api/solicitudes-cambio` | Solicitud, evaluación, aprobaciones, rechazo y ejecución de cambios. |
| Historial | `/api/historial` | Historial de incidencias y de aprobaciones, con permisos específicos. |
| Usuarios | `/api/usuarios` | Usuarios, técnicos disponibles y asignaciones de especialidad. |
| Ubicaciones | `/api/ubicaciones`, `/api/sedes` | Consulta de ubicaciones; la gestión permite crear, editar, listar inactivas y desactivar. |
| Catálogos | `/api/categorias`, `/api/especialidades`, `/api/roles`, `/api/estados-incidencia` | Categorías, especialidades y catálogos de referencia. |
| Activos | `/api/activos` | Registro de activos y componentes instalados. |
| Inventario | `/api/componentes` | Componentes, existencias y movimientos de stock. |
| Panel y reportes | `/api/dashboard`, `/api/reportes` | Resumen operativo y descargas Excel. |
| Administración | `/api/auditoria`, `/api/parametros`, `/api/reglas-automatizacion` | Auditoría y configuración restringida. |

## Pruebas y empaquetado

La suite incluye pruebas HTTP de integración que escriben y eliminan usuarios en PostgreSQL. Configura las variables de entorno para una base de pruebas aislada; no ejecutes estas pruebas contra producción.

Ejecuta la suite completa:

```powershell
.\mvnw.cmd clean test
```

Crear el JAR ejecutable:

```powershell
.\mvnw.cmd clean package
```

El artefacto se genera bajo `target/`. Los reportes de pruebas Maven se guardan en `target/surefire-reports/`.

## Estructura del proyecto

```text
src/main/java/com/vect/vect/
├── config/       configuración de Spring, seguridad, Cloudinary y auditoría
├── controller/   controladores REST
├── dto/          contratos request/response de la API
├── entity/       entidades JPA
├── mapper/       conversión entidad/DTO con MapStruct
├── repository/   acceso a datos con Spring Data
├── security/     validación JWT y filtros de autenticación
├── service/      reglas de negocio
└── common/       manejo y formato común de errores

src/main/resources/
└── application.properties

db/
├── vect.sql
└── migrations/
```

## Comportamientos y límites conocidos

- **Automatización:** actualmente la API permite guardar, listar y actualizar la configuración JSON de reglas. No hay un motor que evalúe y ejecute esas reglas al ocurrir eventos.
- **Parámetros:** el endpoint permite consultar y actualizar valores JSON. Un parámetro solo modifica la lógica si un servicio lo lee; no todos los valores guardados están conectados a la lógica de negocio.
- **Umbral de aprobación:** el umbral de gerencia se encuentra actualmente definido en `SolicitudCambioService`; cambiar una clave de parámetros no debe asumirse como cambio efectivo de ese umbral.
- **Límites de archivos:** el máximo de carga se valida en backend y en el esquema de metadatos de adjuntos; cambiar solo la configuración multipart no elimina los demás límites.
- **Errores:** las excepciones de negocio y validación se convierten en respuestas estructuradas mediante el manejador global de excepciones.
