# 03 - Arquitectura

## Estilo
Arquitectura modular orientada a dominios, con separacion entre capas de aplicacion, dominio e infraestructura.

## Principios
- Tenant scoping obligatorio en datos y consultas.
- Contratos explicitos entre backend y frontend.
- Observabilidad y auditoria desde el inicio.
- Seguridad por defecto.

## Capas objetivo
- Presentacion: interfaz web.
- Aplicacion: casos de uso.
- Dominio: entidades, reglas y eventos.
- Infraestructura: persistencia, auth, mensajeria.

## Artefactos de control
- ADRs para decisiones irreversibles.
- RFCs para cambios mayores.
- RTM para trazabilidad de requerimientos.

<!-- EOF -->