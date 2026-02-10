# SPR-B008 — Reportes (endpoints + export)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 2  
**Duración objetivo:** 45–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Entregar un set de reportes backend (branch-scoped) con filtros y export, usable para demo local-first:
  - Reporte citas por período (filtros: sala, estado).
  - Reporte ventas por período (facturas/pagos).
  - Reporte top servicios (por cantidad y por ingresos).
  - Reporte consumo inventario (movimientos CONSUME/OUT vinculados a visitas).
  - Reporte clientes/pacientes frecuentes.
  - Dashboard home por rol (resumen rápido).
  - Export de reportes CSV/PDF con el MISMO enfoque/librería que ya exista en el repo (no introducir otra).
- Cierra requisitos:
  - **BRD-REQ-045** Reporte “citas por período”
  - **BRD-REQ-046** Reporte “ventas por período”
  - **BRD-REQ-047** Reporte “top servicios”
  - **BRD-REQ-048** Reporte “consumo inventario”
  - **BRD-REQ-049** Reporte “clientes/pacientes frecuentes”
  - **BRD-REQ-050** Exportar reportes CSV/PDF
  - **BRD-REQ-051** Dashboard home por rol

## 2) Alcance

### Incluye

- API branch-scoped (`X-Branch-Id`) y permisos (según `docs/10-permisos.md`):
  - Lectura de reportes: `REPORT_READ`
  - Export de reportes: `REPORT_EXPORT`
- Endpoints (naming en inglés, consistente con arquitectura):

  1) **Appointments report**
  - `GET /api/v1/reports/appointments?from=&to=&roomId=&status=&groupBy=day|week`
  - Export:
    - `GET /api/v1/reports/appointments/export.csv?...`
    - `GET /api/v1/reports/appointments/export.pdf?...`

  2) **Sales report**
  - `GET /api/v1/reports/sales?from=&to=&groupBy=day|week&paymentMethod=&status=PAID|VOID|PENDING`
  - Export:
    - `GET /api/v1/reports/sales/export.csv?...`
    - `GET /api/v1/reports/sales/export.pdf?...`

  3) **Top services**
  - `GET /api/v1/reports/top-services?from=&to=&metric=count|revenue&limit=`
  - Export:
    - `GET /api/v1/reports/top-services/export.csv?...`
    - `GET /api/v1/reports/top-services/export.pdf?...`

  4) **Inventory consumption**
  - `GET /api/v1/reports/inventory-consumption?from=&to=&productId=&groupBy=product|service`
  - Export:
    - `GET /api/v1/reports/inventory-consumption/export.csv?...`
    - `GET /api/v1/reports/inventory-consumption/export.pdf?...`

  5) **Frequent clients/pets**
  - `GET /api/v1/reports/frequent?from=&to=&limit=&dimension=client|pet`
  - Export:
    - `GET /api/v1/reports/frequent/export.csv?...`
    - `GET /api/v1/reports/frequent/export.pdf?...`

  6) **Dashboard**
  - `GET /api/v1/dashboard` (branch-scoped, `REPORT_READ`)
    - Retorna KPIs mínimos (ver 5.6).

- Reglas de negocio / datos:
  - Todos los reportes son **por sucursal** (branch_id).
  - `from/to` obligatorios en reportes por período (422 si faltan).
  - Fechas ISO-8601 con timezone; si se reciben fechas sin TZ, interpretarlas en `America/Guayaquil` (documentar en OpenAPI si el repo ya lo hace).
  - Paginación:
    - Si el reporte devuelve “listado” (ej: citas), usar `page/size/sort` si el estándar ya existe.
    - Si es “agregado” (ej: top services), devolver lista completa con `limit` (default razonable).
  - Formato de error: Problem Details (RFC7807) según convención existente.

- Export:
  - CSV siempre disponible.
  - PDF “imprimible” simple:
    - Reusar el mecanismo/librería de PDF que ya exista (por ejemplo, si se implementó en facturación).
    - Si NO existe aún ningún PDF util, crear RFC en vez de introducir dependencia sin consenso (ver sección 10).

