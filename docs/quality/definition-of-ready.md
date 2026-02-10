# Definition of Ready (DoR)

Un sprint NO puede iniciar si falta alguno de estos ítems (=> BLOCKED):

## Checklist DoR (verificable)
- [ ] Sprint existe y está inmutable.
- [ ] Scope cerrado: incluye/excluye claros.
- [ ] Requisitos objetivo declarados (BRD-REQ-###) o N/A justificado.
- [ ] Dependencias identificadas (otro sprint/ADR/RFC).
- [ ] Datos/scoping definidos (branch_id, headers, claims).
- [ ] Permisos definidos para acciones nuevas (docs/10-permisos.md).
- [ ] Errores/validaciones mínimas definidas (Problem Details).
- [ ] Smoke test del sprint definido (comandos exactos o N/A justificado).
- [ ] Runbook impactado identificado (docs/08-runbook.md).
- [ ] Trazabilidad: RTM puede mapear req→sprint (si aplica).

## Excepciones
Solo por RFC:
- Crear `docs/rfcs/RFC-00xx-*.md` explicando por qué se permite iniciar.

<!-- EOF -->
