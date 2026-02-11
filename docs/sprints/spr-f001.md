# SPR-F001 — Shell + Auth UI + Selector de Sucursal

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)
**Stage:** 1
**Duración objetivo:** 45–90 min (referencial)
**Tipo:** Frontend

## 1) Objetivo

- Entregar un shell vendible mínimo del FRONT con flujo real: **Login → (si aplica) Selección de sucursal → Home**, integrado contra el backend.
- Establecer el “contrato operativo” mínimo del FRONT: base URL por env, cliente API, manejo de sesión, y guardas de rutas.
- Declarar trazabilidad objetivo:
  - BRD-REQ-001 (Login / acceso)
  - BRD-REQ-007 (Selección/escoping por sucursal)
  - BRD-REQ-056 (Credenciales demo visibles/útiles para la demo)

## 2) Alcance

### Incluye

- Detectar el **root real del frontend** dentro del monorepo (sin asumir nombre de carpeta) y trabajar **solo ahí**.
- Pantalla de **Login** (usuario + contraseña) consumiendo el endpoint real del backend (tomado de OpenAPI o scripts smoke existentes).
- Lógica de sesión mínima:
  - almacenar token(es) según el contrato real del backend (sin inventar campos),
  - persistencia mínima para refresh del navegador,
  - logout.
- Pantalla de **Selección de sucursal** si el backend expone más de una sucursal permitida para el usuario (o auto-selección si solo hay una).
- Guardas:
  - si NO hay sesión → redirigir a `/login`,
  - si hay sesión pero NO hay sucursal seleccionada y el backend requiere scope → ir a `/select-branch`.
- Shell básico (layout + navegación mínima) con textos en español y aspecto “demo profesional” (sin pantallas core todavía).
- Mostrar “Credenciales demo” en UI (helper) **sin hardcodear contraseñas** si no están definidas en docs/runbook: deben leerse del `docs/08-runbook.md` (sección de seed/demo) o de scripts/seed existentes en el repo.

### Excluye

- No construir pantallas core (Agenda, Clientes, Mascotas, etc.). Solo enlaces/placeholders, sin CRUD.
- No implementar 2FA/TOTP (eso es SPR-F007).
- No implementar manejo avanzado de permisos/roles en UI (eso se endurece en sprints posteriores; aquí solo base mínima).
- No agregar features no pedidas (p. ej. dashboards completos, calendarios, tablas, etc.).

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio (si no, detener).
- `git config user.name` y `git config user.email` presentes (si no, detener).
- Rama actual (`git rev-parse --abbrev-ref HEAD`).
- Existe este archivo: `docs/sprints/spr-f001.md`.
- Lectura obligatoria antes de tocar código:
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - (si existe) `AGENTS.md`
  - `docs/03-arquitectura.md` (scoping por sucursal / header)
  - `docs/08-runbook.md` (puertos, seeds/credenciales demo, smoke)
  - `docs/sprints/spr-master-front.md`
- Verificar dependencias BACK (sin ejecutar nada):
  - Revisar `docs/status/status.md` y confirmar que lo necesario del BACK para auth+me+scoping+seed está al menos en `READY_FOR_VALIDATION` (si no, bloquear y reportar).

## 4) Entregables

- Frontend:
  - Cliente API base (fetch wrapper o equivalente) con:
    - base URL por env,
    - Authorization según contrato real,
    - header `X-Branch-Id` (solo cuando exista sucursal seleccionada),
    - parseo de errores (mínimo).
  - Rutas/páginas:
    - `/login`
    - `/select-branch`
    - `/` (home placeholder dentro del shell)
  - Estado de sesión (mínimo) + guardas.
- Docs:
  - (solo si el sprint lo requiere al ejecutarse) Actualización mínima de runbook FRONT en `docs/08-runbook.md` explicando cómo levantar el frontend (sin inventar comandos: usar los scripts reales de `package.json`).
