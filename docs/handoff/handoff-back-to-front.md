# Handoff Backend -> Frontend

Regla:
- Este documento solo refleja backend implementado en codigo.
- Si una seccion no tiene implementacion real, se marca `N/A`.

## Estado actual
- Backend funcional aun no implementado.
- Handoff operativo: `N/A` hasta cierre de SPR-B001 en `READY_FOR_VALIDATION`.

## Contrato minimo esperado para habilitar FRONT
- Auth: login + refresh + (si aplica) verificacion TOTP.
- Contexto: endpoint para sucursales permitidas y seleccion de branch activa.
- Agenda: endpoints create/list/reschedule/cancel con scoping.
- Error model: RFC 7807 uniforme.

## Endpoints implementados (reales)
- N/A

## Errores estandar implementados
- N/A

## Reglas de negocio implementadas
- N/A

## Seeds y credenciales reales
- N/A

## Variables de entorno reales
- N/A

## Smoke scripts disponibles
- N/A

## Criterio para actualizar este handoff
- Existe backend ejecutable.
- Endpoints probados por smoke.
- Evidencia en log y status.

<!-- EOF -->
