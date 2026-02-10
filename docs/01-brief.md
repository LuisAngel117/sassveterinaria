# 01 — Brief (contexto de negocio)

## Visión (1 párrafo)
SaaSVeterinaria es una **demo local-first** para gestión de una clínica veterinaria: agenda/turnos, CRM (clientes y pacientes), historia clínica (SOAP), facturación e inventario básico, con reportes mínimos y seguridad/auditoría. Debe verse y sentirse **vendible** (UX en español) y ser demostrable en **2–3 minutos** con datos seed.

## Público objetivo
- Clínicas veterinarias pequeñas/medianas con 1+ sucursales.
- Roles típicos: recepción, veterinarios, administración.
- Necesidad: orden operativo, trazabilidad clínica y control básico de cobros/inventario.

## Alcance V1 vendible (local)
Sí. V1 debe cubrir el flujo core end-to-end:
**crear cita → check-in/en atención → registrar atención SOAP → cerrar → facturar → cobrar (parcial/mixto) → reflejar consumo inventario**.

## No-objetivos (V1)
- Facturación electrónica SRI real (solo placeholder online-only).
- Envío real de recordatorios (solo “pendiente de enviar” + feature flag).
- Multi-tenant (múltiples empresas/clínicas): fuera (solo 1 clínica), pero **sí multi-sucursal**.
- Lotes/caducidad avanzada de inventario.
- Módulos complejos de contabilidad.

## Principios
- **Local-first / offline-first**: todo lo posible funciona sin Internet (DB local, sin dependencias externas para el core).
- **Trazabilidad fuerte**: BRD-REQ-### → sprint → evidencia (commit/log).
- **Seguridad por defecto**: auth robusta, permisos por acción, auditoría before/after en sensibles.
- **Anti “piezas sueltas”**: sprints entregan incrementos integrados; DoR/DoD bloquean.

## Glosario
- **Sucursal (Branch)**: ubicación física; particiona datos (stock, agenda, reportes).
- **Sala**: recurso físico para atención; no-solape por sala.
- **Cita (Appointment)**: bloque de agenda con estado (reservado/confirmado/en atención/cerrado/cancelado).
- **Atención (Encounter)**: registro clínico; puede existir sin cita.
- **SOAP**: Subjective / Objective / Assessment / Plan.
- **Factura**: comprobante interno demo con items, IVA, pagos y estado.
- **Movimiento de inventario**: ingreso/egreso/ajuste/consumo por atención.
- **Acción sensible**: requiere “reason required” + auditoría before/after.

<!-- EOF -->
