# 02 — BRD (Business Requirements) — Contrato funcional

## 0) Definiciones (importantes)

### 0.1 “Local-first” / “Offline-first” (definición para este proyecto)

- El sistema debe ser usable y vendible SIN depender de Internet:
  - Backend, DB y Front corren en local (PC del demo) o red local.
  - No hay llamadas obligatorias a servicios externos para el flujo core.
- “Online-only” existe, pero:
  - queda detrás de feature flags
  - su UI/estructura puede estar preparada, pero NO se implementa integración real en v1 local.

### 0.2 Organización

- Multi-tenant: NO.
- Multi-sucursal: SÍ (branch).
- Recursos compartidos entre sucursales: NO (cada sucursal separada).

## 1) Alcance por módulos

| Módulo | Descripción | Prioridad | Local/Online |
|---|---|---:|---|
| Autenticación y seguridad | Login + refresh + roles/permisos + 2FA para ADMIN/SUPERADMIN | P0 | Local |
| Sucursales (scope) | Usuario opera con contexto de sucursal | P0 | Local |
| Agenda/turnos | Calendario + estados + no-solape por sala + check-in | P0 | Local |
| Clientes | Propietarios y su historial/notas | P0 | Local |
| Pacientes | Mascotas con alertas/alergias/antecedentes | P0 | Local |
| Historia clínica (SOAP) | Atenciones con estructura + plantillas + adjuntos | P0 | Local |
| Servicios | Catálogo con duración y precio base | P0 | Local |
| Facturación | Factura interna simple, impuestos, pagos y export | P0 | Local |
| Inventario básico | Productos, stock, movimientos, mínimos, consumo | P0 | Local |
| Auditoría | Quién cambió qué + before/after en sensibles | P0 | Local |
| Reportes | 5–8 reportes con filtros + export | P1 | Local |
| Recordatorios | “pendiente de enviar” en local; envío real online-only | P1 | Online-only (flag) |

## 2) Definition of usable local

El sistema se considera “usable local” si:
- corre en local con Postgres y sin Internet,
- permite al menos:
  - crear cita (con scope sucursal),
  - registrar atención SOAP,
  - cerrar atención,
  - generar factura,
  - registrar pago,
  - descontar inventario (manual o automático según regla),
  - ver auditoría básica,
- y existe seed/demo para probarlo en 2–3 minutos.

## 3) Definition of vendible (demo portafolio)

Además de “usable local”, “vendible demo” requiere:
- UX en español, clara, con validaciones y errores consistentes.
- Roles/Permisos aplicados (no solo “backend permite todo”).
- Evidencia y trazabilidad:
  - DoR/DoD aplicados
  - RTM mapeada (BRD-REQ → sprint → evidencia)
  - Runbook y smoke scripts para flujo core.

## 4) Reglas de negocio críticas (las que rompen sistemas)

- Scope obligatorio por sucursal para endpoints branch-scoped.
- No-solape por SALA (v1): no se crean dos citas que se crucen en la misma sala, salvo “sobre-cupo” con permiso explícito.
- Estados de cita: reservado → confirmado → en_atencion → cerrado; cancelado como terminal; control de check-in separado.
- Historia clínica: puede cerrarse y bloquear edición; reabrir requiere permiso + reason + auditoría before/after.
- Factura: anulación requiere reason + auditoría before/after; estados: pendiente/pagado/anulado.
- Inventario: stock por sucursal; egresos/consumo; override para facturar sin stock requiere permiso y auditoría.

## 5) Requerimientos funcionales (con IDs estables)

> Formato: BRD-REQ-### (NO renumerar luego; cambios solo por RFC)

### Autenticación / Seguridad / Scope

- BRD-REQ-001 (P0): Login con usuario+password → emite access+refresh JWT.
- BRD-REQ-002 (P0): Refresh token con rotación (refresh viejo invalidado).
- BRD-REQ-003 (P0): Logout invalida refresh token activo.
- BRD-REQ-004 (P0): Roles y permisos por acción (granular).
- BRD-REQ-005 (P0): 2FA TOTP para ADMIN y SUPERADMIN (enforcement configurable; default ON en demo).
- BRD-REQ-006 (P0): Bloqueo por intentos fallidos: 4 intentos en ventana 15 min; lock 15 min (valores configurables).
- BRD-REQ-007 (P0): Scope sucursal obligatorio para endpoints branch-scoped vía header `X-Branch-Id` validado contra claims del JWT.
- BRD-REQ-008 (P0): Si falta `X-Branch-Id` en endpoint branch-scoped → 400; si no está permitido → 403; token inválido → 401.
- BRD-REQ-009 (P1): Rate limit/defensa básica: límites distintos para login/refresh/reportes; respuesta estándar 429.

### Agenda/Turnos

- BRD-REQ-010 (P0): CRUD de citas con estados: reservado/confirmado/en_atencion/cerrado/cancelado.
- BRD-REQ-011 (P0): Vista calendario (semana) con filtros por sala y estado.
- BRD-REQ-012 (P0): No-solape por sala (regla dura).
- BRD-REQ-013 (P0): Sobre-cupo permitido solo con permiso; el sistema registra “sobreCupo=true” y deja auditoría.
- BRD-REQ-014 (P0): Check-in separado de “en atención”.
- BRD-REQ-015 (P1): Bloqueos manuales (slot bloqueado) para feriados/ocupación no clínica (simple).

