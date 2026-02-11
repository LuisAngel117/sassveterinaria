# RTM — Requirements Traceability Matrix

Regla: todo `BRD-REQ-###` debe mapear a sprint(s) o quedar RFC/BLOCKED.

| BRD-REQ | Descripción | Sprint(s) | Evidencia (commit) | Verificación | Estado | Notas |
|---|---|---|---|---|---|---|
| BRD-REQ-001 | Login access+refresh | SPR-B001, SPR-F001 | PENDING_SPR-B001_COMMIT_HASH; PENDING_SPR-F001_COMMIT_HASH | smoke+manual+frontend build | READY_FOR_VALIDATION | Front integra `/api/v1/auth/login` + `/api/v1/me` con session store local. |
| BRD-REQ-002 | Refresh con rotación | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | tests+smoke | READY_FOR_VALIDATION | Rotación y revocación del refresh previo |
| BRD-REQ-003 | Logout revoca refresh | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | smoke | READY_FOR_VALIDATION | Logout revoca refresh token |
| BRD-REQ-004 | Permisos granulares | SPR-B001, SPR-B010, SPR-F008 | PENDING_SPR-B010_COMMIT_HASH | `./mvnw test` + security tests | READY_FOR_VALIDATION | Endpoints sensibles validados con permisos finos y test 403 en endpoint protegido |
| BRD-REQ-005 | 2FA TOTP admin/superadmin | SPR-B010, SPR-F001 | PENDING_SPR-B010_COMMIT_HASH | `./mvnw test` + security tests | READY_FOR_VALIDATION | Setup/enable/challenge/login-2fa implementado para ADMIN/SUPERADMIN |
| BRD-REQ-006 | Lockout 4 intentos | SPR-B010 | PENDING_SPR-B010_COMMIT_HASH | `./mvnw test` + security tests | READY_FOR_VALIDATION | Lockout configurable: 4 fallos en 15 min -> lock 15 min (default) |
| BRD-REQ-007 | Scope X-Branch-Id validado | SPR-B001, SPR-F001 | PENDING_SPR-B001_COMMIT_HASH; PENDING_SPR-F001_COMMIT_HASH | tests+smoke+frontend manual | READY_FOR_VALIDATION | Front agrega `X-Branch-Id` solo cuando existe branch seleccionado. |
| BRD-REQ-008 | Respuestas 400/403/401 por scope | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | tests | READY_FOR_VALIDATION | Problem Details para casos de scope/auth |
| BRD-REQ-009 | Rate limit básico (429) | SPR-B010 | PENDING_SPR-B010_COMMIT_HASH | `./mvnw test` + security tests | READY_FOR_VALIDATION | Rate limit in-memory para login/refresh/reportes con 429 + Retry-After |
| BRD-REQ-010 | CRUD citas + estados | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH; PENDING_SPR-F002_COMMIT_HASH | `./mvnw test` + smoke + `npm run build` | READY_FOR_VALIDATION | UI agenda integra create/update y transiciones (`confirm/start/close/cancel`) sobre contratos reales. |
| BRD-REQ-011 | Calendario semana + filtros | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH; PENDING_SPR-F002_COMMIT_HASH | manual + smoke + `npm run build` | READY_FOR_VALIDATION | Ruta `/agenda` con semana lunes-domingo y filtros por `roomId`/`status` conectados al backend. |
| BRD-REQ-012 | No-solape por sala | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH; PENDING_SPR-F002_COMMIT_HASH | tests+manual + `npm run build` | READY_FOR_VALIDATION | Front muestra conflicto (`APPT_OVERLAP`) y evita inventar validaciones fuera del contrato backend. |
| BRD-REQ-013 | Sobre-cupo con permiso | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH; PENDING_SPR-F002_COMMIT_HASH | smoke + manual | READY_FOR_VALIDATION | Reintento con `overbookReason` solo cuando existe permiso `APPT_OVERBOOK`, usando modal de motivo. |
| BRD-REQ-014 | Check-in separado | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH; PENDING_SPR-F002_COMMIT_HASH | smoke + manual | READY_FOR_VALIDATION | Boton de check-in separado en UI y llamado a `POST /appointments/{id}/checkin`. |
| BRD-REQ-015 | Bloqueos manuales | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH; PENDING_SPR-F002_COMMIT_HASH | manual + smoke + `npm run build` | READY_FOR_VALIDATION | UI minima para crear/listar bloqueos con contrato real `/api/v1/room-blocks` y permiso `BRANCH_MANAGE`. |
| BRD-REQ-016 | CRUD clientes | SPR-B003, SPR-F003 | PENDING_SPR-B003_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoints create/list/detail/patch para clientes branch-scoped |
| BRD-REQ-017 | Búsqueda clientes | SPR-B003, SPR-F003 | PENDING_SPR-B003_COMMIT_HASH | manual + smoke | READY_FOR_VALIDATION | `GET /clients?q=` por fullName/phone/identification |
| BRD-REQ-018 | CRUD mascotas | SPR-B003, SPR-F003 | PENDING_SPR-B003_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoints create/list/detail/patch para mascotas |
| BRD-REQ-019 | Código interno mascota | SPR-B003 | PENDING_SPR-B003_COMMIT_HASH | tests+manual | READY_FOR_VALIDATION | Unicidad `(branch_id, internal_code)` + conflicto 409 |
| BRD-REQ-020 | 1 mascota = 1 propietario | SPR-B003 | PENDING_SPR-B003_COMMIT_HASH | manual | READY_FOR_VALIDATION | `pet.client_id` obligatorio y sin endpoint de multi-owner |
| BRD-REQ-021 | Catálogo servicios | SPR-B004 | PENDING_SPR-B004_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | CRUD v1 servicios con `name`, `durationMinutes`, `priceBase` y permisos `SERVICE_*` |
| BRD-REQ-022 | Duración por servicio | SPR-B004, SPR-B002 | PENDING_SPR-B004_COMMIT_HASH | tests+manual | READY_FOR_VALIDATION | B002 mantiene calculo `endsAt` desde `service.durationMinutes`; en B004 se consolida catalogo. Override queda pendiente por no existir permiso explicito en matriz |
| BRD-REQ-023 | BOM consumo por servicio | SPR-B007 | PENDING_SPR-B007_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoints BOM `GET/PUT /services/{id}/bom` + consumo por visita |
| BRD-REQ-024 | Atención sin cita | SPR-B005, SPR-F004 | PENDING_SPR-B005_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Walk-in y vínculo opcional a appointment implementados en backend |
| BRD-REQ-025 | SOAP mínimo | SPR-B005, SPR-F004 | PENDING_SPR-B005_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Campos SOAP minimos en visita + PATCH mientras OPEN |
| BRD-REQ-026 | Plantillas SOAP | SPR-B005, SPR-F004 | PENDING_SPR-B005_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | CRUD v1 de templates por servicio (`BRANCH_MANAGE`) |
| BRD-REQ-027 | Adjuntos | SPR-B005, SPR-F004 | PENDING_SPR-B005_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Upload/list/download/delete con limite por archivo y por visita, branch-scoped |
| BRD-REQ-028 | Cerrar/reabrir | SPR-B005, SPR-F004, SPR-F008 | PENDING_SPR-B005_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Cierre bloquea edicion; reapertura requiere `VISIT_REOPEN` + reason + auditoria |
| BRD-REQ-029 | Prescripción estructurada | SPR-B005, SPR-F004 | PENDING_SPR-B005_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Prescripciones estructuradas create/list/patch solo con visita OPEN |
| BRD-REQ-030 | Export indicaciones | SPR-B006, SPR-F005 | PENDING_SPR-B006_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Export PDF desde visita (`/visits/{id}/instructions.pdf`) |
| BRD-REQ-031 | Factura interna | SPR-B006, SPR-F005 | PENDING_SPR-B006_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Factura branch-scoped asociada a `visit_id` |
| BRD-REQ-032 | IVA configurable auditado | SPR-B006, SPR-B009 | PENDING_SPR-B009_COMMIT_HASH | `./mvnw test` + audit tests | READY_FOR_VALIDATION | `CONFIG_TAX_UPDATE` queda como evento sensible con reason + before/after en auditoria avanzada |
| BRD-REQ-033 | Descuentos | SPR-B006, SPR-F005 | PENDING_SPR-B006_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Descuento por item y descuento total con validaciones |
| BRD-REQ-034 | Pagos mixtos/parciales | SPR-B006, SPR-F005 | PENDING_SPR-B006_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Pagos CASH/CARD/TRANSFER; suma de pagos determina estado |
| BRD-REQ-035 | Estados factura | SPR-B006, SPR-F005 | PENDING_SPR-B006_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Estados `PENDING`/`PAID`/`VOID` con reglas de transicion |
| BRD-REQ-036 | Anulación con reason+BA | SPR-B006, SPR-F008 | PENDING_SPR-B006_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | `void` exige reason (>=10) y auditoria before/after |
| BRD-REQ-037 | Export factura | SPR-B006, SPR-F005 | PENDING_SPR-B006_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Export CSV y PDF de factura |
| BRD-REQ-038 | Productos | SPR-B007, SPR-F006 | PENDING_SPR-B007_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Catalogo productos branch-scoped + CRUD basico |
| BRD-REQ-039 | Unidades catálogo | SPR-B007 | PENDING_SPR-B007_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoint `GET /api/v1/units` + seeds base |
| BRD-REQ-040 | Stock por sucursal | SPR-B007 | PENDING_SPR-B007_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | `product_stock` por branch con lock pesimista |
| BRD-REQ-041 | Movimientos | SPR-B007, SPR-F006 | PENDING_SPR-B007_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | IN/OUT/ADJUST/CONSUME con ledger `stock_movement` |
| BRD-REQ-042 | Mínimos/alertas | SPR-B007, SPR-F006 | PENDING_SPR-B007_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | `GET /api/v1/stock/low` (`on_hand_qty <= min_qty`) |
| BRD-REQ-043 | Costeo promedio | SPR-B007 | PENDING_SPR-B007_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Promedio ponderado en IN + snapshot de costo en salidas |
| BRD-REQ-044 | Override sin stock | SPR-B007, SPR-F008 | PENDING_SPR-B007_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Bloqueo `insufficient_stock` + override auditado en facturacion |
| BRD-REQ-045 | Reporte citas | SPR-B008, SPR-F007 | PENDING_SPR-B008_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoints `appointments` + filtros/agrupacion + export CSV/PDF implementados en backend |
| BRD-REQ-046 | Reporte ventas | SPR-B008, SPR-F007 | PENDING_SPR-B008_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoint `sales` + agregados de facturacion/pagos + export CSV/PDF |
| BRD-REQ-047 | Top servicios | SPR-B008, SPR-F007 | PENDING_SPR-B008_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoint `top-services` por `metric=count|revenue` con `limit` validado |
| BRD-REQ-048 | Consumo inventario | SPR-B008, SPR-F007 | PENDING_SPR-B008_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoint `inventory-consumption` sobre movimientos `CONSUME/OUT` con agrupacion |
| BRD-REQ-049 | Frecuentes | SPR-B008, SPR-F007 | PENDING_SPR-B008_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoint `frequent` por `dimension=client|pet` y top por conteo |
| BRD-REQ-050 | Export reportes | SPR-B008, SPR-F007 | PENDING_SPR-B008_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Export CSV/PDF habilitado para reportes de citas/ventas/top-services/consumo/frecuentes |
| BRD-REQ-051 | Dashboard por rol | SPR-B008, SPR-F007 | PENDING_SPR-B008_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | `GET /api/v1/dashboard` branch-scoped con KPIs minimos |
| BRD-REQ-052 | Auditoría obligatoria | SPR-B009 | PENDING_SPR-B009_COMMIT_HASH | `./mvnw test` + audit tests | READY_FOR_VALIDATION | Cobertura en auth + agenda + visitas + facturacion + inventario + config IVA |
| BRD-REQ-053 | Before/after sensibles | SPR-B009 | PENDING_SPR-B009_COMMIT_HASH | `./mvnw test` + audit tests | READY_FOR_VALIDATION | `INVOICE_VOID`, `VISIT_REOPEN`, `STOCK_ADJUST`, `APPT_OVERBOOK`, `CONFIG_TAX_UPDATE` con reason y before/after |
| BRD-REQ-054 | Retención 90 días | SPR-B009 | PENDING_SPR-B009_COMMIT_HASH | `./mvnw test` + audit tests | READY_FOR_VALIDATION | Purga configurable por dias (`app.audit.retention-days`, default 90) con scheduler diario |
| BRD-REQ-055 | Seed demo | SPR-B011, SPR-F010 | PENDING_SPR-B011_COMMIT_HASH | `scripts/smoke/spr-b011.ps1` + revision seed | READY_FOR_VALIDATION | Seed demo idempotente ampliado con room/citas/visita. |
| BRD-REQ-056 | Credenciales demo | SPR-B011, SPR-F001 | PENDING_SPR-B011_COMMIT_HASH; PENDING_SPR-F001_COMMIT_HASH | login manual + smoke + frontend manual | READY_FOR_VALIDATION | Front muestra helper de credenciales demo alineadas a `docs/08-runbook.md`. |
| BRD-REQ-057 | Runbook + scripts verdad | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | manual | READY_FOR_VALIDATION | Runbook actualizado + smoke script |
| BRD-REQ-058 | Smoke flujo core | SPR-B011 | PENDING_SPR-B011_COMMIT_HASH | `scripts/smoke/spr-b011.ps1` | READY_FOR_VALIDATION | Flujo E2E: cita -> visita -> cierre -> factura -> pago. |

<!-- EOF -->

