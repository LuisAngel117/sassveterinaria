# ADR-0003 - Tenancy/Scoping (single-tenant + multi-branch)

## Contexto
V1 opera en un solo tenant, pero con multiples sucursales que requieren aislamiento operativo para evitar mezcla de datos.

## Decision
- Incluir `branch_id` en JWT.
- Exigir header `X-Branch-Id` en endpoints branch-scoped.
- Validar coincidencia estricta `header == claim`.
- Rechazar request con:
  - 400 si falta header en endpoint branch-scoped,
  - 403 si header y claim no coinciden.

Referencia:
- https://cheatsheetseries.owasp.org/cheatsheets/Multi_Tenant_Security_Cheat_Sheet.html

## Consecuencias
- Reduce riesgo de fuga cruzada entre sucursales.
- Aumenta complejidad de middleware y pruebas negativas.

## Alternativas descartadas
- Scoping solo por path sin claim: insuficiente contra manipulos de contexto.
- Scoping solo por claim sin header explicito: menos trazable para auditoria de request.

## Fecha
2026-02-10

<!-- EOF -->
