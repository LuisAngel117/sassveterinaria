# ADR-0003 — Scoping por sucursal (branch)

## Contexto
Sistema es single-tenant (una clínica) pero multi-sucursal.

## Decisión
- Branch seleccionado al login.
- Access token incluye claim `branch_id`.
- Requests a endpoints scopiados requieren `X-Branch-Id`.
- Backend valida header+claim.
- Errores:
  - falta header: 400
  - mismatch o sin autorización: 403
  - sin token: 401

## Consecuencias
- Menos riesgo de spoofing por header.
- Front debe siempre enviar header en requests scopiados.

## Alternativas descartadas
- Solo header (spoofable).
- Solo claim (difícil debug y multi-branch operations).

## Fecha
2026-02-10

<!-- EOF -->
