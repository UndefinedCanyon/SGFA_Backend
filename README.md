# SGFA – Backend

API REST del **Software de Gestión para Ferias Artesanales (SGFA)**, desarrollado en la Universidad de Cundinamarca (Ingeniería de Sistemas y Computación) para centralizar la gestión de las ferias artesanales de Fusagasugá.

Frontend: [SGFA_Frontend](https://github.com/UndefinedCanyon/SGFA_Frontend)

**Integrantes:** Jonhatan Emmanuel Alvarado Zamora · Juan David Cañon Vanegas

## Tecnologías

Java 21 · Spring Boot 4 · Spring Security + JWT · BCrypt · Spring Data JPA · MySQL 8 · Swagger (springdoc) · Docker Compose · GitHub Actions · ArchUnit

## Arquitectura hexagonal

```
src/main/java/com/sgfa/backend/
├── domain/model/                  Modelos de negocio, sin dependencias de frameworks
├── application/
│   ├── port/in/                   Puertos de entrada (casos de uso)
│   ├── port/out/                  Puertos de salida (persistencia, cifrado, tokens)
│   ├── usecase/                   Implementación de los casos de uso
│   └── exception/                 Excepciones de negocio
└── infrastructure/
    ├── adapter/in/web/            Controladores REST
    ├── adapter/out/persistence/   Entidades JPA y adaptadores de repositorio
    ├── security/                  JWT, filtro de autenticación y adaptadores de cifrado y token
    └── config/                    Seguridad, CORS y beans de los casos de uso
database/                          schema.sql y data.sql
```

Las dependencias apuntan siempre hacia adentro: infraestructura → aplicación → dominio. La prueba `ArquitecturaHexagonalTest` (ArchUnit) lo verifica automáticamente.

## Cómo ejecutarlo con Docker (recomendado)

Necesitas Docker Desktop. Clona los dos repositorios **uno al lado del otro**, porque el compose construye también el frontend:

```
SGFA_Backend/
SGFA_Frontend/
```

```bash
cd SGFA_Backend
cp .env.example .env        # en Windows: copy .env.example .env
# edita .env y define DB_PASSWORD y JWT_SECRET
docker compose up --build
```

| Servicio | Dirección |
|---|---|
| Aplicación (frontend) | http://localhost:5173 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| MySQL (desde tu PC) | `127.0.0.1:3307`, usuario `root` |

La primera vez, MySQL ejecuta `database/schema.sql` y `database/data.sql` automáticamente. Si tu frontend está en otra carpeta, agrega `FRONTEND_PATH=ruta/a/SGFA_Frontend` al `.env`.

Para apagar: `docker compose down` (conserva los datos) o `docker compose down -v` (borra los datos).

## Cómo ejecutarlo sin Docker

Requisitos: JDK 21 y MySQL 8.

1. Crea la base ejecutando `database/schema.sql` y luego `database/data.sql` en MySQL.
2. Crea el archivo `.env` (ver la tabla de variables).
3. Ejecuta:

```bash
./mvnw spring-boot:run        # en Windows PowerShell: .\mvnw.cmd spring-boot:run
```

## Variables de entorno

Se leen del archivo `.env` (que no se sube a Git) o del entorno del sistema.

| Variable | Obligatoria | Por defecto | Para qué sirve |
|---|---|---|---|
| `DB_PASSWORD` | Sí | | Contraseña de MySQL |
| `JWT_SECRET` | Sí | | Clave con la que se firman los tokens JWT |
| `JWT_EXPIRATION_MS` | No | `7200000` (2 h) | Duración del token |
| `CORS_ORIGINS` | No | `http://localhost:5173` | Orígenes permitidos, separados por coma |
| `JPA_SHOW_SQL` | No | `false` | Muestra las consultas SQL en el log |
| `SWAGGER_ENABLED` | No | `true` | Activa o desactiva Swagger (apágalo en producción) |
| `PORT` | No | `8080` | Puerto del servidor |
| `FRONTEND_PATH` | No | `../SGFA_Frontend` | Solo para `docker compose` |

## Usuarios de prueba

`database/data.sql` carga datos de ejemplo **solo para desarrollo y demostración**:

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@sgfa.com` | `admin123` |
| Artesano | `maria@correo.com` | `clave123` |
| Artesano | `carlos@correo.com` | `clave123` |
| Artesano | `laura@correo.com` | `clave123` |

En un entorno real se carga solo `schema.sql` y se crea el administrador propio.

## Rutas principales

| Acceso | Rutas |
|---|---|
| Público | `POST /api/auth/login`, `POST /api/artesanos` (registro), `GET /api/ferias`, `GET /api/edicionesferia`, `GET /api/edicionesferia/{id}/participantes`, `GET /api/edicionesferia/{id}/ocupacion`, `GET /api/lugares` |
| Artesano | `GET` y `PUT /api/artesanos/perfil`, `/api/productos`, `POST /api/inscripciones`, `GET /api/inscripciones/mias` |
| Administrador | `POST` y `PUT /api/ferias`, `GET /api/ferias/todas`, `/api/edicionesferia` (crear, estado, cupo), `POST /api/lugares`, `GET /api/artesanos`, `PUT /api/artesanos/{id}/estado`, `GET /api/inscripciones`, `PUT /api/inscripciones/{id}/aprobar` y `/rechazar` |

Las rutas protegidas exigen el header `Authorization: Bearer <token>`. La referencia completa está en Swagger.

## Pruebas

```bash
./mvnw test "-Dtest=ArquitecturaHexagonalTest"
```

Ejecuta las 4 reglas de arquitectura (el dominio y la aplicación no dependen de infraestructura ni de frameworks). No ejecutes `./mvnw test` a secas: incluye el test por defecto de Spring, que necesita MySQL.

## Tareas de administración

**Reiniciar los datos de ejemplo (Docker):**

```bash
docker compose down -v
docker compose up -d
```

**Entrar a la base de datos del contenedor:**

```bash
docker compose exec db mysql -uroot -p sgfa_db
```

**Cambiar la contraseña del administrador.** Genera un hash BCrypt (cuenta de costo 10). Una forma sin instalar nada:

```bash
docker run --rm httpd:2 htpasswd -nbB -C 10 admin "NuevaClave"
```

Copia lo que va después de `admin:` y ejecútalo en MySQL:

```sql
UPDATE administrador SET contrasena = '<hash>' WHERE correo_electronico = 'admin@sgfa.com';
```

**Aplicar un cambio de esquema a una base que ya existe.** Los scripts de `database/` solo se ejecutan con la base vacía. Si ya tienes datos, aplica a mano lo que falte:

```sql
ALTER TABLE artesano ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE feria ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE edicion_feria ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE edicion_feria ADD COLUMN cupo_maximo INT NULL;
```

## Flujo de trabajo

Ramas: `desarrollo` → `preproduccion` → `main`. En cada push y pull request, GitHub Actions ejecuta la prueba de arquitectura y construye la imagen Docker, etiquetada con el hash del commit.