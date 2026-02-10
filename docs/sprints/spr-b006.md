# SPR-B006 — Facturación (factura + IVA config + pagos + anulación + export)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 2  
**Duración objetivo:** 45–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Entregar **Facturación v1** usable en local, asociada a una **Atención/Visit**:
  - Crear factura interna simple desde una atención (visit).
  - Items (SERVICE/PRODUCT) + descuentos por ítem y por total.
  - IVA configurable (default 15%): **solo SUPERADMIN** puede actualizar; el cambio queda auditado.
  - Pagos múltiples (efectivo/tarjeta/transferencia/mixto) con pagos parciales.
  - Estados factura: `PENDING` / `PAID` / `VOID`.
  - Anulación requiere `reason` + auditoría before/after.
  - Exportar factura (CSV + PDF) para demo local.
- Cerrar requisitos:
  - **BRD-REQ-031** Factura interna asociada a una atención
  - **BRD-REQ-032** IVA configurable (auditado)
  - **BRD-REQ-033** Descuentos por ítem y/o total
  - **BRD-REQ-034** Pagos parciales y mixtos
  - **BRD-REQ-035** Estados factura
  - **BRD-REQ-036** Anulación con reason + auditoría
  - **BRD-REQ-037** Export factura CSV/PDF
- Además (por dependencia explícita desde clínica):
  - **BRD-REQ-030** Export indicaciones (PDF/HTML) — mínimo PDF/HTML imprimible desde atención (ver Alcance).

## 2) Alcance

### Incluye

- DB/Flyway (branch-scoped):
  - Ajustar modelo de `invoice` para que sea **visit-scoped** (NO appointment-scoped) según BRD:
    - `invoice.visit_id` requerido (FK -> visit)
    - `appointment_id` se elimina o queda nullable solo si el repo ya lo tenía, pero **NO** se usa como asociación principal.
  - Tablas:
    - `invoice`
    - `invoice_item`
    - `invoice_payment`
    - `tax_config` (o config equivalente) por branch
- API branch-scoped (`X-Branch-Id` + permisos):
  - Config IVA:
    - `GET /api/v1/config/tax` (`CONFIG_TAX_READ`)
    - `PUT /api/v1/config/tax` (`CONFIG_TAX_UPDATE`, SENSITIVE: reason + auditoría)
  - Facturas:
    - `POST /api/v1/visits/{visitId}/invoices` (`INVOICE_CREATE`)
    - `GET /api/v1/invoices/{id}` (`INVOICE_READ`)
    - `GET /api/v1/invoices?from=&to=&status=&q=` (`INVOICE_READ`) (q por invoiceNumber o cliente/mascota si está join-eable; si no, solo invoiceNumber)
    - `PATCH /api/v1/invoices/{id}` (`INVOICE_UPDATE`) (SENSITIVE si cambia descuentos o montos: reason + auditoría)
    - `POST /api/v1/invoices/{id}/void` (`INVOICE_VOID`, SENSITIVE: reason + auditoría)
  - Items:
    - `POST /api/v1/invoices/{id}/items` (`INVOICE_UPDATE`) (solo si factura `PENDING`)
    - `PATCH /api/v1/invoice-items/{itemId}` (`INVOICE_UPDATE`) (SENSITIVE si cambia precios/descuentos)
    - `DELETE /api/v1/invoice-items/{itemId}` (`INVOICE_UPDATE`) (solo si factura `PENDING`)
  - Pagos:
    - `POST /api/v1/invoices/{id}/payments` (`INVOICE_PAY`) (solo si factura no está VOID)
    - `GET /api/v1/invoices/{id}/payments` (`INVOICE_READ`)
    - (Opcional v1) `DELETE /api/v1/payments/{id}` solo si existe permiso explícito; si NO existe, **NO** implementar.
  - Exports:
    - `GET /api/v1/invoices/{id}/export.csv` (`INVOICE_EXPORT`)
    - `GET /api/v1/invoices/{id}/export.pdf` (`INVOICE_EXPORT`)
    - Clínica (por BRD-REQ-030):
      - `GET /api/v1/visits/{id}/instructions.pdf` (o `.html`) (`VISIT_READ`)
