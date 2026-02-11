# 08 — Runbook (operación local)

## 1) Prerrequisitos
- Java 21
- Maven Wrapper (`backend/mvnw`)
- PostgreSQL 17 local
- PowerShell 7 (`pwsh`)

## 2) Base de datos local
Crear base de datos local:
- Nombre sugerido: `sassveterinaria`

Credenciales por defecto en `application.properties`:
- DB_URL=`jdbc:postgresql://localhost:5432/sassveterinaria`
- DB_USER=`postgres`
- DB_PASSWORD=`postgres`

## 3) Variables de entorno backend (SPR-B001)
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `APP_JWT_SECRET` (minimo 32 bytes)
- `APP_JWT_ACCESS_SECONDS` (default `3600`)
- `APP_JWT_REFRESH_SECONDS` (default `604800`)
- `STORAGE_DIR` (default `storage`, usado por adjuntos de visitas)
- `APP_VISIT_ATTACHMENTS_MAX_SIZE_BYTES` (default `10485760` = 10MB)
- `APP_VISIT_ATTACHMENTS_MAX_PER_VISIT` (default `5`)
- `APP_AUDIT_RETENTION_DAYS` (default `90`)
- `APP_AUDIT_PURGE_CRON` (default `0 30 3 * * *`)
- `APP_SECURITY_2FA_ENFORCEMENT_ENABLED` (default `true`)
- `APP_SECURITY_2FA_ALLOW_LOGIN_WITHOUT_ENROLLMENT` (default `true`)
- `APP_SECURITY_2FA_CHALLENGE_SECONDS` (default `300`)
- `APP_SECURITY_2FA_ISSUER` (default `SaaSVeterinaria`)
- `APP_SECURITY_LOCKOUT_MAX_ATTEMPTS` (default `4`)
- `APP_SECURITY_LOCKOUT_WINDOW_MINUTES` (default `15`)
- `APP_SECURITY_LOCKOUT_DURATION_MINUTES` (default `15`)
- `APP_SECURITY_RATE_LOGIN_LIMIT` (default `10`)
- `APP_SECURITY_RATE_LOGIN_WINDOW_SECONDS` (default `900`)
- `APP_SECURITY_RATE_REFRESH_LIMIT` (default `30`)
- `APP_SECURITY_RATE_REFRESH_WINDOW_SECONDS` (default `900`)
- `APP_SECURITY_RATE_REPORT_LIMIT` (default `20`)
- `APP_SECURITY_RATE_REPORT_WINDOW_SECONDS` (default `300`)

## 4) Migraciones
- Flyway corre al iniciar backend.
- Migraciones activas:
  - `backend/src/main/resources/db/migration/V1__init.sql`
  - `backend/src/main/resources/db/migration/V2__agenda_core.sql`
  - `backend/src/main/resources/db/migration/V3__crm_clients_pets.sql`
  - `backend/src/main/resources/db/migration/V4__services_catalog.sql`
  - `backend/src/main/resources/db/migration/V5__clinical_visits.sql`
  - `backend/src/main/resources/db/migration/V6__billing_invoices.sql`
  - `backend/src/main/resources/db/migration/V7__inventory_core.sql`
  - `backend/src/main/resources/db/migration/V8__audit_advanced.sql`
  - `backend/src/main/resources/db/migration/V9__security_hardening.sql`

## 5) Levantar backend (SPR-B001)
```powershell
cd backend
./mvnw test
./mvnw spring-boot:run
```

Healthcheck:
```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## 6) Smoke script SPR-B001
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b001.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. `POST /api/v1/auth/login` (usuario `recepcion`)
3. `POST /api/v1/appointments`
4. `GET /api/v1/appointments`

## 7) Smoke script SPR-B002
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b002.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. `POST /api/v1/auth/login` (usuario `admin`)
3. `POST /api/v1/rooms`
4. `GET /api/v1/services`
5. `POST /api/v1/appointments` (creacion base)
6. `POST /api/v1/appointments` (solape sin sobre-cupo -> 422)
7. `POST /api/v1/appointments` (sobre-cupo con `overbookReason`)
8. `POST /api/v1/appointments/{id}/checkin`
9. `GET /api/v1/appointments?from=...&to=...&roomId=...`

## 8) Usuarios demo seed
- `superadmin` / `SuperAdmin123!`
- `admin` / `Admin123!`
- `recepcion` / `Recepcion123!`
- `veterinario` / `Veterinario123!`

Datos CRM demo (si DB vacía):
- 1 cliente demo en sucursal `CENTRO`
- 1 mascota demo asociada (codigo interno unico por sucursal)

Datos servicios demo:
- Consulta general (30 min, $20.00)
- Vacunacion (20 min, $15.00)
- Control post-operatorio (30 min, $18.00)

Branch demo:
- `CENTRO` / `Sucursal Centro`

## 9) Smoke script SPR-B003
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b003.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. `POST /api/v1/auth/login` (usuario `recepcion`)
3. `POST /api/v1/clients`
4. `GET /api/v1/clients?q=...` (busqueda por nombre/telefono)
5. `POST /api/v1/clients/{clientId}/pets`
6. `POST /api/v1/clients/{clientId}/pets` (duplicado internalCode -> 409)
7. `GET /api/v1/clients/{clientId}/pets`
8. `GET/PATCH /api/v1/pets/{id}`

## 10) Troubleshooting
- Si falla conexión DB: revisar `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Si falla JWT: revisar `APP_JWT_SECRET` (min 32 bytes).
- Si falla scope: verificar header `X-Branch-Id` y claim `branch_id` del token.

