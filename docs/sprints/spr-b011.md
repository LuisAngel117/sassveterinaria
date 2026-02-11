# SPR-B011 — Seeds demo + Smoke scripts flujo core

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 3  
**Duración objetivo:** 60–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Entregar un **demo setup usable** para validar el backend en minutos (local-first), cerrando:
  - **BRD-REQ-055** Seed/demo (usuarios/roles, servicios, clientes, mascotas, 1–2 citas y 1 atención ejemplo).
  - **BRD-REQ-056** Credenciales demo fijas (para prueba rápida).
  - **BRD-REQ-058** Smoke script flujo core: “crear cita → atender → cerrar → facturar”.
- Dejar scripts y runbook listos para reproducibilidad + evidencia en LOG/STATUS/RTM/state.

## 2) Alcance

### Incluye

**BRD objetivo:** BRD-REQ-055, BRD-REQ-056, BRD-REQ-058.

1) **Seeds demo (idempotentes)**
- Implementar un mecanismo de seed demo **idempotente** (no duplica datos si se corre más de una vez), alineado con el patrón real del repo:
  - Si ya existe seeding (Flyway repeatable, CommandLineRunner, etc.): **extender**.
  - Si NO existe: crear el mínimo sin romper arquitectura (ver sección 5).
- Dataset mínimo requerido por BRD-REQ-055:
  - **Usuarios demo** (credenciales fijas exactamente como runbook; ver punto 2).
  - **1 branch** (sucursal) activa (code + name).
  - **rooms**: al menos 1 sala/consultorio activo para no-solape por sala.
  - **servicios**: al menos 3 servicios con duración y precio base.
  - **clientes**: al menos 1 cliente.
  - **mascotas**: al menos 1 mascota asociada al cliente.
  - **citas**: 1–2 citas (una creada sin solape) usando branch + room + servicio + mascota.
  - **atención** (visit/encounter): al menos 1 atención de ejemplo (puede estar cerrada).
- Si existen módulos ya implementados (facturación/inventario), el seed puede incluir:
  - 1 factura pagada demo y/o 1 movimiento inventario demo
  - **solo si es trivial** con el modelo real; si no, se deja al smoke core (no inventar).

2) **Credenciales demo fijas (BRD-REQ-056)**
- Asegurar que existen exactamente estas credenciales (según `docs/08-runbook.md`):
  - SUPERADMIN: `superadmin` / `SuperAdmin123!`
  - ADMIN: `admin` / `Admin123!`
  - RECEPCION: `recepcion` / `Recepcion123!`
  - VETERINARIO: `veterinario` / `Veterinario123!`
- Nota de consistencia: si el modelo usa `email` como “usuario”, se guarda ese valor literal en el campo correspondiente (sin inventar un email real).
- Asignar roles correctos y branch access (user_branch) con **is_default** al menos para ADMIN/RECEPCION/VETERINARIO.

3) **Smoke script flujo core (BRD-REQ-058)**
- Crear `scripts/smoke/spr-b011.ps1` que ejecute el flujo core end-to-end contra backend local:
  1) login RECEPCION
  2) obtener branch_id usable (sin hardcode):
     - preferir `GET /api/me` (si existe) o `GET /api/branches` (si existe) para tomar branch por defecto
  3) crear cita (appointment) con:
     - mascota demo existente (o crear cliente+mascota si el endpoint existe y es más simple)
     - servicio demo existente
     - room demo existente
  4) login VETERINARIO
  5) iniciar atención desde la cita (start visit) y completar/cerrar SOAP mínimo
  6) login ADMIN o RECEPCION (según permisos reales)
  7) facturar: crear factura desde la atención o por cita (según modelo real), aplicar IVA actual, y registrar pago
  8) validar respuesta final: estado pagado y/o entidad cerrada
- Reglas anti-invento:
  - El script debe usar **endpoints reales** del repo. Codex debe leer controllers/OpenAPI para construir las rutas exactas.
  - Si faltan endpoints para completar el flujo, levantar RFC y bloquear (ver sección 10).

