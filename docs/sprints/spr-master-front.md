# Plan Maestro - FRONT (spr-master-front)

Regla:
- Este master lista sprints frontend para cerrar BRD-REQ-###.
- Tras aprobacion, cambios solo por RFC/ADR/changelog.
- Front se basa en handoff real: `docs/handoff/handoff-back-to-front.md`.

| Sprint ID | Titulo | Que hace (3-7 bullets) | Que no hace | Dependencias | BRD-REQ-### objetivo |
|---|---|---|---|---|---|
| SPR-F001 | Auth UI y contexto branch | Login; pantalla TOTP; seleccion sucursal; manejo tokens; errores RFC 7807 en UI | No agenda ni SOAP | SPR-B001 | 001,003,005,006,024 |
| SPR-F002 | Agenda semanal UI | Vista semanal; alta/reprogramacion/cancelacion; check-in; feedback de conflictos | No SOAP ni facturacion | SPR-B004, SPR-F001 | 010,011,012,013 |
| SPR-F003 | Clientes y pacientes UI | Listados y formularios; busqueda; validaciones de campos; contexto sucursal | No modulos financieros | SPR-B003, SPR-F001 | 008,009 |
| SPR-F004 | SOAP UI | Editor SOAP; adjuntos; cierre/reapertura con motivo; estados clinicos | No auditoria global | SPR-B005, SPR-F002 | 014,015,016 |
| SPR-F005 | Facturacion e inventario UI | Factura desde cita; anulacion con motivo; vista stock; ajustes con permisos | No reportes avanzados | SPR-B006, SPR-B007, SPR-F002 | 017,018,019,020,021,023 |
| SPR-F006 | Reportes y demo flow | Reportes operativos; recorrido demo 2-3 min; refinamiento UX y mensajes | No online/stage | SPR-B008, SPR-B009 | 026,027 |

<!-- EOF -->
