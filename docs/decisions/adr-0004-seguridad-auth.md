# ADR-0004 — Seguridad (AuthN/AuthZ/2FA)

## Contexto
- Roles: SUPERADMIN, ADMIN, RECEPCION, VETERINARIO
- Permisos por acción.
- 2FA TOTP para ADMIN/SUPERADMIN.

## Decisión
- JWT access (1h) + refresh (7d) con rotación.
- Lockout por intentos fallidos: 4 intentos → lock temporal (15m v1).
- Errores API: Problem Details (RFC 7807): https://datatracker.ietf.org/doc/html/rfc7807
- 2FA: TOTP (RFC 6238): https://datatracker.ietf.org/doc/html/rfc6238

## Consecuencias
TBD

## Alternativas descartadas
TBD

## Fecha
2026-02-10

<!-- EOF -->
