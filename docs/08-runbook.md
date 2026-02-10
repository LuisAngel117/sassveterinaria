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

## 4) Migraciones
- Flyway corre al iniciar backend.
- Migraciones activas:
  - `backend/src/main/resources/db/migration/V1__init.sql`
  - `backend/src/main/resources/db/migration/V2__agenda_core.sql`
  - `backend/src/main/resources/db/migration/V3__crm_clients_pets.sql`

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

<!-- EOF -->
