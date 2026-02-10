# 08 - Runbook local (operacion)

## Prerrequisitos
- Git instalado.
- PowerShell 7 (`pwsh`).
- Java 21 (para backend cuando exista).
- Node.js LTS (para frontend cuando exista).
- PostgreSQL 17 local.

## Validacion inicial
```powershell
git status --porcelain
pwsh -File scripts/verify/preflight.ps1
```

## DB local (Postgres 17)
1. Crear base local (nombre sugerido: `sassveterinaria`).
2. Configurar usuario local con permisos de lectura/escritura.
3. Ejecutar migraciones Flyway cuando el modulo backend este disponible.

## Levantar backend (cuando exista carpeta backend)
```powershell
cd backend
./mvnw spring-boot:run
```
Si `backend/` no existe aun, registrar `N/A` en log del item activo.

## Levantar frontend (cuando exista carpeta frontend)
```powershell
cd frontend
npm install
npm run dev
```
Si `frontend/` no existe aun, registrar `N/A` en log del item activo.

## Smoke local minimo
1. Login valido.
2. Seleccion de sucursal.
3. Alta de cita sin solape.
4. Registro SOAP.
5. Emision de factura.
6. Revision de auditoria y stock.

## Troubleshooting
- Error EOF docs: ejecutar `pwsh -File scripts/verify/verify-docs-eof.ps1`.
- Preflight en rojo: revisar faltantes en salida y corregir antes de continuar.
- Branch mismatch: validar `X-Branch-Id` y claim `branch_id`.
- Error 2FA: sincronizar hora local y regenerar TOTP.

## Scripts verdad
- `scripts/verify/preflight.ps1`
- `scripts/verify/verify-docs-eof.ps1`

## Regla de cierre
No mover a `READY_FOR_VALIDATION` sin evidencia de comandos ejecutados en `docs/log/log.md`.

<!-- EOF -->
