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

## 4) Migraciones
- Flyway corre al iniciar backend.
- Migraciones activas:
  - `backend/src/main/resources/db/migration/V1__init.sql`
  - `backend/src/main/resources/db/migration/V2__agenda_core.sql`
  - `backend/src/main/resources/db/migration/V3__crm_clients_pets.sql`
  - `backend/src/main/resources/db/migration/V4__services_catalog.sql`
  - `backend/src/main/resources/db/migration/V5__clinical_visits.sql`

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

<!-- EOF -->
