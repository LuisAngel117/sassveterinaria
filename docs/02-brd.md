# 02 — BRD (Business Requirements) — contrato funcional

## 1) Alcance por módulos (local vs online)

| Módulo | Descripción | Prioridad | Local/Online |
|---|---|---:|---|
| Auth + roles/permisos + 2FA | Login/refresh, permisos por acción, 2FA TOTP para admin/superadmin | Alta | Local |
| Sucursales + scoping | Selección de sucursal y scoping por header+claim | Alta | Local |
| Agenda/turnos | Calendario semanal, estados, no-solape por sala, check-in | Alta | Local |
| CRM | Clientes y pacientes/mascotas | Alta | Local |
| Historia clínica (SOAP) | Atención, plantillas SOAP, cierre/reapertura con auditoría | Alta | Local |
| Servicios + consumos | Catálogo de servicios (duración/precio) + BOM de consumos | Alta | Local |
| Facturación | Factura interna demo, IVA configurable, pagos mixtos/parciales, export | Alta | Local |
| Inventario | Productos, stock por sucursal, movimientos, mínimos/alertas, override | Alta | Local |
| Reportes | 5–8 reportes mínimos + export | Media | Local |
| Recordatorios | Solo “pendiente de enviar” + feature flag | Baja | Online-only (placeholder) |
| SRI e-factura | Placeholder (no implementación) | Baja | Online-only (placeholder) |

## 2) Definición “usable local”
Se considera usable local si:
- Backend y frontend levantan sin internet.
- DB local Postgres está inicializable con migraciones.
- Existen seeds demo para ejecutar el flujo core en 2–3 min.
- Hay smoke scripts/documentación para reproducir.

## 3) Definición “vendible” (demo portafolio)
Se considera vendible demo si:
- UX consistente en español, con manejo claro de errores.
- Flujo core completo (agenda → atención → factura → pago → inventario).
- Seguridad y auditoría demostrables (roles, acciones sensibles, before/after).
- Reportes básicos exportables.

## 4) Requisitos (IDs estables)

> Regla: todos los requisitos tienen ID `BRD-REQ-###` y se trazan en RTM.

### Identidad / scoping
- **BRD-REQ-001**: El sistema soporta múltiples sucursales (branches) con datos separados por sucursal.
- **BRD-REQ-002**: Usuario selecciona sucursal al iniciar sesión; se emite access token con `branch_id` activo (claim).
- **BRD-REQ-003**: API exige `X-Branch-Id` en endpoints scopiados y valida que coincida con claim o privilegio SUPERADMIN.