## 11) Smoke script SPR-B004
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b004.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. `POST /api/v1/auth/login` (admin)
3. `POST /api/v1/services` (crear servicio)
4. `POST /api/v1/auth/login` (recepcion)
5. `GET /api/v1/services` (read permitido)
6. `POST /api/v1/services` con recepcion (403 esperado)
7. `PATCH /api/v1/services/{id}` actualizando `durationMinutes`
8. `PATCH /api/v1/services/{id}` cambiando `priceBase` sin reason (422 esperado)
9. `PATCH /api/v1/services/{id}` cambiando `priceBase` con reason + verificacion por list

## 12) Smoke script SPR-B005
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b005.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. `POST /api/v1/auth/login` (veterinario)
3. resolver cliente/mascota/servicio demo
4. `POST /api/v1/visits` (walk-in)
5. `PATCH /api/v1/visits/{id}` (SOAP)
6. `POST /api/v1/visits/{id}/prescriptions`
7. `POST /api/v1/visits/{id}/attachments` (multipart PNG)
8. `POST /api/v1/visits/{id}/close`
9. `POST /api/v1/visits/{id}/reopen` (reason >= 10)

## 13) Smoke script SPR-B006
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b006.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. `POST /api/v1/auth/login` (superadmin)
3. crear visita walk-in demo
4. `GET/PUT /api/v1/config/tax`
5. `POST /api/v1/visits/{id}/invoices`
6. `PATCH /api/v1/invoices/{id}` (descuento total + reason)
7. `POST /api/v1/invoices/{id}/items` y `PATCH /api/v1/invoice-items/{itemId}`
8. `POST /api/v1/invoices/{id}/payments` (parcial CASH + parcial TRANSFER)
9. `GET /api/v1/invoices/{id}/export.csv`
10. `GET /api/v1/invoices/{id}/export.pdf`
11. `GET /api/v1/visits/{id}/instructions.pdf`
12. `POST /api/v1/invoices/{id}/void` y verificacion de bloqueo de pagos en VOID

## 14) Smoke script SPR-B007
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b007.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. `POST /api/v1/auth/login` (superadmin)
3. `GET /api/v1/units` + `POST /api/v1/products`
4. `GET /api/v1/products/{id}/stock` (stock inicial)
5. `POST /api/v1/stock/movements` tipo `IN` (recalculo de promedio)
6. `PUT /api/v1/services/{serviceId}/bom`
7. `POST /api/v1/visits/{visitId}/inventory/consume` (`BOM_ONLY`)
8. `POST /api/v1/visits/{visitId}/invoices` con item `PRODUCT` sin override y qty > stock (bloqueo esperado `insufficient_stock`)
9. `POST /api/v1/visits/{visitId}/invoices` con override + reason (permitido y auditado)

## 15) Smoke script SPR-B008
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b008.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. `POST /api/v1/auth/login` (superadmin)
3. `GET /api/v1/reports/appointments`
4. `GET /api/v1/reports/sales`
5. `GET /api/v1/reports/top-services`
6. `GET /api/v1/reports/inventory-consumption`
7. `GET /api/v1/reports/frequent`
8. `GET /api/v1/dashboard`
9. `GET /api/v1/reports/appointments/export.csv`
10. `GET /api/v1/reports/sales/export.pdf`

## 16) Validacion manual SPR-B009 (auditoria avanzada)
Con backend corriendo:
```powershell
cd backend
./mvnw test
./mvnw spring-boot:run
```

Flujo minimo sugerido:
1. Login `superadmin` (`POST /api/v1/auth/login`)
2. Ejecutar accion sensible disponible (p. ej. `PUT /api/v1/config/tax` con `reason` >= 10)
3. Consultar `GET /api/v1/audit/events` con `X-Branch-Id` y token
4. Verificar eventos `AUTH_LOGIN` y `CONFIG_TAX_UPDATE`

## 17) Smoke script SPR-B010
Con backend corriendo:
```powershell
pwsh -File scripts/smoke/spr-b010.ps1
```

Flujo que valida:
1. `GET /actuator/health`
2. login normal (`recepcion`)
3. lockout por intentos fallidos (4 fallos + locked)
4. rate limit login (flood -> 429)
5. 2FA admin: setup + enable + login challenge + completar 2FA (si aplica)

<!-- EOF -->
