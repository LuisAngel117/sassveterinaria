# RFC-0002 - Falta de definición de SPR-B002

## Metadata
- RFC ID: RFC-0002
- Titulo: Bloqueo por ausencia de `docs/sprints/spr-b002.md`
- Autor: Codex
- Fecha: 2026-02-10
- Estado: DRAFT

## Contexto
Se solicitó ejecutar el sprint `SPR-B002`.

## Problema
El archivo requerido `docs/sprints/spr-b002.md` no existe. Sin especificación del sprint no se puede ejecutar alcance, AC, incluye/excluye ni smoke truth sin inventar contenido.

## Objetivos
- Registrar el bloqueo formal.
- Evitar implementación sin contrato de sprint.

## No objetivos
- Definir o inventar el contenido de `SPR-B002`.
- Implementar cambios de backend/frontend sin sprint documentado.

## Opciones evaluadas
1. Implementar por inferencia desde `spr-master-back.md`.
2. Detener ejecución y solicitar creación del sprint detallado.

## Decision propuesta
Adoptar opción 2: detener ejecución y requerir creación previa de `docs/sprints/spr-b002.md`.

## Impacto
- Arquitectura: sin cambios.
- Seguridad: sin cambios.
- Operacion: ejecución bloqueada hasta definir sprint.
- UX/UI: sin cambios.
- Riesgos: retraso del plan backend hasta emitir sprint.

## Plan de implementacion
1. Crear RFC de bloqueo.
2. Notificar estado bloqueado en reporte.
3. Reintentar ejecución cuando exista `docs/sprints/spr-b002.md`.

## Rollback
No aplica.

## Archivos a cambiar
- docs/rfcs/rfc-0002-spr-b002-missing.md

## Trazabilidad
- BRD-REQ afectados: N/A
- ADR relacionados: N/A
- Sprint(s) afectados: SPR-B002

## Validacion
- Criterio para desbloquear: existe `docs/sprints/spr-b002.md` con alcance y AC verificables.

<!-- EOF -->
