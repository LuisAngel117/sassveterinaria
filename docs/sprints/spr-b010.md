# SPR-B010 — Hardening de Seguridad (2FA + lockout + rate limit + permisos finos)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 3  
**Duración objetivo:** 60–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Endurecer seguridad del backend para demo vendible local-first, cerrando:
  - **BRD-REQ-004** Roles y permisos granulares por acción.
  - **BRD-REQ-005** 2FA TOTP para ADMIN/SUPERADMIN (enforcement configurable, default ON en demo).
  - **BRD-REQ-006** Lockout por intentos fallidos (4 intentos / 15 min → lock 15 min; configurable).
  - **BRD-REQ-009** Rate limit básico (login/refresh/reportes) con 429 Problem Details.
- Dejar evidencia y smoke mínimo del hardening.

## 2) Alcance

### Incluye

1) **2FA TOTP (ADMIN/SUPERADMIN)**
- Implementar TOTP (RFC 6238) conforme `docs/05-seguridad.md` y `docs/decisions/adr-0004-seguridad-auth.md`.
- Requisitos mínimos:
  - Campo(s) persistidos en usuario (o tabla asociada) para:
    - `totp_secret` (o equivalente; almacenar de forma segura, mínimo encriptado o hashed+pepper si existe patrón)
    - `totp_enabled` (bool)
    - `totp_verified_at` (nullable)
  - Endpoints (nombres exactos pueden adaptarse a convención existente, pero deben existir estos flujos):
    - **Setup** (solo ADMIN/SUPERADMIN autenticado): genera secret + otpauth URI (y/o QR payload).
    - **Enable/Confirm**: valida código TOTP y habilita 2FA.
    - **Disable** (opcional): deshabilita 2FA (si se implementa, exigir reason y auditar).
  - Login:
    - Si el usuario requiere 2FA y está habilitado: el login debe devolver “challenge” (no tokens) y un token/handle temporal para completar 2FA.
    - Endpoint para completar 2FA (ej: `POST /api/v1/auth/login/2fa`) que intercambia challenge + código por access/refresh.
- Auditoría:
  - Setup/enable/disable deben generar eventos de auditoría si existe infraestructura (mínimo) (ver `docs/05-seguridad.md`).

2) **Lockout por intentos fallidos**
- Default conforme `docs/05-seguridad.md`:
  - 4 intentos fallidos en ventana 15 min → lock 15 min.
  - valores **configurables** por properties.
- Persistencia:
  - Usar `app_user.locked_until` (si existe según dominio) y lo que sea necesario para contar intentos (tabla de intentos o contador con ventana).
- Respuestas:
  - Credenciales inválidas: 401 (Problem Details).
  - Usuario bloqueado: preferir 423 (Problem Details) **o** 429 si ya existe convención; documentar en OpenAPI.
- Auditoría:
  - Registrar intentos fallidos y evento de lock/unlock (si existe audit service).

3) **Rate limit básico (login/refresh/reportes)**
- Implementar rate limit local-first sin depender de servicios externos.
- Targets mínimos (según BRD-REQ-009 / `docs/05-seguridad.md`):
  - login: estricto
  - refresh: medio
  - reportes/export: más bajo (por ser “caro”)
- Reglas:
  - configurables en properties (defaults razonables).
  - key: por `ip + user/email` donde aplique (login) y por `user_id + branch_id` donde aplique (reportes).
- Respuesta:
  - HTTP 429 + Problem Details.
  - Incluir `Retry-After` si es viable.
- Implementación:
  - Reusar librería/patrón existente SI ya hay uno.
  - Si NO hay ninguno, implementar un limiter in-memory (ConcurrentHashMap + sliding window/token bucket) **solo para demo local**.

4) **Permisos finos por acción (BRD-REQ-004)**
- Auditar controladores/endpoints existentes y asegurar:
  - cada acción mutadora (create/update/cancel/close/pay/void/adjust/override/config update) exige el permiso específico definido en `docs/10-permisos.md`.
  - acciones sensibles (marcadas SENSITIVE) exigen permiso + `reason` (min 10) + auditoría before/after (si la infraestructura ya existe; si no, registrar bloqueo).
- Regla anti-invento:
  - NO crear permisos nuevos salvo que sea estrictamente necesario; si falta un permiso para un endpoint real, levantar RFC y bloquear.
