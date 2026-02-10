# 05 - Seguridad (AuthN/AuthZ/2FA/Auditoria)

## Flujos de auth
1. Usuario envia credenciales (`email` + `password`).
2. Backend valida estado de cuenta y lockout.
3. Si rol es `ADMIN` o `SUPERADMIN`, exige TOTP valido.
4. Backend emite `access_token` (1h) y `refresh_token` (7d) rotatorio.
5. Frontend usa access token y header `X-Branch-Id` para endpoints branch-scoped.

## Politica de password y lockout
- Password minimo recomendado: 12 caracteres.
- Requiere mezcla de mayusculas, minusculas, numero y simbolo.
- Lockout: 4 intentos fallidos -> bloqueo temporal 15 minutos.
- Reset de password queda auditado si lo ejecuta un rol administrativo.

## 2FA TOTP (ADMIN/SUPERADMIN)
Referencia: https://datatracker.ietf.org/doc/html/rfc6238
- Activacion obligatoria para roles de alto privilegio.
- Semilla TOTP unica por usuario.
- Ventana de validacion corta (ej. +/-1 paso) para balance seguridad/usabilidad.

## Roles vs permisos
- Roles: agregadores de permisos.
- Permisos: unidad minima de autorizacion por accion.
- La autorizacion valida: sesion activa + permiso + scoping de branch.
- Matriz oficial en `docs/10-permisos.md`.

## Acciones sensibles (reason required)
1. Reapertura de historia clinica cerrada.
2. Anulacion de factura.
3. Override de costo o precio.
4. Ajuste manual de inventario.
5. Cambio de rol a privilegio elevado.

## Auditoria (before/after)
Cada evento sensible registra como minimo:
- actor_id, branch_id, timestamp,
- entidad y entidad_id,
- accion,
- snapshot before/after,
- reason,
- request_id/correlation_id.

## Rate limit y defensa basica
- `/auth/login`: limite por IP + usuario para frenar fuerza bruta.
- `/auth/refresh`: limite por token/sesion.
- Sanitizacion y validacion estricta de payloads.
- Mensajes de error no revelan detalles sensibles.

## CORS local
- Permitir solo origins locales de desarrollo aprobados.
- Bloquear wildcard en ambientes no dev.

## Error format (Problem Details)
Referencia: https://datatracker.ietf.org/doc/html/rfc7807
Ejemplo:
```json
{
  "type": "https://sassveterinaria.local/errors/branch-scope-mismatch",
  "title": "Branch scope mismatch",
  "status": 403,
  "detail": "X-Branch-Id no coincide con el branch de la sesion.",
  "instance": "/api/v1/appointments"
}
```

<!-- EOF -->
