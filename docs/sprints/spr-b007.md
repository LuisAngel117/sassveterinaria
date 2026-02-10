# SPR-B007 — Inventario (stock + movimientos + costeo + override + BOM)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 2  
**Duración objetivo:** 45–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Entregar **Inventario v1** usable por sucursal (branch) con:
  - Catálogo de productos (medicamento/insumo) + unidades.
  - Stock por sucursal con mínimos y alertas.
  - Movimientos: ingreso/egreso/ajuste/consumo por atención.
  - Costeo promedio ponderado por sucursal.
  - Integración con Facturación: **bloquear facturar por falta de stock**, con **override por permiso** (auditado).
  - BOM (“receta de consumo”) por servicio para consumo automático.
- Cerrar requisitos:
  - **BRD-REQ-038** Productos (medicamento/insumo)
  - **BRD-REQ-039** Catálogo de unidades
  - **BRD-REQ-040** Stock por sucursal
  - **BRD-REQ-041** Movimientos: ingreso/egreso/ajuste/consumo por atención
  - **BRD-REQ-042** Mínimos y alertas
  - **BRD-REQ-043** Costeo: costo promedio ponderado por sucursal
  - **BRD-REQ-044** Bloquear facturar por falta de stock, con override por permiso (auditado)
  - **BRD-REQ-023** Receta de consumo (BOM) por servicio

## 2) Alcance

### Incluye

- DB/Flyway (branch-scoped):
  - `product` (catálogo)
  - `unit` (catálogo unidades)
  - `product_stock` (stock por branch + costo promedio)
  - `stock_movement` (ledger de movimientos con trazabilidad a visita/atención si aplica)
  - `service_bom_item` (BOM: consumo por servicio)
- API branch-scoped (`X-Branch-Id` + permisos **según `docs/10-permisos.md`**, sin inventar códigos):
  - Productos:
    - `POST /api/v1/products`
    - `GET /api/v1/products?q=&active=&lowStock=`
    - `GET /api/v1/products/{id}`
    - `PATCH /api/v1/products/{id}`
  - Unidades:
    - `GET /api/v1/units`
  - Stock:
    - `GET /api/v1/products/{id}/stock`
    - `POST /api/v1/stock/movements` (ingreso/egreso/ajuste)
    - `GET /api/v1/stock/movements?productId=&from=&to=&type=`
    - `GET /api/v1/stock/low` (lista productos bajo mínimo)
  - Consumo por atención:
    - `POST /api/v1/visits/{visitId}/inventory/consume` (consumir por BOM y/o items explícitos)
  - BOM por servicio:
    - `GET /api/v1/services/{serviceId}/bom`
    - `PUT /api/v1/services/{serviceId}/bom` (reemplaza BOM completo)
- Reglas de negocio:
  - Stock por branch (siempre validar scope).
  - Movimientos:
    - `IN` (ingreso): requiere `unitCost` (para promedio).
    - `OUT` (egreso): usa costo promedio actual.
    - `ADJUST`: permite `qtyDelta` (+/-), **requiere reason** y se audita before/after si es sensible (ver BRD-REQ-053).
    - `CONSUME`: asociado a `visit_id` (atención); consume por BOM del servicio de la visita (y/o lista explícita).
  - Mínimos:
    - `min_qty` por producto y branch; `lowStock` cuando `on_hand_qty <= min_qty`.
  - Costeo promedio ponderado por branch:
    - En `IN`: recalcular promedio.
    - En `OUT/CONSUME`: descontar stock sin cambiar promedio (solo registrar costo salida = qty * avg).
  - Integración con Facturación (BRD-REQ-044):
    - Al agregar/modificar **PRODUCT** en factura: validar stock suficiente.
    - Si NO hay stock suficiente: bloquear con error estándar.
    - Override: permitido solo con permiso explícito en `docs/10-permisos.md`, requiere `reason` (min 10) + auditoría before/after.
- Auditoría:
  - Before/after en:
    - overrides por falta de stock (facturación)
    - ajustes inventario (ADJUST) con reason
  - Si el sistema ya tiene auditoría base (SPR-B001), reutilizar el mecanismo; no inventar framework nuevo.
- Seeds demo:
  - Unidades base (ej: `UN`, `ML`, `TABLETA`, `AMPOLLA`) si no existen.
  - 5 productos demo con stock inicial y mínimos.
  - BOM demo para al menos 1 servicio (“Vacunación” consume 1 unidad de vacuna, etc.).
