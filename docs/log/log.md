# LOG — Bitácora (append-only)

Formato por entrada:
- Fecha/hora: America/Guayaquil
- Item: T1 / SPR-XXX
- Qué se hizo (bullets)
- Comandos ejecutados (bullets)
- Output (pegar aquí)
- Resultado (READY_FOR_VALIDATION / FAIL)

---

## 2026-02-10T09:00:00-05:00
Item: T1
Qué se hizo:
- Bootstrap de estructura docs/scripts (gobernanza)
- Se creó project-lock + AGENTS + índice
- Se agregaron quality gates (DoR/DoD) + RTM + state snapshot
- Se agregó verificador EOF + preflight

Comandos ejecutados:
- pwsh -File scripts/verify/preflight.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T10:30:00-05:00
Item: T2
Que se hizo:
- Overwrite completo de docs base detallados (brief, BRD, arquitectura, dominio, UX/UI, runbook, permisos, ADR/RFC/sprints).
- Actualizacion controlada de project-lock sin alterar created_at.
- Actualizacion append-only de changelog y status.
- Verificacion final de EOF en docs.

Comandos ejecutados:
- pwsh -File scripts/verify/verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUI

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10 -05:00 (America/Guayaquil)
Tanda: T2
Qué se hizo:
- Se aplicaron docs base detallados (brief, brd con BRD-REQ-###, arquitectura, seguridad, dominio, ux/ui, runbook).
- Se generaron masters BACK/FRONT para aceptación “tal cual”.
Comandos ejecutados:
- scripts/verify/verify-docs-eof.ps1
Output:
- PEGAR OUTPUT AQUÍ
Resultado:
- READY_FOR_VALIDATION
## 2026-02-10T15:50:00-05:00
Item: SPR-B001
Qué se hizo:
- Se creó backend Spring Boot (`backend/`) con Maven wrapper y dependencias del sprint.
- Se implementó auth (`/api/v1/auth/login`, `/refresh`, `/logout`, `/api/v1/me`) con JWT access + refresh rotativo.
- Se implementó scoping `X-Branch-Id` contra claim `branch_id` para endpoints branch-scoped.
- Se implementó caso core mínimo de agenda: `POST/GET /api/v1/appointments`.
- Se agregaron migraciones Flyway y seed idempotente demo.
- Se creó smoke script `scripts/smoke/spr-b001.ps1`.
- Se actualizó runbook, status, RTM y state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- ./mvnw test (en backend)
- pwsh -File scripts/verify/verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION
<!-- EOF -->


