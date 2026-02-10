# ADR-0004 - Seguridad (AuthN/AuthZ/2FA)

## Contexto
La aplicacion maneja datos clinicos, cobros e inventario; requiere control estricto de identidad y autorizacion.

## Decision
- JWT access token (1h) + refresh token (7d) con rotacion.
- Lockout por 4 intentos fallidos durante 15 minutos.
- 2FA TOTP obligatorio para `ADMIN` y `SUPERADMIN`.
- Autorizacion por permisos de accion, no solo por rol.
- Errores API en formato RFC 7807.

Referencias:
- RFC 7807: https://datatracker.ietf.org/doc/html/rfc7807
- RFC 6238: https://datatracker.ietf.org/doc/html/rfc6238

## Consecuencias
- Mayor seguridad operativa en acciones criticas.
- Mayor complejidad UX en login de roles altos.

## Alternativas descartadas
- Sesiones stateful server-side: validas, pero menos alineadas al contrato API stateless planteado.
- Omitir 2FA en V1: reduce friccion pero aumenta riesgo.

## Fecha
2026-02-10

<!-- EOF -->
