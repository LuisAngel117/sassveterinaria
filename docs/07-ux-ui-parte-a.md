# 07 — UX/UI (A): navegación, roles, pantallas

## 1) Principios UX
- “2–3 minutos demo”: todo debe estar a 2 clics del flujo core.
- Validaciones claras + mensajes humanos en español.
- No bloquear al usuario con pantallas vacías: seeds listos.
- Estados visibles (cita / atención / factura / stock).

## 2) Navegación (mapa de pantallas)

Global:
- Login
- Selección de sucursal
- Dashboard (según rol)

Recepción:
- Agenda (calendario semanal)
- Citas (crear/editar/cancelar)
- Clientes (lista + ficha)
- Mascotas (desde cliente)
- Facturación (facturas + pagos)
- Reportes (solo lectura)

Veterinario:
- Agenda (solo lectura + cola “hoy”)
- Atenciones (crear/editar/cerrar/reabrir con permiso)
- Historia clínica (por mascota)
- Indicaciones (export)

Admin:
- Todo lo anterior
- Catálogos (salas, servicios, unidades)
- Inventario (productos, stock, movimientos)
- Usuarios/roles (si se implementa en v1)
- Auditoría (vista de eventos)
- Configuración IVA (solo SUPERADMIN)

SUPERADMIN:
- Acceso total + configuración sensible (IVA, flags)

## 3) Flujos críticos (mínimo)

1) Crear cita (recepción)
- Seleccionar sala + servicio + cliente + mascota
- Fecha/hora (calendario semanal)
- Validación no-solape (409 con detalle)
- Estado inicial: reservado

2) Check-in y “en atención”
- Botón “Check-in” (marca timestamp)
- Botón “Iniciar atención” → estado “en atención”

3) Registrar atención SOAP (veterinario)
- Plantilla SOAP opcional
- Guardar borrador
- Cerrar atención (bloquea)

4) Facturar (recepción/admin)
- Generar factura desde atención
- Aplicar descuento (si permitido)
- Registrar pagos (mixto/parcial)
- Estado pagado o pendiente

5) Inventario automático
- Al cerrar atención o al facturar (definir en implementación), se descuenta BOM
- Si falta stock: bloqueo con opción override si permiso

<!-- EOF -->