4) **Runbook y trazabilidad**
- Al ejecutar el sprint:
  - Actualizar `docs/08-runbook.md` (sección demo) para referenciar el smoke `spr-b011.ps1` y explicar cómo habilitar seed demo (property/perfil).
  - Actualizar `docs/status/status.md` a `READY_FOR_VALIDATION` (nunca DONE).
  - Append en `docs/log/log.md` con comandos y outputs/placeholders.
  - Actualizar `docs/traceability/rtm.md`:
    - BRD-REQ-055/056/058 → SPR-B011 + evidencia (commit hash) + método verificación (smoke).
  - Actualizar `docs/state/state.md` (snapshot + next recomendado: “iniciar FRONT” si el BACK queda listo).

### Excluye

- Cambios grandes de arquitectura (solo mecanismo seed mínimo).
- UI demo (esto es FRONT).
- Reset destructivo de DB por defecto (drop schema) salvo que ya exista patrón y esté explícito en docs.
- Inventar nuevos permisos/roles: se usan los definidos en `docs/10-permisos.md`.

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio (si no, DETENER).
- `git config user.name` y `git config user.email` existen (si no, DETENER).
- `git remote -v` coincide con `docs/project-lock.md` (si no, DETENER).
- Rama actual: `git rev-parse --abbrev-ref HEAD`.
- Existe este sprint: `docs/sprints/spr-b011.md`.
- Lectura obligatoria (en este orden):
  - `AGENTS.md` (si existe)
  - `docs/project-lock.md`
  - `docs/00-indice.md`
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - `docs/02-brd.md` (BRD-REQ-055/056/058)
  - `docs/03-arquitectura.md`
  - `docs/04-convenciones.md`
  - `docs/05-seguridad.md`
  - `docs/06-dominio-parte-a.md` + `docs/06-dominio-parte-b.md`
  - `docs/08-runbook.md` (usuarios demo + demo flow)
  - `docs/10-permisos.md`
  - `docs/traceability/rtm.md`
  - `docs/sprints/spr-master-back.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
- Validar DoR:
  - RTM ya mapea BRD-REQ-055/056/058 a SPR-B011 (existe).
  - El sprint define smoke + AC verificables.

## 4) Entregables

- Backend:
  - Mecanismo seed demo idempotente (según patrón real del repo).
  - Dataset mínimo de demo (ver sección 2).
- Scripts:
  - `scripts/smoke/spr-b011.ps1` (flujo core end-to-end).
- Docs (al ejecutar el sprint):
  - `docs/08-runbook.md` actualizado (demo + cómo habilitar seed)
  - `docs/status/status.md` READY_FOR_VALIDATION con evidencia (commit)
  - `docs/log/log.md` entrada append-only (con outputs/placeholders)
  - `docs/traceability/rtm.md` actualizado (055/056/058)
  - `docs/state/state.md` actualizado (snapshot + next step)
  - `docs/changelog.md` (si el repo lo usa)

## 5) Instrucciones de implementación (cerradas)

1) **Elegir mecanismo de seed (sin inventar)**
- Revisar si ya existe:
  - Flyway repeatable seed (`R__*.sql`) con `ON CONFLICT DO NOTHING`, o
  - `DemoDataSeeder`/`CommandLineRunner`, o
  - endpoints admin de demo (solo si ya están en diseño).
- Implementar/expandir el mecanismo existente.
- Si no existe nada:
  - Crear un `DemoDataSeeder` mínimo que corra **solo** cuando una property esté activa, por ejemplo:
    - `app.demo.seed.enabled=true` (default true en perfil local/dev)  
  - Debe ser idempotente: buscar por `code`/campos únicos antes de crear.

2) **Sembrar credenciales demo (exactas)**
- Crear/asegurar usuarios:
  - `superadmin`, `admin`, `recepcion`, `veterinario`
- Aplicar password hashing conforme al sistema real (bcrypt/argon2 según implementación).
- Asignar roles correctos y branch access.
- Si existe 2FA (SPR-B010):
  - dejar TOTP deshabilitado por defecto (salvo que docs indiquen enforcement ON y requiera habilitar; si hay contradicción, RFC + bloquear).

3) **Sembrar datos mínimos del dominio**
- Crear/asegurar:
  - branch: `code` estable (ej. `BR-001`) + name
  - room(s): `code` estable (ej. `ROOM-01`) + name
  - services: `code` estable (ej. `SVC-CONSULTA`, `SVC-VACUNA`, `SVC-DESPARA`) con duración y precio
  - client + pet (mínimo 1)
  - 1–2 appointments sin solape (si el modelo requiere `start/end`, usar tiempos válidos)
  - 1 visit/encounter de ejemplo (si existe el modelo en ese momento)
- No forzar IDs fijos; usar claves naturales (code) para idempotencia.

4) **Smoke script spr-b011.ps1**
- Debe:
  - Hacer login con credenciales demo.
  - Determinar branch_id dinámicamente (no hardcode).
  - Usar `X-Branch-Id` en requests branch-scoped.
  - Ejecutar flujo core creando lo necesario si no existe (client/pet/appointment).
  - Validar (assert) respuestas HTTP y prints claros.
- Si el backend usa Problem Details:
  - el script debe imprimir `title`, `status`, `detail` al fallar.

5) **Actualizar runbook**
- En `docs/08-runbook.md`, asegurar que:
  - credenciales demo están listadas (ya existen en el doc; mantener consistencia),
  - se explica cómo correr:
    - `pwsh -File scripts/smoke/spr-b011.ps1`
  - se explica cómo habilitar/ejecutar seed demo (property/perfil real).

6) **Evidencia y cierre**
- Ejecutar comandos “verdad” del sprint.
- Actualizar LOG/STATUS/RTM/state según DoD.

## 6) Criterios de aceptación (AC)

- [ ] Existe mecanismo de seed demo idempotente y no duplica datos al re-ejecutar.
- [ ] Existen credenciales demo exactas (4 usuarios) y pueden autenticarse.
- [ ] Existe dataset mínimo seed:
  - [ ] branch + room + 3 services
  - [ ] 1 client + 1 pet
  - [ ] 1–2 appointments
  - [ ] 1 visit/encounter ejemplo (si el modelo existe en el repo al ejecutar)
- [ ] Existe `scripts/smoke/spr-b011.ps1` y ejecuta el flujo core completo:
  - [ ] crear cita
  - [ ] atender + cerrar
  - [ ] facturar + pagar
  - [ ] confirma estado final OK (pagado/cerrado)
- [ ] `./mvnw test` pasa.
- [ ] Docs/evidencia al cierre:
  - [ ] LOG append-only con outputs/placeholders
  - [ ] STATUS: SPR-B011 READY_FOR_VALIDATION con hash
  - [ ] RTM actualizado para BRD-REQ-055/056/058
  - [ ] state snapshot actualizado con “next step”

## 7) Smoke test manual (usuario)

1) Levantar backend:
- (según estructura real) `cd backend` y `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b011.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada `SPR-B011` (o dejar placeholders explícitos si el usuario no pega en chat).

