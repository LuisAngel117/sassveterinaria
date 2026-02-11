# STATUS — Control de tandas y sprints

Estados permitidos:
- NOT_STARTED
- IN_PROGRESS
- READY_FOR_VALIDATION
- DONE
- BLOCKED

Regla:
- DONE/APROBADO solo tras validación local del usuario con evidencia en docs/log/log.md

| Item | Título | Estado | Evidencia (commit) | Notas |
|---|---|---|---|---|
| T1 | Bootstrap gobernanza docs/scripts | READY_FOR_VALIDATION | TBD | Ejecutar scripts/verify/preflight.ps1 y pegar output en LOG |
| T2 | Docs base detallados (brief+brd+arquitectura+ux+runbook+masters) | READY_FOR_VALIDATION | commit-msg: T2: docs base detallados (brief+brd+arquitectura+ux+runbook+masters) | Validado documental |
| SPR-B001 | Walking Skeleton (Auth + Scope + Base API + Smoke mínimo) | READY_FOR_VALIDATION | PENDING_SPR-B001_COMMIT_HASH | Build OK (`./mvnw test`). Smoke script creado; ejecución local pendiente de backend corriendo con Postgres. |
| SPR-B002 | Agenda Core (no-solape por sala + estados + check-in + bloqueos + semana) | READY_FOR_VALIDATION | PENDING_SPR-B002_COMMIT_HASH | Build OK (`./mvnw test`). Smoke script `scripts/smoke/spr-b002.ps1` creado; ejecucion local pendiente con backend+Postgres. |
| SPR-B003 | Clientes y Mascotas (CRUD + búsqueda + invariantes) | READY_FOR_VALIDATION | PENDING_SPR-B003_COMMIT_HASH | Build OK (`./mvnw test`). Smoke script `scripts/smoke/spr-b003.ps1` creado; ejecucion local pendiente con backend+Postgres. |
| SPR-B004 | Servicios (catalogo + duracion + precio base + reason/auditoria) | READY_FOR_VALIDATION | PENDING_SPR-B004_COMMIT_HASH | Build OK (`./mvnw test`). Smoke script `scripts/smoke/spr-b004.ps1` creado; ejecucion local pendiente con backend+Postgres. |
| SPR-B005 | Historia Clinica (atencion + SOAP + plantillas + adjuntos + cierre/reapertura) | READY_FOR_VALIDATION | PENDING_SPR-B005_COMMIT_HASH | Build OK (`./mvnw test`). Smoke script `scripts/smoke/spr-b005.ps1` creado; ejecucion local pendiente con backend+Postgres. |
| SPR-B006 | Facturacion (factura + IVA config + pagos + anulacion + export) | READY_FOR_VALIDATION | PENDING_SPR-B006_COMMIT_HASH | Build OK (`./mvnw test`). Smoke script `scripts/smoke/spr-b006.ps1` creado; ejecucion local pendiente con backend+Postgres. |
| SPR-B007 | Inventario (stock + movimientos + costeo + override + BOM) | READY_FOR_VALIDATION | PENDING_SPR-B007_COMMIT_HASH | Build OK (`./mvnw test`). Smoke script `scripts/smoke/spr-b007.ps1` creado; ejecucion local pendiente con backend+Postgres. |
| SPR-B008 | Reportes (endpoints + export) | READY_FOR_VALIDATION | PENDING_SPR-B008_COMMIT_HASH | Build OK (`./mvnw test`). Smoke script `scripts/smoke/spr-b008.ps1` creado; ejecucion local pendiente con backend+Postgres. |
| SPR-B009 | Auditoria avanzada (before/after + retencion) | READY_FOR_VALIDATION | PENDING_SPR-B009_COMMIT_HASH | Build OK (`./mvnw test`). API `GET /api/v1/audit/events`, retencion configurable (90d default) y pruebas de auditoria/purga implementadas. |
| SPR-B010 | Hardening seguridad (2FA + lockout + rate limit + permisos finos) | READY_FOR_VALIDATION | PENDING_SPR-B010_COMMIT_HASH | Build OK (`./mvnw test`). 2FA TOTP + lockout + rate limit + smoke `scripts/smoke/spr-b010.ps1`. |
| SPR-B011 | Seeds demo + Smoke scripts flujo core | READY_FOR_VALIDATION | PENDING_SPR-B011_COMMIT_HASH | Seed demo idempotente ampliado (room+citas+visit) y smoke `scripts/smoke/spr-b011.ps1` creado. |
| SPR-F001 | Shell + Auth UI + Selector de Sucursal | READY_FOR_VALIDATION | PENDING_SPR-F001_COMMIT_HASH | Front implementado en `frontend/`: rutas `/login`, `/select-branch`, `/`; cliente API unico con auth + `X-Branch-Id`; sesion persistente + logout; build OK (`npm run build`). |
| SPR-F002 | Agenda UI (semana + crear/editar + estados + conflictos + check-in + bloqueos) | READY_FOR_VALIDATION | PENDING_SPR-F002_COMMIT_HASH | Front implementado en `frontend/src/app/agenda/page.tsx`; integra appointments/room-blocks/CRM real; `npm run lint` y `npm run build` OK; `npm run dev` ejecutado con timeout controlado. |

<!-- EOF -->
