# STATE — Snapshot de estado (entrada única para nuevas conversaciones)

## Resumen actual
- Proyecto: SaaSVeterinaria (demo local-first/offline-first real).
- Fuente de verdad: docs/**.
- T1 aplicado: gobernanza base + scripts verify/preflight + quality gates + RTM + state.

## Decisiones clave cerradas (resumen)
- Single-tenant v1 + multi-sucursal (branch) con selección en login.
- Scoping: X-Branch-Id obligatorio + branch_id en JWT; deben coincidir.
- Agenda: no-solape por Sala, slot 30m, buffer 10m, vista semanal, check-in separado.
- Historia clínica SOAP con plantillas por servicio, adjuntos PDF/imagen (10MB), cierre y reapertura gobernada por permisos.
- Facturación interna con IVA global configurable (superadmin) y auditoría.
- Inventario por sucursal + BOM por servicio + costo promedio + override con permiso.
- Errores: RFC 7807 Problem Details. (ref: https://datatracker.ietf.org/doc/html/rfc7807)
- 2FA: RFC 6238 TOTP. (ref: https://datatracker.ietf.org/doc/html/rfc6238)

## Estado de ejecución
Ver tabla en: docs/status/status.md

## Próximo paso recomendado
- T2: completar contenido real (brief, BRD con BRD-REQ-###, arquitectura, permisos, runbook, UX/UI).
- Prerrequisito: pasar preflight y registrar output en LOG (T1).

## Riesgos/bloqueos actuales
- Ninguno registrado aún (pendiente validación T1).

<!-- EOF -->
