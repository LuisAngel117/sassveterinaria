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

| T2 | Docs base detallados (brief+brd+arquitectura+ux+runbook+masters) | READY_FOR_VALIDATION | commit-msg: T2: docs base detallados (brief+brd+arquitectura+ux+runbook+masters) | Validar verify-docs-eof y registrar output en LOG |

<!-- EOF -->

