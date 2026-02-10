# 06 — Dominio (B): CRM, clínica, facturación, inventario, auditoría

## 1) CRM

### Cliente
Campos obligatorios:
- nombre
- cédula/RUC (sí)
- teléfono
- email (opcional pero recomendado)
- dirección (opcional)
- notas
- tags (lista simple)

### Paciente/Mascota
Campos obligatorios:
- código_interno
- nombre
- especie
- raza
- sexo
- fecha_nacimiento (o edad aproximada)
- peso (kg)
- esterilizado (bool)
- alergias/alertas (texto)
- antecedentes (texto)

Regla: 1 mascota → 1 propietario (V1).

### Consentimientos/privacidad
- Consentimiento tratamiento (sí/no) + timestamp
- Consentimiento comunicaciones (sí/no) + timestamp
- Nota legal: demo; no sustituye asesoría legal.

## 2) Clínica (atenciones SOAP)

### Atención (Encounter)
- Puede existir sin cita (BRD-REQ-050).
- Si existe cita asociada: referencia `appointment_id`.

SOAP mínimo:
- S: motivo, anamnesis
- O: peso, temperatura, hallazgos
- A: diagnóstico (texto)
- P: tratamiento/indicaciones, recontrol

### Plantillas SOAP
- Por tipo de servicio (`service_id`).
- Se pueden clonar/editar (admin).

### Adjuntos
- Tipos: PDF, JPG, PNG
- Tamaño máx: 10 MB por archivo
- Almacenamiento local: directorio configurado (ruta en env); en DB se guarda metadata.

### Cierre/Reapertura
- Cerrar bloquea edición.
- Reapertura:
  - Veterinario con permiso `CLINICA_REABRIR_ATENCION` + reason required
  - Auditar before/after.

## 3) Facturación

### Factura (demo)
- Asociada a atención (ideal), o manual.
- Items:
  - servicios (desde atención)
  - ítems manuales (si se permite, controlado)
- IVA:
  - tasa configurable (default 15%)
  - cambio solo SUPERADMIN, con auditoría before/after
- Estados: pendiente / pagado / anulado

Pagos:
- múltiples pagos por factura (parcial)
- método: efectivo / tarjeta / transferencia
- mixto permitido

Anulación:
- reason required + auditoría before/after.

Export:
- CSV/PDF.

## 4) Inventario

Productos:
- medicamento / insumo
- unidad base (catálogo unidades)

Stock por sucursal:
- tabla de stock por producto + branch.

Movimientos:
- ingreso
- egreso
- ajuste manual (sensible)
- consumo por atención (desde BOM)

Costeo:
- costo promedio.

Stock negativo:
- por defecto bloquea facturar/consumir si no hay stock
- override:
  - permiso `INVENTARIO_OVERRIDE_STOCK_NEGATIVO` + reason required + auditoría.

Mínimos:
- stock_minimo por producto/branch
- alertas en dashboard.

## 5) Auditoría

Tabla AuditEvent (mínimo):
- id (UUID)
- actor_user_id
- branch_id
- action (string)
- entity_type
- entity_id
- reason (nullable)
- before_json (nullable)
- after_json (nullable)
- ip (nullable)
- user_agent (nullable)
- created_at (timestamptz)

Retención demo:
- 90 días (futuro: configurable).

<!-- EOF -->