- Reglas de negocio (mínimas, sin inventar):
  - Factura se crea desde una **visit** existente del mismo branch.
  - Factura inicia `PENDING`.
  - Al registrar pagos:
    - `paid_total = sum(payments.amount)`
    - si `paid_total >= invoice.total` → `PAID`
    - si `< total` → `PENDING`
  - `VOID` es terminal:
    - no permite agregar items ni pagos
    - requiere `reason` (min 10) + auditoría before/after
  - Cálculo:
    - `itemsSubtotal = sum(lineTotal)`
    - `invoiceDiscountAmount` (>=0) aplica al subtotal (no puede exceder subtotal)
    - `taxableBase = itemsSubtotal - invoiceDiscountAmount`
    - `taxAmount = round(taxableBase * taxRate, 2)`
    - `total = taxableBase + taxAmount`
  - Descuentos:
    - Por ítem: `discountAmount` monetario (USD) (v1).
    - Por total: `invoiceDiscountAmount` monetario (USD) (v1).
- Auditoría:
  - Acciones sensibles (reason + before/after):
    - `CONFIG_TAX_UPDATE`
    - `INVOICE_VOID`
    - `INVOICE_UPDATE` cuando cambia: `unitPrice`, `discountAmount`, `invoiceDiscountAmount`
- Seeds demo:
  - Si no existe, crear `tax_config` por branch con `rate=0.15`.
- Smoke script:
  - `scripts/smoke/spr-b006.ps1` (flujo: crear factura → descuentos → pagos parciales → export → void con reason).
- Docs:
  - Actualizar `docs/06-dominio-parte-a.md` (si contradice) para reflejar **invoice.visit_id**.
  - Actualizar `docs/08-runbook.md` con endpoints y cómo correr el smoke.
  - RTM/state/status/log al cierre.

### Excluye

- Integración con inventario (consumo/stock/override por falta de stock):
  - Se implementa en **SPR-B007** (BRD-REQ-044) cuando exista inventario.
  - En este sprint, los items PRODUCT no ajustan stock.
- Numeración fiscal SRI, facturación electrónica → fuera (BRD dice “no SRI”).
- Reembolsos/cancelaciones de pagos → fuera v1.

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
  - `docs/02-brd.md`
  - `docs/03-arquitectura.md`
  - `docs/04-convenciones.md`
  - `docs/05-seguridad.md`
  - `docs/06-dominio-parte-a.md` + `docs/06-dominio-parte-b.md`
  - `docs/08-runbook.md`
  - `docs/10-permisos.md`
  - `docs/decisions/adr-0005-auditoria.md`
  - `docs/traceability/rtm.md`
  - `docs/sprints/spr-master-back.md`
  - Este sprint: `docs/sprints/spr-b006.md`
  - `docs/status/status.md`
  - `docs/log/log.md`

## 4) Entregables

- DB / Migraciones:
  - `invoice` asociado a `visit` (FK) + montos + descuentos.
  - `invoice_item` con `item_type` (`SERVICE`/`PRODUCT`) + `item_id` + `qty` + `unit_price` + `discount_amount` + `line_total`.
  - `invoice_payment` con método + monto.
  - `tax_config` por branch.
- Backend:
  - Endpoints de config IVA + factura + items + pagos + void + exports.
  - Cálculo consistente y validaciones.
  - Permisos aplicados.
  - Reason required + auditoría before/after en sensibles.
- Scripts:
  - `scripts/smoke/spr-b006.ps1`
- Docs:
  - `docs/06-dominio-parte-a.md` (si aplica)
  - `docs/08-runbook.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - `docs/traceability/rtm.md`
  - `docs/state/state.md`

## 5) Instrucciones de implementación (cerradas)

### 5.1 Modelo de datos (mínimo)

Migración nueva (ej. `V6__billing_invoices.sql` o convención existente).

`tax_config`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id` (unique)
- `tax_rate numeric(6,4) not null` (default 0.1500)
- `updated_by uuid not null fk -> app_user.id`
- `updated_at timestamptz not null`

