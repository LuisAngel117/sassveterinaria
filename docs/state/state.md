# State Snapshot

## Resumen actual (hoy)

- SPR-B001, SPR-B002, SPR-B003, SPR-B004, SPR-B005, SPR-B006 y SPR-B007 implementados en backend.
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

## Estado de sprints (alto nivel)

- SPR-B001: READY_FOR_VALIDATION.
- SPR-B002: READY_FOR_VALIDATION.
- SPR-B003: READY_FOR_VALIDATION.
- SPR-B004: READY_FOR_VALIDATION.
- SPR-B005: READY_FOR_VALIDATION.
- SPR-B006: READY_FOR_VALIDATION.
- SPR-B007: READY_FOR_VALIDATION.
- Proximo sprint recomendado: SPR-B008 (Reportes).

## Riesgos/bloqueos actuales

- Smokes B002/B003/B004/B005/B006/B007 requieren backend corriendo con PostgreSQL local y datos seed demo.
- La validacion funcional final (READY_FOR_VALIDATION -> DONE) depende de ejecucion local del usuario y evidencia en LOG.

<!-- EOF -->
