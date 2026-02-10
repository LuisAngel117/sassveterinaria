# 06B - Dominio (modelo de datos) - Parte B

## Estados y transiciones

### appointment.status
| Estado | Puede pasar a | Regla |
|---|---|---|
| SCHEDULED | CHECKED_IN, CANCELLED, NO_SHOW | estado inicial |
| CHECKED_IN | IN_PROGRESS, CANCELLED | check-in separado de atencion |
| IN_PROGRESS | COMPLETED, CANCELLED | solo personal autorizado |
| COMPLETED | CLOSED | requiere nota SOAP cerrada o justificacion |
| CANCELLED | (final) | requiere motivo |
| NO_SHOW | (final) | marcado por recepcion o admin |
| CLOSED | (final) | no editable salvo reapertura con permiso |

### soap_note.status
| Estado | Puede pasar a | Regla |
|---|---|---|
| DRAFT | CLOSED | editable por veterinario/autorizado |
| CLOSED | REOPENED | requiere permiso + reason required |
| REOPENED | CLOSED | se mantiene traza de reapertura |

### invoice.status
| Estado | Puede pasar a | Regla |
|---|---|---|
| DRAFT | ISSUED, VOID | borrador editable |
| ISSUED | VOID, PAID | anulacion requiere motivo |
| PAID | (final) | no editable |
| VOID | (final) | conserva evidencia before/after |

## Auditoria: eventos auditables minimos
- `auth.login_failed`
- `auth.user_locked`
- `appointment.created`
- `appointment.rescheduled`
- `appointment.cancelled`
- `soap.closed`
- `soap.reopened`
- `invoice.issued`
- `invoice.voided`
- `inventory.adjusted`
- `inventory.cost_override`
- `role.changed`

## Seeds demo minimos

### Sucursales
- `branch_centro` - Clinica Centro
- `branch_norte` - Clinica Norte

### Usuarios demo
- `superadmin@demo.local` / `Demo1234!` / rol `SUPERADMIN`
- `admin@demo.local` / `Demo1234!` / rol `ADMIN`
- `recepcion@demo.local` / `Demo1234!` / rol `RECEPCION`
- `vet@demo.local` / `Demo1234!` / rol `VETERINARIO`

Nota: credenciales demo son exclusivas de entorno local.

### Datos de negocio demo
- 5 clientes con 1-2 mascotas cada uno.
- 2 salas por sucursal.
- 10 citas distribuidas en semana actual.
- 4 servicios base con BOM asociado.
- 12 productos con stock inicial por sucursal.
- 3 facturas emitidas y 1 anulada (con motivo).

### Flujo demo esperado (2-3 minutos)
1. Login y seleccion de sucursal.
2. Crear cita sin solape.
3. Realizar check-in y cerrar SOAP.
4. Emitir factura con IVA.
5. Ver reflejo de consumo en inventario.
6. Consultar evento de auditoria asociado.

<!-- EOF -->
