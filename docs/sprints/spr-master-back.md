# Plan Maestro - BACK (spr-master-back)

Regla:
- Este master lista sprints backend para cerrar BRD-REQ-###.
- Tras aprobacion, cambios solo por RFC/ADR/changelog.

| Sprint ID | Titulo | Que hace (3-7 bullets) | Que no hace | Dependencias | BRD-REQ-### objetivo |
|---|---|---|---|---|---|
| SPR-B001 | Auth y branch scoping | Login; lockout; JWT; 2FA TOTP; seleccion de branch; middleware scoping; errores RFC 7807 | No agenda completa ni SOAP | ADR-0003, ADR-0004 | 001,002,003,004,005,006,024 |
| SPR-B002 | Usuarios y permisos | CRUD usuarios; asignacion rol/permisos; bloqueo usuario; auditoria de cambios de rol | No UI admin completa | SPR-B001 | 007 |
| SPR-B003 | Clientes y pacientes | CRUD clientes/pacientes; validaciones de integridad; filtros por branch | No facturacion ni inventario | SPR-B001 | 008,009 |
| SPR-B004 | Agenda core | Agenda semanal; create/reschedule/cancel; no-solape por sala; check-in | No SOAP detallado | SPR-B001, SPR-B003 | 010,011,012,013 |
| SPR-B005 | SOAP y adjuntos | Nota SOAP; cierre/reapertura con permiso; adjuntos hasta 10MB; auditoria sensible | No reporteria avanzada | SPR-B004 | 014,015,016,025 |
| SPR-B006 | Facturacion base | Catalogo servicios/productos; emision factura con IVA; anulacion con motivo | No integraciones fiscales externas | SPR-B004 | 017,018,019 |
| SPR-B007 | Inventario y BOM | Inventario por branch; movimientos; BOM por servicio; costo promedio; override con permiso | No compras avanzadas | SPR-B006 | 020,021,022,023 |
| SPR-B008 | Auditoria y reportes | Consulta auditoria; reportes operativos minimos; filtros por fecha/branch | No BI avanzado | SPR-B005, SPR-B007 | 025,027 |
| SPR-B009 | Seeds y hardening demo | Seeds demo; smoke E2E backend; ajustes de performance y errores | No despliegue online | SPR-B001..SPR-B008 | 026 |

<!-- EOF -->