- Smoke script:
  - `scripts/smoke/spr-b008.ps1`:
    - llama al menos 3 reportes + 1 export CSV + 1 export PDF (si existe soporte PDF) usando datos seed/demo.

- Docs:
  - Actualizar `docs/08-runbook.md` (cómo correr reportes + smoke).
  - Actualizar RTM + state + status + log al cierre.

### Excluye

- Reportes online-only (envíos, integraciones externas).
- BI avanzado (gráficas, cubos, etc.) — eso es frontend.
- “Multi-tenant” (BRD dice NO).

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio (si no, DETENER).
- `git config user.name` y `git config user.email` existen (si no, DETENER).
- `git remote -v` coincide con `docs/project-lock.md` (repo_url) (si no, DETENER).
- Rama actual: `git rev-parse --abbrev-ref HEAD`.
- Lectura obligatoria (en este orden):
  - `docs/project-lock.md`
  - `AGENTS.md` (si existe)
  - `docs/00-indice.md`
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - `docs/02-brd.md` (BRD-REQ-045..051)
  - `docs/03-arquitectura.md`
  - `docs/04-convenciones.md`
  - `docs/05-seguridad.md` (rate limit puede afectar reportes en B010; aquí solo respetar lo existente)
  - `docs/06-dominio-parte-a.md` + `docs/06-dominio-parte-b.md`
  - `docs/08-runbook.md`
  - `docs/10-permisos.md` (REPORT_READ, REPORT_EXPORT)
  - `docs/traceability/rtm.md`
  - `docs/sprints/spr-master-back.md`
  - Este sprint: `docs/sprints/spr-b008.md`
  - `docs/status/status.md`
  - `docs/log/log.md`

## 4) Entregables

- Backend:
  - Controladores/servicios de reportes con queries (JPA o SQL nativo según convención existente).
  - DTOs estables para resultados.
  - Validaciones y Problem Details.
  - Permisos aplicados (`REPORT_READ`, `REPORT_EXPORT`).
- Export:
  - CSV para cada reporte.
  - PDF para cada reporte (solo si ya existe mecanismo/librería; si no, RFC + bloquear PDF).
- Scripts:
  - `scripts/smoke/spr-b008.ps1`