### Seguridad
- **BRD-REQ-010**: Login con usuario+password y JWT access+refresh.
- **BRD-REQ-011**: Refresh token con rotación; revocación por logout.
- **BRD-REQ-012**: Lockout por intentos fallidos (4 intentos, ventana) + auditoría.
- **BRD-REQ-013**: Política de contraseña con complejidad mínima.
- **BRD-REQ-014**: 2FA TOTP para ADMIN/SUPERADMIN.
- **BRD-REQ-015**: Formato de errores API con Problem Details (RFC 7807: https://www.rfc-editor.org/rfc/rfc7807).

### Auditoría
- **BRD-REQ-020**: Auditoría de acciones clave (crear/editar/cancelar cita; crear/cerrar atención; facturar; pagos; inventario).
- **BRD-REQ-021**: Auditoría before/after para acciones sensibles (anular factura, cambiar precio, reabrir historia cerrada, ajustes manuales inventario).
- **BRD-REQ-022**: Auditoría de login/logout/refresh.
- **BRD-REQ-023**: Retención demo 90 días (configurable a futuro).

### Agenda/turnos
- **BRD-REQ-030**: Gestión de salas (recurso) por sucursal.
- **BRD-REQ-031**: Crear/editar/confirmar/cancelar citas con estados: reservado/confirmado/en atención/cerrado/cancelado.
- **BRD-REQ-032**: Vista calendario semanal por sucursal y sala.
- **BRD-REQ-033**: Regla de no-solape por sala + buffer configurable (10 min).
- **BRD-REQ-034**: Check-in separado de “en atención”.
- **BRD-REQ-035**: Override de no-solape solo con permiso + reason required (para casos excepcionales).

### CRM (clientes/pacientes)
- **BRD-REQ-040**: CRUD de clientes (nombre, cédula/RUC, teléfono, email, dirección, notas, tags).
- **BRD-REQ-041**: CRUD de pacientes/mascotas (especie, raza, sexo, fecha nac/edad, peso, esterilizado, alergias/alertas, antecedentes, código interno).
- **BRD-REQ-042**: 1 mascota → 1 propietario (V1).
- **BRD-REQ-043**: Consentimientos/privacidad (registro de consentimientos con timestamp).

### Historia clínica (SOAP) y atenciones
- **BRD-REQ-050**: Atención puede existir sin cita; si existe cita, puede asociarse.
- **BRD-REQ-051**: SOAP mínimo por atención:
  - S: motivo, anamnesis
  - O: peso, temperatura, hallazgos
  - A: diagnóstico (texto)
  - P: tratamiento/indicaciones, recontrol
- **BRD-REQ-052**: Plantillas SOAP por tipo de servicio.
- **BRD-REQ-053**: Adjuntos (PDF/JPG/PNG) con tamaño máx definido.
- **BRD-REQ-054**: Cierre de atención (bloquea edición).
- **BRD-REQ-055**: Reapertura permitida para VETERINARIO con permiso especial + auditoría + reason required; ADMIN puede ayudar si se bloquea.

### Servicios y prescripciones
- **BRD-REQ-060**: Catálogo de servicios con duración sugerida (30m) y precio base.
- **BRD-REQ-061**: Prescripción/indicaciones estructuradas: dosis, unidad, frecuencia, duración, vía, notas.
- **BRD-REQ-062**: Exportar indicaciones (PDF/HTML).
- **BRD-REQ-063**: Consumo inventario automático por servicio (BOM).

### Facturación
- **BRD-REQ-070**: Factura interna demo (no SRI real).
- **BRD-REQ-071**: IVA aplicable; tasa configurable (default 15%) solo SUPERADMIN, con auditoría before/after. (Circular SRI NAC-DGECCGC25-00000006, 26-dic-2025).
- **BRD-REQ-072**: Descuentos: por ítem y por total.
- **BRD-REQ-073**: Formas de pago mixtas; pagos parciales.
- **BRD-REQ-074**: Estados factura: pendiente/pagado/anulado.
- **BRD-REQ-075**: Anulación requiere reason + auditoría before/after.
- **BRD-REQ-076**: Exportación factura CSV/PDF.

### Inventario
- **BRD-REQ-080**: Productos (medicamento/insumo); servicios viven aparte.
- **BRD-REQ-081**: Unidades (catálogo de unidades).
- **BRD-REQ-082**: Stock por sucursal.
- **BRD-REQ-083**: Movimientos: ingreso/egreso/ajuste/consumo por atención.
- **BRD-REQ-084**: Mínimos y alertas.
- **BRD-REQ-085**: Sin lotes/caducidad (V1).
- **BRD-REQ-086**: Costeo: costo promedio.
- **BRD-REQ-087**: Bloquear facturar si no hay stock, con override por permiso + reason required.

### Reportes
- **BRD-REQ-090**: Reporte citas por período (filtros: sucursal, sala, estado).
- **BRD-REQ-091**: Ventas por período.
- **BRD-REQ-092**: Top servicios.
- **BRD-REQ-093**: Consumo inventario.
- **BRD-REQ-094**: Clientes/pacientes frecuentes.
- **BRD-REQ-095**: Exportación reportes (CSV/PDF).
- **BRD-REQ-096**: Dashboard home por rol.

### Demo / operación
- **BRD-REQ-100**: Seeds demo completos: usuarios/roles, sucursales, salas, servicios, clientes, mascotas, productos/stock.
- **BRD-REQ-101**: Flujo demo “2–3 minutos” documentado en runbook.
- **BRD-REQ-102**: Scripts “verdad” + smoke scripts por flujo crítico.
- **BRD-REQ-103**: Trazabilidad fuerte (RTM actualizado).

### Online-only placeholders
- **BRD-REQ-110**: Recordatorios/confirmaciones: estado “pendiente de enviar”; envío real detrás de feature flag (online).
- **BRD-REQ-111**: SRI e-factura: placeholder de estructura sin implementación.

<!-- EOF -->
