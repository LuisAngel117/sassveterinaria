# SaaSVeterinaria (demo local-first)

Demo vendible en local para portafolio:
- Agenda/turnos + flujo “crear cita → atender → cerrar → facturar”
- Clientes y pacientes/mascotas
- Historia clínica SOAP con plantillas
- Facturación interna + pagos
- Inventario básico con consumo por servicio (BOM)
- Reportes mínimos
- Seguridad (JWT access/refresh + 2FA TOTP para admin/superadmin)
- Auditoría before/after en acciones sensibles

## Fuente de verdad
Toda la especificación y metodología vive en `docs/**`.
La conversación NO es fuente de verdad.

## Preflight (docs)
Ejecutar desde PowerShell:
- pwsh -File scripts/verify/preflight.ps1

## Estado del proyecto
Ver:
- docs/state/state.md
- docs/status/status.md
- docs/log/log.md