`invoice`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `visit_id uuid not null fk -> visit.id`
- `invoice_number varchar(40) not null` (unique por branch)
- `status varchar(20) not null` (`PENDING`/`PAID`/`VOID`)
- `items_subtotal numeric(12,2) not null`
- `discount_amount numeric(12,2) not null default 0.00` (descuento total)
- `tax_rate numeric(6,4) not null` (snapshot del config al crear)
- `tax_amount numeric(12,2) not null`
- `total numeric(12,2) not null`
- `void_reason varchar(255) null`
- `voided_at timestamptz null`
- `created_by uuid not null fk -> app_user.id`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

`invoice_item`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `invoice_id uuid not null fk -> invoice.id`
- `item_type varchar(20) not null` (`SERVICE`/`PRODUCT`)
- `item_id uuid not null` (FK lógica: service/product; FK real opcional si es simple)
- `description varchar(200) not null`
- `qty numeric(12,3) not null` (>=0.001)
- `unit_price numeric(12,2) not null` (>=0)
- `discount_amount numeric(12,2) not null default 0.00` (>=0 y <= qty*unit_price)
- `line_total numeric(12,2) not null`
- `created_at timestamptz not null`

`invoice_payment`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `invoice_id uuid not null fk -> invoice.id`
- `method varchar(20) not null` (`CASH`/`CARD`/`TRANSFER`)
- `amount numeric(12,2) not null` (>0)
- `reference varchar(80) null` (opcional: nro transferencia/voucher)
- `created_by uuid not null fk -> app_user.id`
- `created_at timestamptz not null`

Notas:
- Si ya existe `invoice` en el repo con `appointment_id`, NO romper datos:
  - agregar `visit_id` y backfill si posible solo si hay mapeo; si no, dejar `visit_id` nullable temporal con RFC.  
  - Si no existe `invoice` aún (lo normal), crear desde cero con `visit_id` requerido.

### 5.2 Numeración de factura (invoice_number)

- Si el repo ya tiene un generador de correlativos, reutilizarlo.
- Si NO existe:
  - Implementar contador por branch con tabla `invoice_counter(branch_id unique, next_number int)` y `SELECT ... FOR UPDATE` para incrementar.
  - Formato recomendado: `INV-YYYYMM-000001` (por branch).
- No inventar dependencias externas.

### 5.3 Crear factura desde visita

Endpoint: `POST /api/v1/visits/{visitId}/invoices`

Request (JSON):
- `items[]`:
  - `itemType` = `SERVICE` | `PRODUCT`
  - `itemId` (serviceId o productId)
  - `description` (si SERVICE puede default desde service.name; si PRODUCT desde product.name si existe; si no, requerido)
  - `qty`
  - `unitPrice` (si SERVICE default desde service.priceBase; si PRODUCT requerido)
  - `discountAmount` (opcional, default 0)
- `discountAmount` (descuento total, opcional, default 0)

Reglas:
- Validar:
  - `visitId` existe y es del branch.
  - `qty` > 0
  - `unitPrice` >= 0
  - `discountAmount` por ítem y total no exceden bases.
- Calcular montos con fórmula definida en Alcance.
- Guardar `invoice.tax_rate` = config actual del branch.

### 5.4 Actualizar factura / items (solo PENDING)

- Solo permitir modificaciones si `invoice.status == PENDING`.
- Si un PATCH cambia montos (unitPrice/discounts):
  - exigir `reason` (min 10)
  - registrar auditoría before/after.
- `POST /invoices/{id}/items` agrega item y recalcula.
- `PATCH /invoice-items/{itemId}` actualiza qty/precio/descuento y recalcula.
- `DELETE /invoice-items/{itemId}` elimina item y recalcula.

### 5.5 Pagos y estado

`POST /api/v1/invoices/{id}/payments`:
- request: `method`, `amount`, `reference?`
- Regla:
  - No permitir si `invoice.status == VOID`.
  - Recalcular `paid_total`.
  - Si `paid_total >= invoice.total` → set `PAID` (y persistir).
  - No permitir pagos negativos/cero.
