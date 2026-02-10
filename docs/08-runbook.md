# 08 — Runbook (operación local)

## 1) Prerrequisitos
- Java 21
- Node.js LTS (recomendado 20+)
- Postgres 17 instalado local
- PowerShell (pwsh)

## 2) Base de datos local
- Crear DB (nombre sugerido): `sassveterinaria`
- Crear usuario con password (según env local)

## 3) Variables de entorno (ejemplo)
> Nota: ajustar a estructura real del repo cuando exista backend/frontend.

Backend (ejemplo):
- DB_URL=jdbc:postgresql://localhost:5432/sassveterinaria
- DB_USER=...
- DB_PASS=...
- JWT_ACCESS_SECRET=...
- JWT_REFRESH_SECRET=...
- STORAGE_DIR=./.local-storage (adjuntos)
- CORS_ALLOWED_ORIGINS=http://localhost:3000
- IVA_DEFAULT_RATE=0.15

Frontend (ejemplo):
- NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1

## 4) Migraciones
- Flyway corre al iniciar backend (según configuración).
- Verificar que la versión de Flyway soporte Postgres 17 (recomendado Flyway 10.20.1+).

## 5) Cómo levantar (local)
> TBD: comandos exactos dependerán de estructura del repo.

- Backend:
  - `./mvnw test`
  - `./mvnw spring-boot:run`

- Frontend:
  - `npm install`
  - `npm run dev`
  - `npm run build`

## 6) Demo “2–3 minutos” (script humano)
1) Login como RECEPCION
2) Selecciona sucursal “Sucursal Centro”
3) Agenda → crear cita (Sala 1, Servicio Consulta)
4) Check-in → Iniciar atención
5) Login como VETERINARIO (u otra pestaña) → abrir atención → completar SOAP → cerrar
6) Regresar como RECEPCION → generar factura → registrar pago → ver estado pagado
7) Ir a Inventario → verificar movimiento/consumo

## 7) Usuarios demo (credenciales fijas)
- SUPERADMIN: `superadmin` / `SuperAdmin123!` (2FA: habilitable)
- ADMIN: `admin` / `Admin123!`
- RECEPCION: `recepcion` / `Recepcion123!`
- VETERINARIO: `veterinario` / `Veterinario123!`

> Nota: solo demo. En stage/online deben cambiarse.

## 8) Troubleshooting
- Si falla DB: revisar servicio Postgres, credenciales y puerto.
- Si falla CORS: verificar CORS_ALLOWED_ORIGINS.
- Si falla migración: revisar versión Flyway y logs.

<!-- EOF -->