- Docs:
  - `docs/08-runbook.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - `docs/traceability/rtm.md` (BRD-REQ-045..051)
  - `docs/state/state.md` (next sprint recomendado: `SPR-B009`)

## 5) Instrucciones de implementación (cerradas)

### 5.1 Fuente de datos por reporte (no inventar tablas nuevas)

- Appointments report:
  - Usa entidad/tablas de citas (appointment) + room si existe.
  - Campos mínimos por fila: `start`, `end`, `status`, `roomName`, `clientName`, `petName` (si join es simple; si no, retornar ids y texto básico).
- Sales report:
  - Usa `invoice` + `invoice_payment` (si existen) + estado de invoice.
  - Entregar agregados:
    - totalFacturado (base+tax)
    - totalImpuesto
    - totalCobrado
    - breakdown por método (opcional si existe `method` en pagos).
- Top services:
  - Usa items de factura o referencia a servicios en visitas (según implementación real):
    - Si la factura registra items SERVICE con `item_id` y `description`, usar eso.
    - Si no existe, usar `visit.service_id` (si aplica) pero no inventar.
- Inventory consumption:
  - Usa `stock_movement` filtrando `type in (CONSUME, OUT)` y rango de fechas.
  - Retorna agregados: qty total, costo total, top N productos.
- Frequent:
  - Usa visitas o citas por cliente/pet:
    - “frecuentes” = top por conteo en rango.
  - Retorna: entityId + displayName + count.

### 5.2 Validaciones (mínimo)

- `from` y `to`:
  - requeridos en reportes por período; 422 si falta.
  - `from <= to` (422 si no).
- `limit`:
  - default 10, max 100 (422 si excede).
- `groupBy`:
  - permitir solo `day` o `week`; default `day`.

### 5.3 Respuesta (contratos mínimos)

- Listados:
  - `{ items: [...], page, size, total }` si el estándar ya existe.
- Agregados:
  - `{ from, to, totals: {...}, series: [...], breakdown: [...] }` (mantener simple y documentar en OpenAPI).

### 5.4 Export CSV

- CSV debe incluir:
  - Encabezado con metadatos (`report`, `branch`, `from`, `to`, `generatedAt`)
  - Luego tabla de datos (headers consistentes).
- Content-Type:
  - `text/csv`
- Nombre archivo sugerido:
  - `report-<name>-<from>-<to>.csv`

### 5.5 Export PDF

- PDF “simple imprimible”:
  - Título + rango + tabla(s) básicas.
- Reusar util/librería existente:
  - Si el repo ya tiene generador PDF (factura/indicaciones), reutilizarlo.
  - Si NO existe, NO introducir PDF aquí: crear RFC `docs/rfcs/` para elegir librería y bloquear PDF (ver sección 10).

### 5.6 Dashboard home por rol (BRD-REQ-051)

Endpoint: `GET /api/v1/dashboard`

- Respuesta mínima (branch-scoped):
  - `todayAppointmentsCount`
  - `todayInProgressVisitsCount`
  - `todaySalesTotal` (si hay facturas)
  - `lowStockCount` (si inventario existe)
  - `topServicesThisWeek` (opcional si el reporte está disponible)
- Permisos:
  - `REPORT_READ`
- No inventar “rol->widgets” complejos: solo devolver data; el FRONT decide.

## 6) Criterios de aceptación (AC)

- [ ] Backend compila y tests pasan: `./mvnw test`.
- [ ] Todos los endpoints report `GET` responden 200 con filtros válidos.
- [ ] Validaciones:
  - [ ] falta `from/to` donde aplique → 422.
  - [ ] `from>to` → 422.
  - [ ] `limit>100` → 422.
- [ ] Seguridad:
  - [ ] sin `X-Branch-Id` en endpoints branch-scoped → 400 (según convención).
  - [ ] sin `REPORT_READ` → 403.
  - [ ] export requiere `REPORT_EXPORT`.
- [ ] Export:
  - [ ] CSV de al menos 2 reportes descarga correcto.
  - [ ] PDF:
    - [ ] si existe util/librería: PDF descarga correcto
    - [ ] si NO existe: RFC creado y el sprint queda BLOCKED para PDF (no marcar READY_FOR_VALIDATION).
- [ ] Smoke:
  - [ ] `scripts/smoke/spr-b008.ps1` existe y ejecuta llamadas a reportes y export (según disponibilidad).
- [ ] Evidencia docs:
  - [ ] `docs/log/log.md` entrada append-only `SPR-B008`
  - [ ] `docs/status/status.md` fila `SPR-B008` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualizado: BRD-REQ-045..051
  - [ ] `docs/state/state.md` actualizado con next sprint recomendado: `SPR-B009`

## 7) Smoke test manual (usuario)

1) Levantar backend:
- `cd backend`
- `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b008.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada de `SPR-B008` (o dejar placeholder explícito).

## 8) Comandos verdad

- Backend:
  - `cd backend`
  - `./mvnw test`
  - `./mvnw spring-boot:run`
- Docs verify:
  - `pwsh -File scripts/verify/verify-docs-eof.ps1`

## 9) DoD

- Cumple `docs/quality/definition-of-done.md` (además de AC).
- `docs/status/status.md` queda en `READY_FOR_VALIDATION` (nunca DONE por Codex).
- `docs/log/log.md` append-only con comandos + outputs/placeholder.
- `docs/traceability/rtm.md` actualizado con evidencia/verificación.
- `docs/state/state.md` actualizado (snapshot + next sprint).

## 10) Si hay huecos/contradicciones

- Prohibido editar este sprint.
- Si faltan permisos `REPORT_READ` / `REPORT_EXPORT` en `docs/10-permisos.md` (o difieren):
  - RFC y BLOQUEAR (no inventar).
- Si NO existe aún un mecanismo/librería de PDF en el repo:
  - Crear RFC: `docs/rfcs/RFC-00xx-report-pdf-lib.md` proponiendo opciones (PDFBox vs OpenPDF, etc.)
  - Actualizar `docs/changelog.md`
  - Marcar `SPR-B008` como `BLOCKED` (porque BRD-REQ-050 exige PDF) y DETENER.

<!-- EOF -->
