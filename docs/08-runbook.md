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
- Migracion inicial del sprint: `backend/src/main/resources/db/migration/V1__init.sql`.

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

## 7) Usuarios demo seed (SPR-B001)
- `superadmin` / `SuperAdmin123!`
- `admin` / `Admin123!`
- `recepcion` / `Recepcion123!`
- `veterinario` / `Veterinario123!`

Branch demo:
- `CENTRO` / `Sucursal Centro`

## 8) Troubleshooting
- Si falla conexión DB: revisar `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Si falla JWT: revisar `APP_JWT_SECRET` (min 32 bytes).
- Si falla scope: verificar header `X-Branch-Id` y claim `branch_id` del token.

<!-- EOF -->
