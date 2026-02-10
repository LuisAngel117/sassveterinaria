# 02 - BRD (Business Requirements Document)

Regla:
- Todo requisito funcional se identifica con `BRD-REQ-###`.
- Todo requisito no funcional se identifica con `BRD-NFR-###`.

## Modulos
| Modulo | Objetivo | Requisitos principales |
|---|---|---|
| M1 Identidad y acceso | Proteger acceso y contexto de sucursal | BRD-REQ-001..006 |
| M2 Administracion interna | Gestionar usuarios y permisos | BRD-REQ-007 |
| M3 Clientes y pacientes | Base clinica y administrativa | BRD-REQ-008..009 |
| M4 Agenda y atencion | Operar turnos sin conflicto | BRD-REQ-010..013 |
| M5 Historia SOAP | Registrar acto medico trazable | BRD-REQ-014..016 |
| M6 Facturacion | Cobro interno y control fiscal base | BRD-REQ-017..019 |
| M7 Inventario | Control por sucursal y costo | BRD-REQ-020..023 |
| M8 Calidad operativa | Errores consistentes y auditoria | BRD-REQ-024..025 |
| M9 Demo vendible | Flujo demostrable local | BRD-REQ-026..027 |

## Requisitos funcionales
| ID | Requisito | Prioridad | Criterio de aceptacion resumido |
|---|---|---|---|
| BRD-REQ-001 | Login con usuario y password | Must | Usuario valido obtiene sesion; invalido recibe error estandar |
| BRD-REQ-002 | Lockout por intentos fallidos | Must | 4 intentos fallidos bloquean 15 minutos |
| BRD-REQ-003 | 2FA TOTP para ADMIN y SUPERADMIN | Must | Roles elevados no ingresan sin segundo factor valido |
| BRD-REQ-004 | JWT access/refresh con rotacion | Must | Access 1h y refresh 7d con refresh token rotatorio |
| BRD-REQ-005 | Seleccion de sucursal al inicio | Must | Usuario multi-sucursal elige contexto activo antes de operar |
| BRD-REQ-006 | Scoping por `X-Branch-Id` + claim | Must | Header obligatorio y debe coincidir con claim en JWT |
| BRD-REQ-007 | CRUD de usuarios internos y roles | Must | ADMIN/SUPERADMIN gestionan altas, bajas, bloqueo y roles |
| BRD-REQ-008 | CRUD de clientes | Must | Alta, edicion y consulta por sucursal |
| BRD-REQ-009 | CRUD de pacientes/mascotas | Must | Paciente ligado a cliente, con especie, raza, peso y alertas |
| BRD-REQ-010 | Agenda semanal por sala | Must | Visualizacion semanal con filtros por sala/profesional |
| BRD-REQ-011 | Citas con slot 30m + buffer 10m | Must | El sistema respeta tiempos de agenda configurados |
| BRD-REQ-012 | No-solape por sala | Must | No se pueden guardar citas traslapadas en la misma sala |
| BRD-REQ-013 | Check-in separado de atencion | Should | Cita puede marcarse `CHECKED_IN` antes de consulta |
| BRD-REQ-014 | Registro SOAP por consulta | Must | Cada atencion guarda Subjective/Objective/Assessment/Plan |
| BRD-REQ-015 | Adjuntos PDF/imagen hasta 10MB | Should | Se aceptan adjuntos validos con limite de tamano |
| BRD-REQ-016 | Cerrar/reabrir SOAP con permiso | Must | Reapertura requiere permiso + motivo auditable |
| BRD-REQ-017 | Catalogo de servicios y productos | Must | Servicios/productos activos con precio vigente |
| BRD-REQ-018 | Factura interna con IVA global | Must | Factura calcula impuestos segun configuracion global |
| BRD-REQ-019 | Anulacion de factura con motivo | Must | Anulacion deja reason required y before/after auditado |
| BRD-REQ-020 | Inventario por sucursal | Must | Existencias separadas por branch |
| BRD-REQ-021 | Consumo por BOM de servicio | Must | Al cerrar servicio se descuentan insumos definidos |
| BRD-REQ-022 | Costo promedio de inventario | Should | Entradas recalculan costo promedio por producto/sucursal |
| BRD-REQ-023 | Override de costo con permiso | Must | Solo roles permitidos, con motivo y auditoria |
| BRD-REQ-024 | Errores API en Problem Details | Must | Errores devuelven formato RFC 7807 consistente |
| BRD-REQ-025 | Auditoria de acciones sensibles | Must | Evento con actor, fecha, entidad, before/after y motivo |
| BRD-REQ-026 | Seeds demo operables | Must | Datos demo permiten flujo completo en 2-3 minutos |
| BRD-REQ-027 | Reportes operativos minimos | Should | Agenda diaria, ventas por sucursal y alertas de stock |

## Reglas de negocio criticas
1. Scoping de sucursal es obligatorio y no bypassable.
2. No existe no-solape "best effort"; la regla se valida en app y DB.
3. Reapertura SOAP, anulacion de factura y override de costo siempre exigen motivo.
4. Ajustes de inventario y anulaciones deben quedar auditados con before/after.
5. Todo error funcional de API responde en formato Problem Details.

## Requisitos no funcionales
| ID | Requisito |
|---|---|
| BRD-NFR-001 | Operacion local-first sin dependencia cloud para el core |
| BRD-NFR-002 | Seguridad por roles/permisos + 2FA en roles altos |
| BRD-NFR-003 | Persistencia en PostgreSQL 17 con migraciones versionadas |
| BRD-NFR-004 | Auditoria de acciones sensibles con retencion base de 90 dias |
| BRD-NFR-005 | Tiempos de respuesta locales aptos para operacion diaria |
| BRD-NFR-006 | Manejo de errores uniforme RFC 7807 |
| BRD-NFR-007 | Runbook local reproducible para levantar ambiente |
| BRD-NFR-008 | Trazabilidad documental completa (RTM, status, log, changelog) |

## Definition of usable local
Se considera usable local cuando:
- el sistema inicia en entorno local,
- login y seleccion de sucursal funcionan,
- se puede ejecutar flujo agenda -> SOAP -> factura,
- inventario refleja consumos de servicios,
- auditoria registra acciones sensibles,
- existe runbook y preflight en verde.

## Definition of vendible local
Se considera vendible local cuando:
- hay demo de 2-3 minutos con seeds,
- flujo critico completo sin pasos manuales externos,
- mensajes y UI estan en espanol operativo,
- se puede mostrar evidencia de seguridad, permisos y auditoria,
- estado documental queda en `READY_FOR_VALIDATION`.

<!-- EOF -->