- Trazabilidad (al ejecutar el sprint):
  - `docs/traceability/rtm.md` actualizado para BRD-REQ-001/007/056 → SPR-F001 (evidencia commit).
  - `docs/state/state.md` snapshot actualizado.

## 5) Instrucciones de implementación (cerradas)

> REGLA: NO asumas nombres de carpetas, rutas de API, ni shape de JSON. Todo debe salir de:
> - OpenAPI del backend (`/v3/api-docs` o swagger), o
> - scripts smoke existentes en `scripts/smoke/`, o
> - docs/runbook ya existentes.

Pasos:

1) **Identificar FRONT_DIR (root real del frontend)**
   - Desde la raíz del repo, localizar el `package.json` que corresponde al frontend.
   - Criterio: debe contener dependencia `next` (Next.js) o scripts típicos `dev/build`.
   - Si hay más de un candidato:
     - elegir el que esté referenciado por el runbook/índice del repo; si no hay referencia clara → crear RFC `docs/rfcs/rfc-front-root.md` explicando ambigüedad y DETENER.
   - Si NO existe frontend → crear RFC `docs/rfcs/rfc-front-missing.md` y DETENER.

2) **Confirmar modo de routing (sin re-arquitectura)**
   - Detectar si el proyecto usa:
     - App Router: `app/` o `src/app/`
     - Pages Router: `pages/` o `src/pages/`
   - Implementar las páginas dentro del router que YA esté siendo usado.
   - Si no se puede determinar → RFC y DETENER.

3) **Config de entorno (sin inventar)**
   - Crear/asegurar un archivo ejemplo (commit-eable) en FRONT_DIR:
     - `FRONT_DIR/.env.example`
   - Variable mínima:
     - `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080` (solo como ejemplo; el valor real se valida con `docs/08-runbook.md`)
   - Asegurar que `.env.local` esté ignorado por git (si no lo está, agregarlo a `.gitignore` del FRONT_DIR).

4) **Descubrir contrato real de Auth y Me**
   - Localizar en el repo:
     - el script smoke de login (en `scripts/smoke/`), y/o
     - OpenAPI del backend.
   - Extraer de ahí (y usar exactamente):
     - ruta de login (p. ej. `/api/v1/auth/login`),
     - body (nombres de campos exactos),
     - shape de respuesta (tokens/campos),
     - ruta y shape de `/me`.
   - Si no existe fuente de verdad en repo (scripts/OpenAPI) → RFC y DETENER.

5) **Implementar cliente API mínimo**
   - Implementar un módulo único (p. ej. `src/lib/api/client.ts` o equivalente según estructura existente) que:
     - construya la URL con `NEXT_PUBLIC_API_BASE_URL`,
     - setee `Authorization` según el contrato real (por ejemplo Bearer si aplica),
     - agregue `X-Branch-Id` SOLO si hay sucursal seleccionada (ID real),
     - centralice manejo de errores (mínimo: mostrar mensaje legible para 401/403/400).
   - Prohibido crear múltiples clientes paralelos.

6) **Implementar sesión mínima**
   - Crear un “SessionStore” (módulo único) que guarde:
     - tokens (según contrato real),
     - `branchId` seleccionado (si aplica),
   - Persistencia:
     - usar `localStorage` o cookies NO-httpOnly solo si el contrato del backend no soporta cookie; documentar con comentario `// TODO: endurecer storage` si aplica.
   - Implementar `logout()` (limpia store y redirige a `/login`).

7) **UI: Login**
   - Crear pantalla `/login` con:
     - inputs (usuario, contraseña),
     - submit que llama login,
     - en success: llamar `/me` y decidir siguiente paso:
       - si ya hay branch resuelto o solo 1 → set branch y redirigir a `/`
       - si hay varias → redirigir a `/select-branch`
     - bloque “Credenciales demo”:
       - Debe mostrar lo que esté definido en docs/runbook o seed, sin inventar.
       - Si el runbook/seed define usuarios demo (p. ej. `radmin`, `recepcion`, `veterinario`) mostrarlos; si no, ocultar el bloque (no inventar).

