# ADR-0004 — Seguridad: JWT + refresh rotativo + 2FA TOTP

## Contexto
Se requiere seguridad demostrable en demo vendible.

## Decisión
- JWT access (1h) + refresh (7d) con rotación.
- Lockout 4 intentos.
- 2FA TOTP para ADMIN/SUPERADMIN (RFC 6238).

## Consecuencias
- Se requiere almacenamiento de refresh tokens (hash) y revocación.
- Flujo 2FA debe estar en UI para admins.

## Alternativas descartadas
- Session cookies only (menos portable para demo).
- Sin 2FA (menos vendible).

## Fecha
2026-02-10

<!-- EOF -->
