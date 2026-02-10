# State Snapshot

## Resumen actual (hoy)

- Se consolidó el BRD con IDs estables `BRD-REQ-###`.
- Se definió el modelo de seguridad (JWT + refresh con rotación + 2FA TOTP para ADMIN/SUPERADMIN).
- Se definió el scoping por sucursal (`X-Branch-Id` validado contra claims).
- Se actualizó RTM con mapeo inicial a sprints PLANNED.
- Se actualizaron planes maestros por pista (BACK y FRONT).

## Decisiones cerradas (referencia rápida)

- Multi-tenant: NO. Multi-sucursal: SÍ (branch).
- No-solape v1: por SALA (veterinario queda como dato informativo, posible extensión futura vía RFC).
- Estados cita: reservado/confirmado/en_atencion/cerrado/cancelado + check-in separado.
- Historia clínica SOAP con plantillas y adjuntos (10MB, max 5).
- Facturación interna (no SRI), IVA configurable (default 15%) solo SUPERADMIN.
- Inventario por sucursal con costeo promedio y override auditado.
- Auditoría con before/after en sensibles, retención demo 90 días.

## Estado de sprints (alto nivel)

- Aún NO se han generado sprints detallados `SPR-B###`/`SPR-F###`.
- Solo existen planes maestro por pista:
  - `docs/sprints/spr-master-back.md`
  - `docs/sprints/spr-master-front.md`

## Próximo paso recomendado (bloqueante)

1) Aplicar esta T3 en repo (si no está aplicada).
2) Validar EOF (ya tienes el script).
3) Responder (usuario):
- “Acepto el plan maestro BACK tal cual”
- “Acepto el plan maestro FRONT tal cual”

Luego:
- Abrimos conversación BACK para generar sprints backend detallados (uno por tanda si hace falta).
- Separado: conversación FRONT para sprints frontend.

## Riesgos/bloqueos actuales

- Riesgo: contradicción “no-solape por sala” vs objetivo inicial “sala/veterinario”.
  - Mitigación: v1 cierra SALA; extender a VET solo vía RFC.
- Riesgo: “offline-first” interpretado como PWA sin backend local.
  - Mitigación: se define offline-first como “sin internet / sin servicios externos”, con backend+DB locales.

<!-- EOF -->