- Smoke script:
  - `scripts/smoke/spr-b007.ps1` (flujo inventario + consumo + bloqueo facturación + override).
- Docs:
  - Actualizar `docs/08-runbook.md` (inventario + storage/props si aplica + smoke B007).
  - RTM/state/status/log al cierre.

### Excluye

- Lotes y caducidad (explicitamente fuera v1 en BRD).
- Inventario multi-almacén por sucursal.
- Reembolsos/retornos complejos a stock (si se requiere, RFC).

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
  - `docs/02-brd.md` (BRD-REQ-023,038..044,053)
  - `docs/03-arquitectura.md`
  - `docs/04-convenciones.md`
  - `docs/05-seguridad.md`
  - `docs/06-dominio-parte-a.md` + `docs/06-dominio-parte-b.md`
  - `docs/08-runbook.md`
  - `docs/10-permisos.md`
  - `docs/decisions/adr-0005-auditoria.md` (si existe en repo; si no, NO inventar: RFC)
  - `docs/traceability/rtm.md`
  - `docs/sprints/spr-master-back.md`
  - Este sprint: `docs/sprints/spr-b007.md`
  - `docs/status/status.md`
  - `docs/log/log.md`

## 4) Entregables

- DB / Migraciones:
  - Tablas: `product`, `unit`, `product_stock`, `stock_movement`, `service_bom_item`.
  - Constraints mínimas (branch-scope).
- Backend:
  - Endpoints inventario + BOM + consumo por visita.
  - Cálculo de costo promedio ponderado.
  - Validación de stock en facturación (PRODUCT items) + override con reason + auditoría.
- Scripts:
  - `scripts/smoke/spr-b007.ps1`
- Docs:
  - `docs/08-runbook.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - `docs/traceability/rtm.md`
  - `docs/state/state.md`

## 5) Instrucciones de implementación (cerradas)

### 5.1 Modelo de datos (mínimo)

Migración nueva (ej. `V7__inventory_core.sql` o convención existente).

`unit`:
- `id uuid pk`
- `code varchar(30) not null` (unique)
- `name varchar(80) not null`
- `is_active boolean not null default true`

`product`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `sku varchar(60) null` (unique por branch si se usa; si no, no forzar)
- `name varchar(160) not null`
- `unit_id uuid not null fk -> unit.id`
- `min_qty numeric(12,3) not null default 0`
- `is_active boolean not null default true`
- `created_at timestamptz not null`

`product_stock`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id` (unique con product_id)
- `product_id uuid not null fk -> product.id`
- `on_hand_qty numeric(12,3) not null default 0`
- `avg_unit_cost numeric(12,4) not null default 0.0000`
- `updated_at timestamptz not null`

`stock_movement`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `product_id uuid not null fk -> product.id`
- `type varchar(20) not null` (`IN`,`OUT`,`ADJUST`,`CONSUME`)
- `qty numeric(12,3) not null` (siempre >0 en registro; el signo se expresa por type)
- `unit_cost numeric(12,4) null` (requerido en IN; snapshot del avg en OUT/CONSUME)
- `total_cost numeric(12,4) not null`
- `reason varchar(255) null` (requerido en ADJUST y overrides)
- `visit_id uuid null fk -> visit.id` (solo CONSUME)
- `created_by uuid not null fk -> app_user.id`
- `created_at timestamptz not null`

`service_bom_item`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `service_id uuid not null fk -> service.id`
- `product_id uuid not null fk -> product.id`
- `qty numeric(12,3) not null` (>0)
- Unique recomendado: (branch_id, service_id, product_id)

### 5.2 Concurrencia y consistencia de stock (obligatorio)

- Para todo movimiento que afecte stock:
  - Cargar `product_stock` con lock (`SELECT ... FOR UPDATE`) para evitar carreras.
  - Recalcular `on_hand_qty` y `avg_unit_cost` determinísticamente.
- Si `on_hand_qty` quedaría negativo:
  - Solo permitido si es override explícito (ver 5.6) y si el permiso existe en `docs/10-permisos.md`.
  - Si no, bloquear.

### 5.3 Costo promedio ponderado (regla exacta)

- Al `IN`:
  - `newQty = oldQty + inQty`
  - `newAvg = (oldQty*oldAvg + inQty*inUnitCost) / newQty` (si newQty>0)
- Al `OUT/CONSUME`:
  - `unitCostSnapshot = oldAvg`
  - `totalCost = qty * unitCostSnapshot`
  - `newQty = oldQty - qty` (no permitir <0 salvo override)
  - `avg_unit_cost` queda igual (si newQty==0, mantener avg o set 0 según convención existente; documentar y mantener consistente)

