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

<!-- EOF -->
