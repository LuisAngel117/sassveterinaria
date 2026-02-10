# 11 - Entrega (checklist vendible local)

## Checklist demo local
- [ ] Login + seleccion de sucursal funcional.
- [ ] Crear cita sin solape en agenda semanal.
- [ ] Registrar y cerrar SOAP.
- [ ] Emitir factura con IVA configurado.
- [ ] Ver impacto en inventario por BOM.
- [ ] Mostrar auditoria de al menos una accion sensible.

## Checklist seguridad
- [ ] Lockout por intentos fallidos validado.
- [ ] 2FA activo para ADMIN/SUPERADMIN.
- [ ] Scoping branch validado con caso positivo y negativo.
- [ ] Errores API en formato Problem Details.

## Checklist RC local
- [ ] `pwsh -File scripts/verify/preflight.ps1` en verde.
- [ ] `docs/log/log.md` actualizado con output/evidencia.
- [ ] `docs/status/status.md` en `READY_FOR_VALIDATION`.
- [ ] `git status --porcelain` limpio.

## Que no incluye (online-only)
- Integraciones SaaS externas.
- Balanceo y despliegue multi-ambiente.
- Gestion enterprise de identidades.

## Criterio de aprobacion
`DONE` solo despues de validacion local del usuario y evidencia documentada.

<!-- EOF -->