- Mínimo obligatorio a cubrir en este sprint:
  - Auth/admin/config IVA:
    - `CONFIG_TAX_READ`, `CONFIG_TAX_UPDATE`
    - `ROLE_ASSIGN` (si existe endpoint)
    - `USER_*` (si existe endpoint)
  - Reportes:
    - `REPORT_READ`, `REPORT_EXPORT`
  - Auditoría:
    - `AUDIT_READ` (si existe endpoint)
  - Agenda:
    - `APPT_OVERBOOK`, `APPT_CANCEL` (si existen endpoints)
  - Facturación:
    - `INVOICE_VOID` (si existe endpoint)
  - Inventario:
    - `STOCK_ADJUST`, `STOCK_OVERRIDE_INVOICE` (si existen endpoints)

5) **Smoke script**
- Crear `scripts/smoke/spr-b010.ps1` con pruebas mínimas:
  - login normal ok,
  - lockout (intentos fallidos → lock) o al menos validación de respuesta “locked”,
  - rate limit login (reintentos → 429) **si el rate limit está habilitado**,
  - 2FA: setup + enable + login con challenge → completar 2FA (si se puede con credenciales demo).

6) **Docs y trazabilidad**
- Al cerrar sprint:
  - actualizar `docs/status/status.md` a `READY_FOR_VALIDATION` (nunca DONE),
  - append en `docs/log/log.md`,
  - actualizar `docs/traceability/rtm.md` (BRD-REQ-004/005/006/009 → SPR-B010 + evidencia),
  - actualizar `docs/state/state.md` (next sprint recomendado: `SPR-B011`).

### Excluye

- UI de 2FA (eso es FRONT; aquí solo API).
- Integraciones externas (SMS/email).
- Rate limit distribuido (Redis, etc.) — solo local/demo.
- Cambios de arquitectura mayores (solo hardening incremental).

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio (si no, DETENER).
- `git config user.name` y `git config user.email` existen (si no, DETENER).
- `git remote -v` coincide con `docs/project-lock.md` (si no, DETENER).
- Rama actual: `git rev-parse --abbrev-ref HEAD`.
- Existe este sprint: `docs/sprints/spr-b010.md`.
- Lectura obligatoria (en este orden):
  - `AGENTS.md` (si existe)
  - `docs/project-lock.md`
  - `docs/00-indice.md`
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - `docs/02-brd.md` (BRD-REQ-004/005/006/009)
  - `docs/03-arquitectura.md`
  - `docs/04-convenciones.md`
  - `docs/05-seguridad.md`
  - `docs/10-permisos.md`
  - ADRs: `docs/decisions/adr-0004-seguridad-auth.md`
  - `docs/traceability/rtm.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
- Validar DoR:
  - Permisos relevantes existen en `docs/10-permisos.md`.
  - Config defaults para lockout/rate limit están definidos (si no existen en config actual, implementar como properties con defaults del BRD).

## 4) Entregables

- Backend:
  - 2FA TOTP: persistencia + endpoints + flujo login challenge.
  - Lockout: conteo intentos + `locked_until` + respuestas y auditoría.
  - Rate limit: login/refresh/report endpoints con 429 Problem Details.
  - Permisos finos: hardening de endpoints existentes (mínimo módulos listados).
  - Tests (mínimos):
    - 2FA enable + login con 2FA
    - lockout: 4 fallos → locked
    - rate limit: exceder → 429
    - permisos: endpoint sensible retorna 403 sin permiso
- Scripts:
  - `scripts/smoke/spr-b010.ps1`
- Docs:
  - `docs/status/status.md`
  - `docs/log/log.md`
  - `docs/traceability/rtm.md`
  - `docs/state/state.md`
  - `docs/changelog.md` (si el repo lo está usando para registrar cambios)

## 5) Instrucciones de implementación (cerradas)

1) **2FA (TOTP)**
- Implementar generador/validador TOTP (sin inventar dependencias si ya hay patrón; si no, librería mínima o implementación estándar).
- Setup:
  - Generar secret (base32).
  - Retornar `otpauth://totp/...` y metadata necesaria (issuer = proyecto; account = email).
