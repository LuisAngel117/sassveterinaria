# State Snapshot

## Resumen actual (hoy)

- SPR-B001, SPR-B002, SPR-B003, SPR-B004, SPR-B005, SPR-B006, SPR-B007, SPR-B008, SPR-B009, SPR-B010 y SPR-B011 implementados en backend.
- Agenda Core (B002) activo con no-solape/estados/check-in/bloqueos.
- CRM base (B003) activo para clientes y mascotas.
- Servicios (B004) activo:
  - CRUD v1 (`POST/GET/GET{id}/PATCH /api/v1/services`)
  - campos `name`, `durationMinutes`, `priceBase`, `isActive`
  - cambio de `priceBase` exige `reason` y audita before/after
  - permisos `SERVICE_READ/CREATE/UPDATE` aplicados por rol
- Historia clinica (B005) activo:
  - visitas `OPEN/CLOSED` (walk-in o vinculadas a cita)
  - SOAP minimo y plantillas por servicio
  - prescripciones estructuradas por visita
  - adjuntos pdf/jpg/png con limites configurables y almacenamiento local (`STORAGE_DIR`)
  - reapertura con `VISIT_REOPEN` + reason + auditoria before/after
- Facturacion (B006) activo:
  - `tax_config` por branch con IVA configurable (`GET/PUT /config/tax`)
  - factura asociada a visita (`visit_id`) con items y descuentos (item/total)
  - pagos parciales/mixtos y transicion automatica a `PAID`
  - anulacion (`VOID`) con reason + auditoria before/after
  - export factura CSV/PDF y export de indicaciones en PDF
- Inventario (B007) activo:
  - catalogo de unidades y productos (`/api/v1/units`, `/api/v1/products`)
  - stock por sucursal con minimos y alertas (`/api/v1/products/{id}/stock`, `/api/v1/stock/low`)
  - movimientos `IN/OUT/ADJUST/CONSUME` con lock pesimista y costo promedio ponderado
  - BOM por servicio (`GET/PUT /api/v1/services/{id}/bom`) y consumo por visita (`POST /api/v1/visits/{id}/inventory/consume`)
  - validacion de stock en facturacion para `PRODUCT`, con `STOCK_OVERRIDE_INVOICE` + reason + auditoria
- Se creó smoke script `scripts/smoke/spr-b007.ps1`.
- Reportes y dashboard (B008) activo:
  - endpoints branch-scoped de reportes (`/api/v1/reports/appointments|sales|top-services|inventory-consumption|frequent`)
  - export CSV/PDF por reporte reutilizando mecanismo PDF existente (OpenPDF)
  - endpoint `GET /api/v1/dashboard` con KPIs minimos para home
  - validaciones de rango y parametros (`from/to`, `from<=to`, `limit<=100`)
- Se creó smoke script `scripts/smoke/spr-b008.ps1`.
- Auditoria avanzada (B009) activo:
  - API branch-scoped `GET /api/v1/audit/events` con filtros por rango, accion, entidad y actor
  - eventos de auditoria en auth (`AUTH_LOGIN`, `AUTH_REFRESH`, `AUTH_LOGOUT`) y en modulos core
  - eventos sensibles con `reason` (min 10) + before/after (`INVOICE_VOID`, `VISIT_REOPEN`, `STOCK_ADJUST`, `APPT_OVERBOOK`, `CONFIG_TAX_UPDATE`)
  - retencion configurable de auditoria (default 90 dias) con purga programada diaria
  - pruebas `AuditServiceIntegrationTests` para alta de evento, before/after sensible y purga
- Hardening de seguridad (B010) activo:
  - 2FA TOTP para ADMIN/SUPERADMIN con endpoints setup/enable/login challenge (`/auth/login/2fa`)
  - lockout configurable por intentos fallidos (`4/15 -> 15` por default)
  - rate limit in-memory para login/refresh/reportes con 429 + `Retry-After`
  - pruebas `SecurityHardeningIntegrationTests` para 2FA, lockout, rate limit y permisos (403)
  - smoke script `scripts/smoke/spr-b010.ps1`
- Seeds demo + smoke core (B011) activo:
  - `DemoDataSeeder` idempotente ampliado con sala demo, citas demo y visita demo cerrada
  - credenciales demo fijas y branch default para `superadmin/admin/recepcion/veterinario`
  - smoke script `scripts/smoke/spr-b011.ps1` para flujo core E2E (cita -> atencion -> cierre -> factura -> pago)
- Frontend bootstrap creado:
  - `FRONT_DIR` definido en `frontend/` (Next.js + TypeScript + Tailwind + App Router)
  - scripts reales disponibles: `npm run dev`, `npm run build`
  - archivo de entorno ejemplo: `frontend/.env.example`

## Estado de sprints (alto nivel)

- SPR-B001: READY_FOR_VALIDATION.
- SPR-B002: READY_FOR_VALIDATION.
- SPR-B003: READY_FOR_VALIDATION.
- SPR-B004: READY_FOR_VALIDATION.
- SPR-B005: READY_FOR_VALIDATION.
- SPR-B006: READY_FOR_VALIDATION.
- SPR-B007: READY_FOR_VALIDATION.
- SPR-B008: READY_FOR_VALIDATION.
- SPR-B009: READY_FOR_VALIDATION.
- SPR-B010: READY_FOR_VALIDATION.
- SPR-B011: READY_FOR_VALIDATION.
- Proximo sprint recomendado: SPR-F001.

## Riesgos/bloqueos actuales

- Smokes B002/B003/B004/B005/B006/B007 requieren backend corriendo con PostgreSQL local y datos seed demo.
- La validacion funcional final (READY_FOR_VALIDATION -> DONE) depende de ejecucion local del usuario y evidencia en LOG.
- F001/F002 dependen de ejecutar el flujo FRONT sobre el root `frontend/` ya creado y contratos backend reales (OpenAPI/smokes).

<!-- EOF -->
