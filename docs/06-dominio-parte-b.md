# 06 - Dominio Parte B

## Entidades principales
- Tenant
- User
- Role
- Appointment
- Patient
- Service
- Product

## Value Objects sugeridos
- TenantId
- UserId
- AppointmentId
- Money
- TimeRange

## Invariantes
- Un usuario solo opera dentro de su tenant activo.
- Una cita no puede quedar sin paciente asociado.
- Un movimiento de inventario requiere actor y motivo.

## Casos de uso semilla
- Alta de tenant.
- Alta de usuario interno.
- Registro y consulta de cita.

<!-- EOF -->