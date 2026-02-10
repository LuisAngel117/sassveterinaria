# STATE - Snapshot de estado (entrada unica)

## Resumen actual
- Proyecto: SaaSVeterinaria.
- Fuente de verdad: `docs/**`.
- T1 aplicado: gobernanza base y scripts de verificacion.
- T2 aplicado: docs base detallados (brief, BRD, arquitectura, dominio, UX, runbook, permisos, masters).

## Decisiones cerradas vigentes
- V1 single-tenant + multi-sucursal (branch).
- `X-Branch-Id` obligatorio y validado contra claim en JWT.
- Agenda: slot 30m, buffer 10m, no-solape por sala.
- SOAP con cierre/reapertura controlada y adjuntos 10MB.
- Facturacion interna con IVA global configurable.
- Inventario por sucursal + BOM por servicio + costo promedio.
- Errores RFC 7807 y 2FA TOTP RFC 6238.

## Estado de ejecucion
Ver `docs/status/status.md`.

## Proximo paso recomendado
- Iniciar SPR-B001 (walking skeleton backend auth + scoping + agenda minima).
- En paralelo, preparar SPR-F001 para login, 2FA y seleccion de sucursal.

## Riesgos y bloqueos
- Sin implementacion de codigo aun (riesgo tecnico esperado en fase documental).
- Pendiente validar decisiones con primer vertical slice funcional.

## Ultima actualizacion
- Fecha: 2026-02-10
- Item: T2
- Estado: READY_FOR_VALIDATION

<!-- EOF -->
