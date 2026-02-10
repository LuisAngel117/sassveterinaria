# RTM - Requirements Traceability Matrix

Regla:
- Todo `BRD-REQ-###` debe mapearse a sprint(s) o quedar bloqueado con RFC.
- Evidencia minima: commit del item que define, implementa o valida.

| Requisito (ID) | Descripcion | Sprint(s) objetivo | Evidencia (commit) | Verificacion | Estado | Notas |
|---|---|---|---|---|---|---|
| BRD-REQ-001 | Login con usuario y password | SPR-B001, SPR-F001 | T2-docs | TBD | DOCUMENTED | Auth base |
| BRD-REQ-002 | Lockout por intentos fallidos | SPR-B001 | T2-docs | TBD | DOCUMENTED | 4 intentos/15m |
| BRD-REQ-003 | 2FA TOTP roles altos | SPR-B001, SPR-F001 | T2-docs | TBD | DOCUMENTED | RFC 6238 |
| BRD-REQ-004 | JWT access/refresh | SPR-B001 | T2-docs | TBD | DOCUMENTED | Rotacion refresh |
| BRD-REQ-005 | Seleccion de sucursal | SPR-B001, SPR-F001 | T2-docs | TBD | DOCUMENTED | Contexto activo |
| BRD-REQ-006 | Scoping header + claim | SPR-B001 | T2-docs | TBD | DOCUMENTED | 400/403 segun caso |
| BRD-REQ-007 | CRUD usuarios y roles | SPR-B002, SPR-F005 | T2-docs | TBD | DOCUMENTED | Admin/Superadmin |
| BRD-REQ-008 | CRUD clientes | SPR-B003, SPR-F003 | T2-docs | TBD | DOCUMENTED | Branch-scoped |
| BRD-REQ-009 | CRUD pacientes | SPR-B003, SPR-F003 | T2-docs | TBD | DOCUMENTED | Ligado a cliente |
| BRD-REQ-010 | Agenda semanal por sala | SPR-B004, SPR-F002 | T2-docs | TBD | DOCUMENTED | Vista semanal |
| BRD-REQ-011 | Slot 30m + buffer 10m | SPR-B004, SPR-F002 | T2-docs | TBD | DOCUMENTED | Configurable |
| BRD-REQ-012 | No-solape por sala | SPR-B004 | T2-docs | TBD | DOCUMENTED | Regla dura |
| BRD-REQ-013 | Check-in separado | SPR-B004, SPR-F002 | T2-docs | TBD | DOCUMENTED | Estado intermedio |
| BRD-REQ-014 | Registro SOAP | SPR-B005, SPR-F004 | T2-docs | TBD | DOCUMENTED | Consulta clinica |
| BRD-REQ-015 | Adjuntos 10MB | SPR-B005, SPR-F004 | T2-docs | TBD | DOCUMENTED | PDF/imagen |
| BRD-REQ-016 | Reabrir SOAP con permiso | SPR-B005, SPR-F004 | T2-docs | TBD | DOCUMENTED | Reason required |
| BRD-REQ-017 | Catalogo servicios/productos | SPR-B006, SPR-F005 | T2-docs | TBD | DOCUMENTED | Precios base |
| BRD-REQ-018 | Factura con IVA global | SPR-B006, SPR-F005 | T2-docs | TBD | DOCUMENTED | IVA configurable |
| BRD-REQ-019 | Anulacion factura auditada | SPR-B006, SPR-F005 | T2-docs | TBD | DOCUMENTED | Reason required |
| BRD-REQ-020 | Inventario por sucursal | SPR-B007, SPR-F005 | T2-docs | TBD | DOCUMENTED | Kardex |
| BRD-REQ-021 | Consumo por BOM | SPR-B007, SPR-F005 | T2-docs | TBD | DOCUMENTED | Al cerrar servicio |
| BRD-REQ-022 | Costo promedio | SPR-B007 | T2-docs | TBD | DOCUMENTED | Recalculo en entradas |
| BRD-REQ-023 | Override costo con permiso | SPR-B007 | T2-docs | TBD | DOCUMENTED | Auditado |
| BRD-REQ-024 | Problem Details RFC 7807 | SPR-B001, SPR-F001 | T2-docs | TBD | DOCUMENTED | Errores consistentes |
| BRD-REQ-025 | Auditoria acciones sensibles | SPR-B008, SPR-F005 | T2-docs | TBD | DOCUMENTED | Before/after |
| BRD-REQ-026 | Seeds demo operables | SPR-B009, SPR-F006 | T2-docs | TBD | DOCUMENTED | Flujo 2-3 min |
| BRD-REQ-027 | Reportes operativos minimos | SPR-B008, SPR-F006 | T2-docs | TBD | DOCUMENTED | Agenda/ventas/stock |

<!-- EOF -->
