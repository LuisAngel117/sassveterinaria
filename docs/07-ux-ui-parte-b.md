# 07B - UX/UI vendible - Parte B

## Flujos criticos end-to-end

### Flujo 1: crear cita
1. Recepcion abre agenda semanal.
2. Selecciona slot disponible (30m) y sala.
3. Selecciona cliente y mascota.
4. Confirma cita y recibe feedback de exito.

### Flujo 2: check-in y atencion
1. Recepcion marca check-in.
2. Veterinario abre consulta.
3. Registra SOAP.
4. Cierra atencion.

### Flujo 3: facturacion
1. Recepcion abre modulo de facturacion desde cita.
2. Agrega servicios/productos.
3. Sistema calcula subtotal + IVA.
4. Emite factura.

### Flujo 4: anulacion sensible
1. Usuario con permiso intenta anular factura o reabrir SOAP.
2. Sistema exige motivo obligatorio.
3. Se registra auditoria before/after.

## Pantalla -> acciones -> permisos -> validaciones
| Pantalla | Accion | Permiso | Validacion clave |
|---|---|---|---|
| Login | Iniciar sesion | AUTH_LOGIN | credenciales validas + lockout |
| 2FA | Confirmar codigo | AUTH_2FA_VERIFY | TOTP valido |
| Seleccion sucursal | Activar contexto | BRANCH_SELECT | sucursal asignada al usuario |
| Agenda | Crear cita | APPOINTMENT_CREATE | no-solape, slot y buffer |
| Agenda | Reprogramar | APPOINTMENT_RESCHEDULE | conflicto de sala |
| Agenda | Cancelar | APPOINTMENT_CANCEL | motivo en cancelaciones |
| SOAP | Guardar nota | SOAP_EDIT | campos SOAP requeridos |
| SOAP | Reabrir nota | SOAP_REOPEN | permiso + reason required |
| Facturacion | Emitir factura | INVOICE_ISSUE | IVA y totales consistentes |
| Facturacion | Anular factura | INVOICE_VOID | permiso + reason required |
| Inventario | Ajustar stock | INVENTORY_ADJUST | cantidad y motivo validos |
| Inventario | Override costo | INVENTORY_COST_OVERRIDE | rol alto + reason required |

## Reglas visuales
- Idioma UI: espanol.
- Estados UI obligatorios: vacio, carga, error, exito.
- Colores de estado consistentes en todos los modulos.
- Confirmaciones criticas con copy explicito de impacto.
- Alertas no bloqueantes para warning, modales para acciones irreversibles.

<!-- EOF -->
