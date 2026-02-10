# 05 — Seguridad (modelo)

## 1) Flujos de autenticación

- Login:
  - usuario + password
  - si el rol requiere 2FA: challenge TOTP
  - entrega access + refresh
- Refresh:
  - refresh token válido y no revocado
  - rotación: emite refresh nuevo, invalida anterior
- Logout:
  - revoca refresh activo (y opcionalmente familia)

## 2) Política de password (demo, pero seria)

- Longitud mínima: 10
- Debe incluir: mayúscula, minúscula, número y símbolo
- Prohibido: contraseñas comunes (lista básica)
- Hash: bcrypt/argon2 (decisión en implementación; documentar si aplica)

## 3) Bloqueo por intentos fallidos

- Default:
  - 4 intentos fallidos en 15 min → lock de 15 min
- Se audita:
  - intentos fallidos
  - lock/unlock
- Respuesta:
  - 401 para credenciales inválidas
  - 423/429 se evalúa en implementación (si se usa, documentar en OpenAPI). En v1 se prioriza claridad UX.

## 4) Rate limit / defensa básica (v1)

- Objetivo: reducir brute force y abuso sin depender de servicios externos.
- Reglas (defaults; configurables en properties):
  - Login: límite estricto (ej: 10 intentos / 15 min por usuario+IP)
  - Refresh: límite medio
  - Reportes/export: límite más bajo por ser “caro”
- Respuesta estándar:
  - HTTP 429 con Problem Details y (si aplica) `Retry-After`.

## 5) Roles vs permisos

- Roles v1:
  - SUPERADMIN (demo/control total)
  - ADMIN
  - RECEPCION
  - VETERINARIO
- Permisos granulares por acción (ver `docs/10-permisos.md`).

## 6) Acciones sensibles (reason required)

Mínimo:
- anular factura
- cambiar precio (servicio o ítem)
- borrar/anular atención
- editar historia clínica cerrada
- ajustes inventario manuales
- cambiar IVA/configuración fiscal

Regla:
- `reason` obligatorio (string, min 10 chars).
- Auditoría before/after obligatoria.

## 7) CORS (local)

- Permitir origen del frontend local (localhost) hacia backend local.
- En stage/online, se restringe por lista blanca (ver `docs/09-stage-release.md`).

## 8) Auditoría (seguridad y compliance)

- Se audita (mínimo):
  - login/logout/refresh
  - cambios de roles/permisos
  - CRUD citas/atenciones/facturas/pagos/inventario
  - acciones sensibles con before/after
- Retención demo: 90 días.

<!-- EOF -->