## 8) Comandos verdad

- Backend:
  - (según estructura real) `./mvnw test`
  - (según estructura real) `./mvnw spring-boot:run`
- Smoke:
  - `pwsh -File scripts/smoke/spr-b011.ps1`
- Docs verify:
  - `pwsh -File scripts/verify/verify-docs-eof.ps1`

## 9) DoD

- Cumple `docs/quality/definition-of-done.md` además de AC.
- `docs/status/status.md` queda `READY_FOR_VALIDATION` (nunca DONE por Codex).
- `docs/log/log.md` append-only con comandos + outputs/placeholders.
- `docs/traceability/rtm.md` actualizado con evidencia/verificación.
- `docs/state/state.md` actualizado (snapshot + next step recomendado).

## 10) Si hay huecos/contradicciones

- Prohibido editar este sprint.
- Prohibido inventar endpoints o rutas en el smoke:
  - si no hay endpoints suficientes para completar el flujo core:
    1) crear RFC en `docs/rfcs/` describiendo el hueco exacto (qué falta, dónde)
    2) actualizar `docs/changelog.md` (si aplica)
    3) marcar `SPR-B011` como `BLOCKED` en `docs/status/status.md`
    4) registrar en `docs/log/log.md` el motivo exacto
    5) detener sin romper el repo

<!-- EOF -->
