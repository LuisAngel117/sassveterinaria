# 04 — Convenciones (normas duras)

## 1) Naming y casing
- Rutas y archivos docs: minúsculas, sin espacios.
- Kebab-case para scripts/docs numerados: `00-indice.md`, `verify-docs-eof.ps1`.
- Paquetes Java: `com.sassveterinaria.<modulo>` (ajustar si repo define otro; no inventar sin RFC).

## 2) Idioma
- UI: Español.
- Código (clases/variables): Inglés.
- Comentarios: preferible inglés técnico (opcional).

## 3) Git
- Commits con prefijo:
  - `T#:` para tandas docs
  - `SPR-B###:` / `SPR-F###:` / `SPR-RC###:` para sprints
- No mezclar cambios fuera del scope.

## 4) API
- `/api/v1`
- JSON `camelCase`.
- Fechas: ISO-8601 con offset.

## 5) DB
- Tablas: `snake_case`
- Columnas: `snake_case`
- PK: `id` UUID
- FK: `<entidad>_id`

## 6) Logs
- Backend: logs estructurados mínimos (nivel, traceId, userId, branchId).
- Auditoría: registro formal en tabla.

## 7) Linux strict
- Casing consistente.
- Evitar archivos duplicados por mayúsculas.

## 8) EOF en docs
- Todo `docs/**/*.md` termina EXACTO con `<!-- EOF -->`.
- `scripts/verify/verify-docs-eof.ps1` debe fallar si no cumple.

<!-- EOF -->