- Enable:
  - Validar código TOTP; si ok, marcar `totp_enabled=true`.
- Login:
  - Si el rol es ADMIN/SUPERADMIN y enforcement está ON:
    - Si `totp_enabled=true`, exigir paso 2FA.
    - Si `totp_enabled=false`, decidir:
      - o bloquear login con mensaje “debe activar 2FA” (solo si está definido así), o
      - permitir login y forzar activación en UI (documentar).  
    - Si este punto NO está cerrado en docs, crear RFC y bloquear (no inventar).

2) **Lockout**
- Contar intentos fallidos por usuario en ventana de 15 min (default).
- Aplicar lock 15 min (default) escribiendo `locked_until`.
- Configurable por properties.
- Registrar auditoría de intentos y lock si el audit service existe.

3) **Rate limit**
- Implementar limiter y aplicarlo en:
  - login
  - refresh
  - endpoints `reports/*` y `export` (si existen)
- Responder 429 Problem Details + Retry-After si aplica.

4) **Permisos finos**
- Para cada endpoint existente en los módulos mínimos, asegurar `@PreAuthorize` (o equivalente) con el permiso correcto.
- Acciones sensibles:
  - validar `reason` (min 10) en request,
  - integrar auditoría before/after si existe infraestructura (si no, RFC + bloquear solo esa parte si impide cumplir BRD).

5) **OpenAPI**
- Documentar:
  - flujo 2FA (challenge + completar),
  - errores de lockout/rate limit (401/423/429),
  - permisos requeridos por endpoint (si el repo ya lo documenta).

6) **Evidencia**
- Actualizar RTM + state al cierre del sprint (READY_FOR_VALIDATION).

## 6) Criterios de aceptación (AC)

- [ ] `./mvnw test` pasa.
- [ ] 2FA:
  - [ ] existe setup + enable (ADMIN/SUPERADMIN).
  - [ ] login de ADMIN/SUPERADMIN con 2FA habilitado devuelve challenge y requiere completar para tokens.
- [ ] Lockout:
  - [ ] 4 intentos fallidos → usuario bloqueado (default) y responde con error claro.
- [ ] Rate limit:
  - [ ] al exceder límites en login/refresh/reportes → 429 con Problem Details.
- [ ] Permisos finos:
  - [ ] endpoints sensibles mínimos están protegidos con permisos de `docs/10-permisos.md`.
- [ ] Smoke:
  - [ ] `scripts/smoke/spr-b010.ps1` existe y corre contra backend local (o deja placeholder explícito si depende de seeds que vienen en SPR-B011).
- [ ] Evidencia docs:
  - [ ] `docs/log/log.md` entrada append-only `SPR-B010`
  - [ ] `docs/status/status.md` fila `SPR-B010` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualizado: BRD-REQ-004/005/006/009
  - [ ] `docs/state/state.md` actualizado con next sprint recomendado: `SPR-B011`

## 7) Smoke test manual (usuario)

1) Levantar backend:
- `cd backend`
- `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b010.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada `SPR-B010` (o dejar placeholder explícito).

## 8) Comandos verdad

- Backend:
  - `cd backend`
  - `./mvnw test`
  - `./mvnw spring-boot:run`
- Docs verify:
  - `pwsh -File scripts/verify/verify-docs-eof.ps1`

## 9) DoD

- Cumple `docs/quality/definition-of-done.md` además de AC.
- `docs/status/status.md` queda en `READY_FOR_VALIDATION` (nunca DONE por Codex).
- `docs/log/log.md` append-only con comandos + outputs/placeholder.
- `docs/traceability/rtm.md` actualizado con evidencia/verificación.
- `docs/state/state.md` actualizado (snapshot + next sprint).

## 10) Si hay huecos/contradicciones

- Prohibido editar este sprint.
- Si el flujo exacto de enforcement 2FA (bloquear vs permitir y forzar setup) NO está cerrado en docs:
  - crear RFC y **BLOQUEAR** (no inventar).
- Si falta un permiso necesario para proteger un endpoint real:
  - RFC + BLOQUEAR (no inventar permisos).
- Si no existe infraestructura mínima para auditoría requerida por acciones sensibles:
  - RFC + BLOQUEAR solo lo que impida cumplir BRD.

<!-- EOF -->
