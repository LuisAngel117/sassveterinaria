# 03 — Arquitectura (monolito modular, local-first)

## 1) Resumen
- Arquitectura: **monolito modular** (backend) + **frontend web** (Next.js) + **Postgres local**.
- Objetivo: maximizar consistencia y trazabilidad, minimizando piezas sueltas.

## 2) Diagrama lógico (texto)

[Frontend Next.js]
  - UI en Español
  - Manejo de sesión (access/refresh)
  - Selección de sucursal
  - Consumo API REST

        │ HTTPS (local)
        ▼

[Backend Spring Boot]
  Módulos:
  - auth (login/refresh/logout, 2FA)
  - scoping (branch)
  - agenda (citas)
  - crm (clientes, mascotas)
  - clinical (atenciones, SOAP, adjuntos, prescripciones)
  - catalog (salas, servicios, unidades)
  - billing (facturas, pagos, impuestos, export)
  - inventory (stock, movimientos, mínimos, consumo)
  - reports
  - audit
  - feature-flags
        │
        ▼
[Postgres 17]
  - migraciones por Flyway
  - datos scopiados por branch_id

## 3) Decisiones clave (links a ADRs)
- Stack: ver `docs/decisions/adr-0001-stack.md`
- Arquitectura modular: ver `docs/decisions/adr-0002-arquitectura.md`
- Scoping multi-sucursal: ver `docs/decisions/adr-0003-tenancy-scoping.md`
- Seguridad (JWT+2FA): ver `docs/decisions/adr-0004-seguridad-auth.md`
- Auditoría: ver `docs/decisions/adr-0005-auditoria.md`

## 4) Tenancy & scoping (branch)

**Contexto:**
- No hay multi-tenant (1 clínica).
- Sí hay multi-sucursal (branch) y **separación de datos**.

**Regla de seguridad (anti spoofing):**
- El access token incluye claim `branch_id` activo.
- La request incluye header `X-Branch-Id`.
- Backend valida:
  - Si falta header en endpoint scopiado → **400** (scope requerido).
  - Si el token no tiene scope válido (no seleccionó sucursal) → **403**.
  - Si header no coincide con claim y no es SUPERADMIN → **403**.
  - Si no hay/expiró token → **401**.

**Excepciones:**
- Endpoints “globales” (ej. `GET /api/v1/branches` para listar sucursales accesibles) no requieren `X-Branch-Id`.

## 5) Seguridad

### Auth
- JWT access token (1h) + refresh token (7 días) con rotación.
- 2FA TOTP (RFC 6238: https://datatracker.ietf.org/doc/html/rfc6238) para ADMIN/SUPERADMIN.
- Password hashing: BCrypt/Argon2 (definir en implementación; preferible BCrypt por simplicidad).

### CORS local
- Permitir `http://localhost:<puerto-frontend>` → `http://localhost:<puerto-backend>`.

### Rate limit / defensa básica
- Rate-limit fuerte en endpoints de auth.
- 429 Too Many Requests como respuesta estándar (RFC 6585 define 429: https://datatracker.ietf.org/doc/html/rfc6585).
- Lockout por intentos fallidos (BRD-REQ-012).

## 6) Convenciones de API

### Versionado
- Prefijo: `/api/v1`

### Naming
- Endpoints en inglés (consistencia técnica); UI en español.

### Errores
- RFC 7807 Problem Details (https://www.rfc-editor.org/rfc/rfc7807)
- Campos mínimos: `type`, `title`, `status`, `detail`, `instance`
- Extensiones: `errorCode`, `traceId`, `fieldErrors[]` para validación.

### Estándar de códigos (mínimo)
- 400: validación / scope header faltante
- 401: no autenticado
- 403: autenticado sin permiso / scope inválido
- 404: recurso no existe (dentro del scope)
- 409: conflictos (no-solape, duplicados)
- 429: rate limit

## 7) Data / DB

### IDs
- UUID para entidades principales.

### Timezone
- Persistir con `timestamptz` (Postgres) y exponer ISO-8601 con offset.
- Regla: business timezone `America/Guayaquil`.

### Money
- `BigDecimal` (backend) con escala definida (ej. 2).
- Moneda: USD (Ecuador).

### Migraciones
- Flyway (asegurar compatibilidad con Postgres 17; mínimo Flyway 10.20.1+ recomendado).
  - Nota: Postgres anunció compatibilidad 12–17 en notas relacionadas a Flyway 10.20.1+ (referencia pública: https://www.postgresql.org/about/news/flyway-community-drift-check-released-2970/)

## 8) Estrategia de pruebas
- Unit tests (servicios/reglas).
- Integration tests (repositorios + Postgres Testcontainers si se decide, pero local-first y sin Docker al inicio: dejarlo como “futuro”).
- Smoke scripts (PowerShell) para flujos críticos.

## 9) Anti-desviación
- No inventar requisitos.
- Cambios de alcance: RFC/ADR + changelog.
- EOF markers obligatorios en docs.
- Sprints inmutables.

<!-- EOF -->