### 5.4 Endpoints inventario (mínimo)

- `POST /api/v1/products`:
  - valida name, unitId, minQty>=0
- `POST /api/v1/stock/movements`:
  - request: `productId`, `type`, `qty`, `unitCost?`, `reason?`
  - reglas:
    - IN requiere `unitCost`
    - ADJUST requiere `reason` (min 10)
- `POST /api/v1/visits/{visitId}/inventory/consume`:
  - request:
    - `mode`: `BOM_ONLY` | `EXPLICIT_ONLY` | `BOM_PLUS_EXPLICIT`
    - `items[]` opcional para explícitos: `{ productId, qty }`
  - reglas:
    - valida visita del branch y que exista `serviceId` en visita
    - BOM se toma de `service_bom_item` del `serviceId` de la visita
    - registra movimientos `CONSUME` con `visit_id`
    - si falta stock, bloquear salvo que el caller tenga permiso de override (si está definido) y traiga reason

### 5.5 BOM por servicio

- `PUT /api/v1/services/{serviceId}/bom` reemplaza el BOM completo:
  - request: `items[]: { productId, qty }`
  - valida qty>0, productos del mismo branch
- Auditar cambios de BOM solo si ya existe política en auditoría; si no, dejar sin before/after (no inventar), pero sí registrar evento simple si el mecanismo existe.

### 5.6 Integración con Facturación (BRD-REQ-044) — obligatorio

- En endpoints de facturación que agregan/modifican items PRODUCT:
  - Validar stock disponible (`product_stock.on_hand_qty`).
  - Si `qty > on_hand_qty`:
    - Responder con error estándar (409 o 422 según convención existente) indicando “insufficient_stock”.
  - Permitir override **solo** si:
    - existe permiso explícito en `docs/10-permisos.md` para override de stock,
    - request incluye `override=true` + `reason` (min 10),
    - se registra auditoría before/after (BRD-REQ-053).
- Nota: este sprint puede tocar código del módulo facturación para agregar esta validación; eso es parte del alcance.

## 6) Criterios de aceptación (AC)

- [ ] Backend compila y tests pasan: `./mvnw test`.
- [ ] Unidades:
  - [ ] `GET /units` devuelve catálogo (incluye seeds).
- [ ] Productos + stock:
  - [ ] Crear producto y consultar stock inicial 0.
  - [ ] Movimiento IN con unitCost recalcula avg.
  - [ ] Movimiento OUT descuenta stock y registra costo salida.
  - [ ] ADJUST requiere reason (422 si falta) y registra movimiento.
- [ ] Mínimos/alertas:
  - [ ] `GET /stock/low` lista productos con `on_hand_qty <= min_qty`.
- [ ] BOM + consumo:
  - [ ] Definir BOM para un service.
  - [ ] Crear/usar una visita y ejecutar `consume` → crea movimientos CONSUME con `visit_id` y descuenta stock.
- [ ] Bloqueo facturación:
  - [ ] Intentar agregar PRODUCT en factura con qty > stock → falla “insufficient_stock”.
  - [ ] Con override permitido + reason → permite y deja auditoría before/after.
- [ ] Smoke:
  - [ ] `scripts/smoke/spr-b007.ps1` existe y cubre: crear producto+IN → crear BOM → consumo por visita → bloqueo facturación → override con reason.
- [ ] Evidencia docs:
  - [ ] `docs/log/log.md` entrada append-only `SPR-B007`
  - [ ] `docs/status/status.md` fila `SPR-B007` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualizado: BRD-REQ-023,038..044
  - [ ] `docs/state/state.md` actualizado con next sprint recomendado: `SPR-B008`

## 7) Smoke test manual (usuario)

1) Levantar backend:
- `cd backend`
- `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b007.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada de `SPR-B007` (o dejar placeholder explícito).

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
- Si los permisos necesarios para override/ajustes no existen o son ambiguos en `docs/10-permisos.md`:
  - Crear RFC en `docs/rfcs/` proponiendo el permiso faltante (sin implementarlo por defecto).
  - Marcar `SPR-B007` como `BLOCKED`, registrar en LOG y DETENER.
- Si el modelo existente de facturación no permite validar stock en el punto correcto sin romper contratos:
  - RFC + detener (no “parchar” inventando comportamiento).

<!-- EOF -->