- No inventar “cambio”/vuelto; solo registrar pagos.

### 5.6 Void (anulación)

`POST /api/v1/invoices/{id}/void`:
- request: `reason` (min 10)
- Reglas:
  - Requiere permiso `INVOICE_VOID`.
  - Si invoice ya está VOID → 409 (`INVOICE_ALREADY_VOID`).
  - Set status=VOID, set `void_reason`, set `voided_at`.
  - Auditoría before/after obligatoria.

### 5.7 Export CSV/PDF (factura) + export indicaciones (clínica)

- CSV:
  - Contenido mínimo: encabezado factura + items + pagos + totales.
- PDF:
  - PDF simple “imprimible” (no diseño complejo).
  - Si se requiere agregar una librería PDF:
    - hacerlo explícito en el commit del sprint (scope controlado),
    - sin traer dependencias “enterprise”.
- Indicaciones (BRD-REQ-030):
  - Generar salida imprimible desde `visit`:
    - mínimo: datos mascota/cliente + diagnóstico + tratamiento + instrucciones + prescripciones.
  - Puede ser PDF o HTML (pero BRD pide PDF/HTML; se recomienda PDF para consistencia con factura).

### 5.8 Problem Details / errores

- 404 si recurso no existe en branch (no filtrar cross-branch).
- 422 validación (con `errors[]` por campo).
- 409 para estados inválidos (ej. editar factura PAID/VOID, void repetido).

## 6) Criterios de aceptación (AC)

- [ ] Backend compila y tests pasan: `./mvnw test`.
- [ ] IVA config:
  - [ ] `GET /config/tax` funciona.
  - [ ] `PUT /config/tax` solo SUPERADMIN (CONFIG_TAX_UPDATE) y exige `reason` (min 10) + audita before/after.
- [ ] Crear factura desde visita:
  - [ ] Crea invoice `PENDING` con items y cálculos correctos.
  - [ ] Discounts por ítem y total se aplican y no permiten exceder bases.
- [ ] Pagos:
  - [ ] Permite 2 pagos parciales (ej: CASH + TRANSFER).
  - [ ] Cambia estado a `PAID` cuando suma >= total.
- [ ] Actualizaciones:
  - [ ] Agregar item mientras PENDING funciona.
  - [ ] Intentar modificar invoice/items cuando PAID/VOID falla (409).
  - [ ] Cambiar precio/descuento sin `reason` falla (422).
  - [ ] Cambiar precio/descuento con `reason` genera auditoría before/after.
- [ ] Anulación:
  - [ ] `void` sin reason falla (422).
  - [ ] `void` con reason cambia a `VOID` y audita before/after.
  - [ ] En VOID no permite pagos ni items.
- [ ] Export:
  - [ ] CSV descarga correcto.
  - [ ] PDF descarga correcto (invoice).
  - [ ] Indicaciones export (visit) devuelve PDF/HTML imprimible con datos mínimos.
- [ ] Smoke:
  - [ ] `scripts/smoke/spr-b006.ps1` existe y cubre flujo completo.
- [ ] Evidencia docs:
  - [ ] `docs/log/log.md` entrada append-only `SPR-B006`
  - [ ] `docs/status/status.md` fila `SPR-B006` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualizado para BRD-REQ-030..037
  - [ ] `docs/state/state.md` actualizado con next sprint recomendado: `SPR-B007`

## 7) Smoke test manual (usuario)

1) Levantar backend:
- `cd backend`
- `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b006.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada de `SPR-B006` (o dejar placeholder explícito).

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
- Si `docs/06-dominio-parte-a.md` contradice la asociación de factura (appointment vs visit):
  - Actualizar dominio para reflejar BRD (esto NO cambia BRD).
- Si falta una decisión crítica (ej. no existe convención de export PDF o dependencia aprobada):
  - Crear RFC en `docs/rfcs/` + actualizar `docs/changelog.md`.
  - Si bloquea: marcar `SPR-B006` como `BLOCKED`, registrar en LOG y DETENER.

<!-- EOF -->
