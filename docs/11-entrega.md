# 11 — Entrega (checklist vendible local)

## 1) Checklist demo local (mínimo)
- [ ] Backend levanta local
- [ ] Frontend levanta local
- [ ] DB inicializa con migraciones
- [ ] Seeds demo cargan sin errores
- [ ] Flujo core completo:
  - [ ] crear cita
  - [ ] check-in / en atención
  - [ ] atención SOAP + cierre
  - [ ] facturar + pago
  - [ ] inventario refleja consumo
- [ ] Reportes básicos muestran datos seed
- [ ] Export CSV/PDF funciona (mínimo uno)

## 2) Checklist seguridad
- [ ] Login + refresh funcionan
- [ ] Roles/permisos bloquean acciones
- [ ] Acciones sensibles piden reason
- [ ] Auditoría registra before/after
- [ ] Lockout activo (4 intentos)

## 3) Checklist RC
- [ ] scripts/verify/preflight.ps1 pasa
- [ ] scripts smoke definidos y ejecutables
- [ ] LOG/STATUS actualizados con evidencia
- [ ] RTM actualizado (req→sprint→evidencia)
- [ ] state snapshot actualizado

## 4) Qué NO incluye (V1)
- Envío real de recordatorios (online-only)
- SRI e-factura real (online-only)

<!-- EOF -->