8) **UI: Selector de sucursal**
   - Crear pantalla `/select-branch`:
     - obtiene `/me`,
     - muestra lista de sucursales permitidas si existe,
     - al seleccionar: guardar `branchId` y redirigir a `/`.
   - Si `/me` no expone sucursales de ninguna forma:
     - RFC `docs/rfcs/rfc-branch-selection-contract.md` con propuesta de contrato mínimo y DETENER (porque BRD-REQ-007 quedaría imposible sin inventar).

9) **Shell mínimo / Home**
   - Crear layout simple (header + sidebar) y una home `/` que muestre:
     - “Sesión activa: <usuario>”
     - “Sucursal: <branchId o nombre si existe>”
     - enlaces placeholder (sin pantallas core).
   - Guardas:
     - si no hay token → redirect `/login`
     - si el backend requiere branch scope y no hay `branchId` → redirect `/select-branch`

10) **Cierre del sprint**
   - Ejecutar comandos “verdad” (ver sección 8).
   - Actualizar:
     - `docs/log/log.md` (append-only) con outputs o placeholders
     - `docs/status/status.md` → `READY_FOR_VALIDATION` con hash commit
     - `docs/traceability/rtm.md` para BRD-REQ-001/007/056 → evidencia del commit
     - `docs/state/state.md` snapshot y siguiente sprint recomendado
   - Commit: `SPR-F001: shell + login + selector sucursal`

## 6) Criterios de aceptación (AC)

- [ ] Existe `/login` funcional y consume el endpoint real (sin inventar campos).
- [ ] Tras login exitoso:
  - [ ] Se llama `/me` y se decide flujo:
    - [ ] auto-branch si aplica, o
    - [ ] `/select-branch` si hay múltiples.
- [ ] Existe `/select-branch` y guarda `branchId` real.
- [ ] El cliente API agrega `X-Branch-Id` cuando hay `branchId` seleccionado.
- [ ] Guardas de rutas funcionan (sin sesión → `/login`).
- [ ] `npm run build` (o equivalente real del frontend) pasa.
- [ ] Se actualizó `docs/status/status.md` a `READY_FOR_VALIDATION` con evidencia (commit hash).
- [ ] Se append-eó `docs/log/log.md` con sección SPR-F001 y placeholders/outputs.
- [ ] Se actualizó `docs/traceability/rtm.md` para BRD-REQ-001/007/056 apuntando a SPR-F001.
- [ ] `docs/state/state.md` actualizado (snapshot).

## 7) Smoke test manual (usuario)

> Nota: comandos exactos pueden variar por package manager, pero deben salir del `package.json` real del FRONT_DIR.

1) Levantar backend (según `docs/08-runbook.md`) y asegurar seed demo cargado.
2) Levantar frontend:
   - `cd <FRONT_DIR>`
   - `npm install` (o el comando real según lockfile)
   - `npm run dev`
3) En navegador:
   - Ir a `http://localhost:<puerto-que-reporte-next>`
   - Login con credenciales demo definidas en docs/seed/runbook
   - Si aparece selector de sucursal: elegir una
   - Ver home con sesión activa y shell

**Evidencia:** PEGAR OUTPUT EN `docs/log/log.md` en la entrada de SPR-F001.

## 8) Comandos verdad

- Frontend (en `<FRONT_DIR>`):
  - `npm run build`
  - `npm run dev`

Si el proyecto NO tiene esos scripts: **N/A** con razón en LOG + RFC (no inventar).

## 9) DoD

- AC completos.
- `docs/status/status.md` en `READY_FOR_VALIDATION` (nunca DONE).
- `docs/log/log.md` actualizado (append-only).
- Cumplir `docs/quality/definition-of-done.md`.

## 10) Si hay huecos/contradicciones

- Crear RFC/ADR/CHANGELOG según corresponda.
- Detener si bloquea (no inventar contratos ni campos).
- Dejar repo compilable.

<!-- EOF -->
