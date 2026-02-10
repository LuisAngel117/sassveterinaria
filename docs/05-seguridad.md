# 05 — Seguridad (auth, permisos, 2FA, rate-limit)

## 1) Flujos auth
- Login: usuario+password (+ TOTP si aplica).
- Refresh: rotación de refresh token.
- Logout: revoca refresh token activo.

## 2) Tokens
- Access: 1 hora.
- Refresh: 7 días.
- Rotación: refresh token se invalida y se emite uno nuevo por uso.

## 3) Password policy (mínimo)
- Longitud mínima 10
- Requiere: mayúscula, minúscula, número, símbolo
- Bloqueo por 4 intentos (ventana definida en config)

## 4) 2FA TOTP
- RFC 6238: https://datatracker.ietf.org/doc/html/rfc6238
- Solo ADMIN/SUPERADMIN.
- Enrollment: genera secreto y QR; requiere confirmar 2 códigos para activar.
- Recovery codes: opcional (si se implementa, se audita).

## 5) Roles vs permisos
- Roles v1: SUPERADMIN, ADMIN, RECEPCION, VETERINARIO
- Permisos por acción (ver `docs/10-permisos.md`).

## 6) Acciones sensibles (reason required + auditoría before/after)
Mínimo:
- anular factura
- cambiar precio (servicio o ítem manual)
- borrar/anular atención
- editar historia clínica cerrada / reabrir
- ajustes inventario manuales
- override no-solape
- override stock negativo

## 7) Rate limit / defensa básica
Objetivo: reducir brute force/abuso.
- Rate-limit estricto en:
  - `/auth/login`
  - `/auth/refresh`
  - endpoints 2FA (verify/enable)
- Respuesta estándar cuando excede: **HTTP 429 Too Many Requests** (RFC 6585: https://datatracker.ietf.org/doc/html/rfc6585).
- Registrar evento en auditoría cuando se activa rate-limit.
- Lockout: 4 intentos fallidos → bloqueo temporal + auditoría.

## 8) CORS (local)
- Permitir localhost del frontend hacia backend.
- En stage/online se restringe por dominio.

## 9) Formato de errores API (Problem Details)
- RFC 7807: https://www.rfc-editor.org/rfc/rfc7807
- Para validación: incluir `fieldErrors[]` (campo, mensaje).

## 10) Seguridad de scoping (branch)
- `X-Branch-Id` requerido en endpoints scopiados.
- Validación header+claim (ver `docs/03-arquitectura.md`).

<!-- EOF -->
