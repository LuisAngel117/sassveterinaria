# 06 - Dominio Parte A

## Bounded Contexts iniciales
- Clinica: configuracion del tenant y sus politicas.
- Agenda: citas, disponibilidad y reasignaciones.
- Pacientes: fichas clinicas y metadatos basicos.
- Inventario: catalogo y control operativo inicial.

## Reglas base
- Cada recurso pertenece a un tenant.
- Las consultas cruzadas entre tenants estan prohibidas.
- Cambios de estado relevantes deben quedar auditados.

## Eventos de negocio iniciales
- `tenant.created`
- `appointment.created`
- `appointment.rescheduled`
- `patient.registered`

<!-- EOF -->