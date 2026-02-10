# 09 — Stage/Release (futuro online)

## 1) Qué cambia al ir online
- CORS por dominios reales
- HTTPS obligatorio
- Secrets en vault/CI
- Storage de adjuntos en servicio (S3 u otro) — no local

## 2) Online-only
- Recordatorios: integración email/WhatsApp/SMS (feature flag)
- SRI e-factura real (fuera v1)

## 3) Feature flags (concepto)
- `REMINDERS_ENABLED` (default false)
- `SRI_EINVOICE_ENABLED` (default false)

## 4) Checklist stage (TBD)
- Deploy pipeline
- DB migraciones en stage
- Smoke tests stage
- Hardening de rate limit

<!-- EOF -->
