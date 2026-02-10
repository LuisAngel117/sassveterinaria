# 07 — UX/UI (B): validaciones, permisos en UI, mensajes

## 1) Tabla pantalla → acciones → permisos → validaciones (mínimo)

| Pantalla | Acción | Permiso | Validación clave |
|---|---|---|---|
| Agenda | Crear cita | AGENDA_CREAR | No-solape + datos obligatorios |
| Agenda | Cancelar cita | AGENDA_CANCELAR | reason opcional (v1) |
| Agenda | Override no-solape | AGENDA_OVERRIDE_NO_SOLAPE | reason requerido |
| Cliente | Crear/editar cliente | CRM_CLIENTE_EDITAR | cédula/RUC formato básico |
| Mascota | Crear/editar mascota | CRM_PACIENTE_EDITAR | peso numérico, código interno único |
| Atención | Crear atención | CLINICA_ATENCION_CREAR | mascota requerida |
| Atención | Cerrar atención | CLINICA_ATENCION_CERRAR | SOAP mínimo completo |
| Atención | Reabrir cerrada | CLINICA_REABRIR_ATENCION | reason requerido + auditoría |
| Factura | Anular | FACTURACION_ANULAR | reason requerido + before/after |
| Inventario | Ajuste manual | INVENTARIO_AJUSTE_MANUAL | reason requerido + before/after |
| Inventario | Override stock negativo | INVENTARIO_OVERRIDE_STOCK_NEGATIVO | reason requerido |

## 2) Permisos en UI (regla)
- Si no tiene permiso:
  - ocultar acciones destructivas
  - deshabilitar botones y mostrar tooltip “Sin permiso”
- Para acciones sensibles:
  - modal obligatorio “Motivo (reason)”
  - mostrar que quedará auditado

## 3) Mensajes de error (Problem Details)
- Mostrar `title` al usuario.
- Si hay `fieldErrors[]`, mapear a inputs.
- En conflictos de agenda (409): mostrar “Conflicto: la sala ya está ocupada en ese horario”.

## 4) i18n
- UI en español (códigos internos pueden ir en inglés, pero etiquetas al usuario en español).

<!-- EOF -->
