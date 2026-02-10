# 09 - Stage/Release (futuro online)

## Que cambia al ir a online
- Separacion de ambientes (dev, stage, prod).
- Gestion de secretos y rotacion fuera de codigo.
- Endurecimiento de CORS, TLS y politicas de red.
- Monitoreo y alertas centralizadas.

## Feature flags (online-only)
- Integraciones externas (mensajeria, pagos, laboratorios).
- Sincronizacion remota.
- Analitica extendida.

## Checklist stage
1. Migraciones aplicadas sin drift.
2. Variables de entorno validadas.
3. Smoke test completo en stage.
4. Auditoria y permisos validados con casos negativos.
5. Plan de rollback documentado.

## Evidencia minima de release
- Commit/tag de release.
- Output de smoke y pruebas clave.
- Actualizacion de `status/log/changelog/rtm`.

## Criterio de bloqueo
Si falla seguridad, scoping o auditoria, se bloquea release hasta resolver.

<!-- EOF -->
