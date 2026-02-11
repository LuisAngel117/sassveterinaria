# RFC-0003 - Bloqueo SPR-F001 por frontend ausente

## Metadata
- RFC ID: RFC-0003
- Titulo: Bloqueo por ausencia de `FRONT_DIR` para `SPR-F001`
- Autor: Codex
- Fecha: 2026-02-10
- Estado: IMPLEMENTED

## Contexto
Se solicito ejecutar `SPR-F001` (frontend: shell + auth UI + selector de sucursal) con reglas de alcance estricto y sin inventar contratos.

## Problema
El repositorio no contiene un frontend ejecutable:
- No existe ningun `package.json` en el arbol del repo.
- No existe carpeta candidata de Next.js para resolver `FRONT_DIR`.

Sin `FRONT_DIR` no se pueden cumplir pasos obligatorios del sprint:
- identificar root frontend,
- implementar rutas `/login`, `/select-branch`, `/`,
- ejecutar comandos verdad `npm run build` y `npm run dev`.

## Objetivos
- Registrar formalmente el bloqueo de `SPR-F001`.
- Evitar implementacion por inferencia o invencion de estructura frontend.

## No objetivos
- Crear un frontend nuevo sin contrato previo.
- Modificar alcance del sprint.

## Opciones evaluadas
1. Crear frontend desde cero por inferencia.
2. Bloquear sprint y requerir alta explicita de frontend en repo.

## Decision propuesta
Adoptar opcion 2: bloquear `SPR-F001` hasta que exista un `FRONT_DIR` real con scripts frontend verificables.

## Impacto
- Arquitectura: sin cambios.
- Seguridad: sin cambios.
- Operacion: `SPR-F001` no ejecutable hasta definir e incorporar frontend.
- Trazabilidad: sprint marcado `BLOCKED` por DoR FAIL.

## Plan de implementacion
1. Incorporar `FRONT_DIR` real (Next.js) en el repo.
2. Actualizar `docs/08-runbook.md` con comandos reales del frontend.
3. Reintentar `SPR-F001` desde pre-check y DoR.

## Rollback
No aplica.

## Archivos a cambiar
- `docs/rfcs/rfc-front-missing.md`
- `docs/status/status.md`
- `docs/log/log.md`
- `docs/changelog.md`

## Trazabilidad
- BRD-REQ afectados: BRD-REQ-001, BRD-REQ-007, BRD-REQ-056 (pendientes por bloqueo frontend).
- Sprint afectado: SPR-F001.
- ADR relacionados: N/A.

## Validacion
- Criterio para desbloquear: existe `FRONT_DIR` con `package.json` y scripts frontend (`dev` y `build`) ejecutables.
- Estado actual:
  - `frontend/package.json` existe.
  - `npm run build` en `frontend/` compila correctamente.

<!-- EOF -->
