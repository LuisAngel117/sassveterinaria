# ADR-0003 — Tenancy/Scoping (single-tenant + multi-branch)

## Contexto
- V1 es single-tenant.
- Existe multi-sucursal (branch) con aislamiento de datos.

## Decisión
- `branch_id` va en JWT (fuente de verdad).
- Requests branch-scoped exigen header `X-Branch-Id`.
- Regla: header y claim deben coincidir; si no → 403.
- Si falta header en endpoint branch-scoped → 400.

Referencia de buenas prácticas multi-tenant/aislamiento:
- https://cheatsheetseries.owasp.org/cheatsheets/Multi_Tenant_Security_Cheat_Sheet.html

## Consecuencias
TBD

## Alternativas descartadas
TBD

## Fecha
2026-02-10

<!-- EOF -->
