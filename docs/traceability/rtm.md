# RTM — Requirements Traceability Matrix

Regla: todo `BRD-REQ-###` debe mapear a sprint(s) o quedar RFC/BLOCKED.

| BRD-REQ | Descripción | Sprint(s) | Evidencia (commit) | Verificación | Estado | Notas |
|---|---|---|---|---|---|---|
| BRD-REQ-001 | Login access+refresh | SPR-B001, SPR-F001 | PENDING_SPR-B001_COMMIT_HASH | smoke+manual | READY_FOR_VALIDATION | Implementado en backend |
| BRD-REQ-002 | Refresh con rotación | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | tests+smoke | READY_FOR_VALIDATION | Rotación y revocación del refresh previo |
| BRD-REQ-003 | Logout revoca refresh | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | smoke | READY_FOR_VALIDATION | Logout revoca refresh token |
| BRD-REQ-004 | Permisos granulares | SPR-B001, SPR-B010, SPR-F008 | TBD | tests+manual | PLANNED |  |
| BRD-REQ-005 | 2FA TOTP admin/superadmin | SPR-B010, SPR-F001 | TBD | manual | PLANNED |  |
| BRD-REQ-006 | Lockout 4 intentos | SPR-B010 | TBD | tests+manual | PLANNED |  |
| BRD-REQ-007 | Scope X-Branch-Id validado | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | tests+smoke | READY_FOR_VALIDATION | Filtro branch scope activo |
| BRD-REQ-008 | Respuestas 400/403/401 por scope | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | tests | READY_FOR_VALIDATION | Problem Details para casos de scope/auth |
| BRD-REQ-009 | Rate limit básico (429) | SPR-B010 | TBD | manual | PLANNED |  |
| BRD-REQ-010 | CRUD citas + estados | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Estados y transiciones minimas implementadas en backend |
| BRD-REQ-011 | Calendario semana + filtros | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH | manual + smoke | READY_FOR_VALIDATION | API week list con filtros `roomId` y `status` |
| BRD-REQ-012 | No-solape por sala | SPR-B002 | PENDING_SPR-B002_COMMIT_HASH | tests+manual | READY_FOR_VALIDATION | Regla dura en backend contra citas no canceladas + room blocks |
| BRD-REQ-013 | Sobre-cupo con permiso | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH | smoke | READY_FOR_VALIDATION | Requiere `APPT_OVERBOOK` + `reason` y audita evento sensible |
| BRD-REQ-014 | Check-in separado | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH | smoke | READY_FOR_VALIDATION | `POST /appointments/{id}/checkin` no altera status |
| BRD-REQ-015 | Bloqueos manuales | SPR-B002, SPR-F002 | PENDING_SPR-B002_COMMIT_HASH | manual + smoke | READY_FOR_VALIDATION | CRUD minimo de `room_block` branch-scoped |
| BRD-REQ-016 | CRUD clientes | SPR-B003, SPR-F003 | PENDING_SPR-B003_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoints create/list/detail/patch para clientes branch-scoped |
| BRD-REQ-017 | Búsqueda clientes | SPR-B003, SPR-F003 | PENDING_SPR-B003_COMMIT_HASH | manual + smoke | READY_FOR_VALIDATION | `GET /clients?q=` por fullName/phone/identification |
| BRD-REQ-018 | CRUD mascotas | SPR-B003, SPR-F003 | PENDING_SPR-B003_COMMIT_HASH | `./mvnw test` + smoke | READY_FOR_VALIDATION | Endpoints create/list/detail/patch para mascotas |
| BRD-REQ-019 | Código interno mascota | SPR-B003 | PENDING_SPR-B003_COMMIT_HASH | tests+manual | READY_FOR_VALIDATION | Unicidad `(branch_id, internal_code)` + conflicto 409 |
| BRD-REQ-020 | 1 mascota = 1 propietario | SPR-B003 | PENDING_SPR-B003_COMMIT_HASH | manual | READY_FOR_VALIDATION | `pet.client_id` obligatorio y sin endpoint de multi-owner |
| BRD-REQ-021 | Catálogo servicios | SPR-B004 | TBD | manual | PLANNED |  |
| BRD-REQ-022 | Duración por servicio | SPR-B004, SPR-B002 | PENDING_SPR-B002_COMMIT_HASH | tests+manual | READY_FOR_VALIDATION | `POST /appointments` calcula `endsAt` desde `service.durationMinutes`; override pendiente para B004 |
| BRD-REQ-023 | BOM consumo por servicio | SPR-B007 | TBD | tests+manual | PLANNED |  |
| BRD-REQ-024 | Atención sin cita | SPR-B005, SPR-F004 | TBD | manual | PLANNED |  |
| BRD-REQ-025 | SOAP mínimo | SPR-B005, SPR-F004 | TBD | manual | PLANNED |  |
| BRD-REQ-026 | Plantillas SOAP | SPR-B005, SPR-F004 | TBD | manual | PLANNED |  |
| BRD-REQ-027 | Adjuntos | SPR-B005, SPR-F004 | TBD | manual | PLANNED |  |
| BRD-REQ-028 | Cerrar/reabrir | SPR-B005, SPR-F004, SPR-F008 | TBD | manual | PLANNED |  |
| BRD-REQ-029 | Prescripción estructurada | SPR-B005, SPR-F004 | TBD | manual | PLANNED |  |
| BRD-REQ-030 | Export indicaciones | SPR-B006, SPR-F005 | TBD | manual | PLANNED |  |
| BRD-REQ-031 | Factura interna | SPR-B006, SPR-F005 | TBD | smoke | PLANNED |  |
| BRD-REQ-032 | IVA configurable auditado | SPR-B006, SPR-B009 | TBD | manual | PLANNED |  |
| BRD-REQ-033 | Descuentos | SPR-B006, SPR-F005 | TBD | manual | PLANNED |  |
| BRD-REQ-034 | Pagos mixtos/parciales | SPR-B006, SPR-F005 | TBD | manual | PLANNED |  |
| BRD-REQ-035 | Estados factura | SPR-B006, SPR-F005 | TBD | manual | PLANNED |  |
| BRD-REQ-036 | Anulación con reason+BA | SPR-B006, SPR-F008 | TBD | manual | PLANNED |  |
| BRD-REQ-037 | Export factura | SPR-B006, SPR-F005 | TBD | manual | PLANNED |  |
| BRD-REQ-038 | Productos | SPR-B007, SPR-F006 | TBD | manual | PLANNED |  |
| BRD-REQ-039 | Unidades catálogo | SPR-B007 | TBD | manual | PLANNED |  |
| BRD-REQ-040 | Stock por sucursal | SPR-B007 | TBD | tests | PLANNED |  |
| BRD-REQ-041 | Movimientos | SPR-B007, SPR-F006 | TBD | manual | PLANNED |  |
| BRD-REQ-042 | Mínimos/alertas | SPR-B007, SPR-F006 | TBD | manual | PLANNED |  |
| BRD-REQ-043 | Costeo promedio | SPR-B007 | TBD | tests | PLANNED |  |
| BRD-REQ-044 | Override sin stock | SPR-B007, SPR-F008 | TBD | manual | PLANNED |  |
| BRD-REQ-045 | Reporte citas | SPR-B008, SPR-F007 | TBD | manual | PLANNED |  |
| BRD-REQ-046 | Reporte ventas | SPR-B008, SPR-F007 | TBD | manual | PLANNED |  |
| BRD-REQ-047 | Top servicios | SPR-B008, SPR-F007 | TBD | manual | PLANNED |  |
| BRD-REQ-048 | Consumo inventario | SPR-B008, SPR-F007 | TBD | manual | PLANNED |  |
| BRD-REQ-049 | Frecuentes | SPR-B008, SPR-F007 | TBD | manual | PLANNED |  |
| BRD-REQ-050 | Export reportes | SPR-B008, SPR-F007 | TBD | manual | PLANNED |  |
| BRD-REQ-051 | Dashboard por rol | SPR-F007 | TBD | manual | PLANNED |  |
| BRD-REQ-052 | Auditoría obligatoria | SPR-B009 | TBD | manual | PLANNED |  |
| BRD-REQ-053 | Before/after sensibles | SPR-B009 | TBD | manual | PLANNED |  |
| BRD-REQ-054 | Retención 90 días | SPR-B009 | TBD | manual | PLANNED |  |
| BRD-REQ-055 | Seed demo | SPR-B011, SPR-F010 | TBD | smoke | PLANNED |  |
| BRD-REQ-056 | Credenciales demo | SPR-B011, SPR-F001 | TBD | manual | PLANNED |  |
| BRD-REQ-057 | Runbook + scripts verdad | SPR-B001 | PENDING_SPR-B001_COMMIT_HASH | manual | READY_FOR_VALIDATION | Runbook actualizado + smoke script |
| BRD-REQ-058 | Smoke flujo core | SPR-B011 | TBD | smoke | PLANNED |  |

<!-- EOF -->