### Clientes / Pacientes

- BRD-REQ-016 (P0): CRUD de clientes (propietarios): nombre, identificación (opcional pero si existe debe validar formato), teléfono, email, dirección, notas.
- BRD-REQ-017 (P0): Búsqueda de clientes por nombre/teléfono/identificación.
- BRD-REQ-018 (P0): CRUD de mascotas: especie, raza, sexo, fecha nac/edad, peso, esterilizado, alergias/alertas, antecedentes.
- BRD-REQ-019 (P0): Código interno de mascota por sucursal (único dentro de sucursal).
- BRD-REQ-020 (P0): Una mascota tiene un solo propietario (v1).

### Servicios / Procedimientos

- BRD-REQ-021 (P0): Catálogo de servicios con: nombre, duraciónMin (default 30), precioBase.
- BRD-REQ-022 (P0): Duración de cita se toma del servicio (v1); override opcional con permiso (si se implementa, queda auditado).
- BRD-REQ-023 (P1): “Receta de consumo” por servicio (BOM) para consumo automático de inventario.

### Historia clínica / Atenciones (SOAP)

- BRD-REQ-024 (P0): Atención puede existir sin cita (walk-in), pero puede vincularse a cita si aplica.
- BRD-REQ-025 (P0): SOAP mínimo por atención:
  - S: motivo_consulta, anamnesis
  - O: peso, temperatura, hallazgos
  - A: diagnostico (texto), severidad (opcional)
  - P: plan_tratamiento, indicaciones, recontrol (fecha opcional)
- BRD-REQ-026 (P0): Plantillas SOAP por tipo de servicio (seleccionable al crear atención).
- BRD-REQ-027 (P0): Adjuntos en atención: imagen/pdf; máximo 10MB por archivo; máximo 5 adjuntos por atención (configurable).
- BRD-REQ-028 (P0): Cerrar atención bloquea edición; reabrir requiere permiso + reason + auditoría before/after.

### Prescripciones / Indicaciones

- BRD-REQ-029 (P0): Prescripción estructurada: medicamento, dosis, unidad, frecuencia, duración, vía, observaciones.
- BRD-REQ-030 (P1): Exportar indicaciones (PDF/HTML) para impresión.

### Facturación

- BRD-REQ-031 (P0): Factura interna simple (no SRI) asociada a una atención.
- BRD-REQ-032 (P0): IVA configurable (default 15%); solo SUPERADMIN puede cambiar; cambio queda auditado.
- BRD-REQ-033 (P0): Descuentos por ítem y/o total (v1: ambos).
- BRD-REQ-034 (P0): Pagos: efectivo/tarjeta/transferencia/mixto; pagos parciales permitidos.
- BRD-REQ-035 (P0): Estados factura: pendiente/pagado/anulado.
- BRD-REQ-036 (P0): Anulación requiere reason + auditoría before/after.
- BRD-REQ-037 (P1): Exportar factura CSV/PDF.

### Inventario

- BRD-REQ-038 (P0): Productos (medicamento/insumo); servicios viven aparte.
- BRD-REQ-039 (P0): Catálogo de unidades (unidad/ml/tabletas/etc).
- BRD-REQ-040 (P0): Stock por sucursal.
- BRD-REQ-041 (P0): Movimientos: ingreso/egreso/ajuste/consumo por atención.
- BRD-REQ-042 (P0): Mínimos y alertas.
- BRD-REQ-043 (P0): Costeo: costo promedio (promedio ponderado) por sucursal.
- BRD-REQ-044 (P0): Bloquear facturar por falta de stock, con override por permiso (auditado).

### Reportes

- BRD-REQ-045 (P1): Reporte “citas por período” con filtros (sucursal, sala, estado).
- BRD-REQ-046 (P1): Reporte “ventas por período”.
- BRD-REQ-047 (P1): Reporte “top servicios”.
- BRD-REQ-048 (P1): Reporte “consumo inventario”.
- BRD-REQ-049 (P1): Reporte “clientes/pacientes frecuentes”.
- BRD-REQ-050 (P1): Exportar reportes CSV/PDF.
- BRD-REQ-051 (P1): Dashboard home por rol.

### Auditoría y trazabilidad

- BRD-REQ-052 (P0): Auditoría obligatoria en: login/logout/refresh, cambios de permisos, citas, atenciones, facturas, pagos, inventario, configuración IVA.
- BRD-REQ-053 (P0): Before/after obligatorio en acciones sensibles (anulación, cambio precio, reabrir historia, ajustes inventario).
- BRD-REQ-054 (P0): Retención demo: 90 días (purgado/archivado simple).

### Seed/Runbook/Smoke

- BRD-REQ-055 (P0): Seed/demo: usuarios/roles, servicios, clientes, mascotas, 1–2 citas y 1 atención ejemplo.
- BRD-REQ-056 (P0): Credenciales demo fijas (para prueba rápida).
- BRD-REQ-057 (P0): Runbook local actualizado + scripts “verdad”.
- BRD-REQ-058 (P0): Smoke script para flujo core “crear cita → atender → cerrar → facturar”.

## 6) No-objetivos v1 (explícitos)

- Integración SRI / e-factura real.
- Cliente auto-reserva real (online-only futuro).
- Multi-tenant real (solo una clínica).
- Lotes y caducidad en inventario.
- Offline PWA sin backend local (se asume backend local disponible).

<!-- EOF -->
