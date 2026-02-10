# sassveterinaria

Repositorio base para una plataforma SaaS de gestion veterinaria.

## Estado
- Fase actual: T1 (bootstrap de gobernanza docs/scripts).
- Estado de validacion: `READY_FOR_VALIDATION`.

## Estructura inicial
- `docs/`: gobernanza, arquitectura, seguridad, dominio, UX, runbooks y trazabilidad.
- `scripts/verify/`: validadores de integridad documental.

## Comandos de validacion
```powershell
pwsh -File scripts/verify/preflight.ps1
```

## Referencias rapidas
- Indice maestro: `docs/00-indice.md`
- Estado de tareas: `docs/status/status.md`
- Log append-only: `docs/log/log.md`
- Trazabilidad: `docs/traceability/rtm.md`