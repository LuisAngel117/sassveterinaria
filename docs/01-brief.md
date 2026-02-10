# 01 - Brief (contexto de negocio)

## Vision
SaaSVeterinaria es una plataforma local-first para gestion operativa de clinicas veterinarias multi-sucursal, enfocada en agenda, atencion clinica SOAP, facturacion e inventario con trazabilidad completa.

## Problema
Clinicas pequenas y medianas suelen operar con herramientas separadas o manuales, lo que genera:
- perdida de citas y tiempos muertos,
- falta de trazabilidad clinica,
- errores de cobro y ajustes de stock sin evidencia,
- baja capacidad de auditar decisiones operativas.

## Publico objetivo
- Duenos de clinica que necesitan control operativo y datos confiables.
- Administradores de sucursal que coordinan agenda, caja e inventario.
- Recepcion y veterinarios que requieren flujos rapidos y consistentes.

## Alcance V1 vendible (local-first/offline-first)
- Login con roles y seleccion de sucursal.
- Agenda semanal con no-solape por sala, slot de 30 min y buffer de 10 min.
- Clientes + pacientes + historial clinico SOAP.
- Facturacion interna con IVA global configurable.
- Inventario por sucursal con BOM por servicio y costo promedio.
- Auditoria de acciones sensibles con reason required.
- Demo funcional en 2-3 minutos con seeds locales.

## No objetivos V1
- Pasarela de pagos online.
- Integraciones con WhatsApp, laboratorio externo o PAC fiscal.
- Multi-tenant real por empresa (en V1 es single-tenant + multi-sucursal).
- Sincronizacion cloud en tiempo real.

## Principios
- Local-first: operacion core sin dependencia de internet.
- Flujo vendible rapido: recorrido completo en pocos pasos.
- Seguridad por defecto: auth + permisos + auditoria desde el inicio.
- Trazabilidad fuerte: BRD/RTM/status/log/changelog alineados.
- Cambios controlados por gobernanza documental.

## Metricas de exito inicial
- Crear cita en menos de 60 segundos.
- Completar flujo "cita -> SOAP -> factura" sin salir de la app.
- Registrar ajustes sensibles con motivo y huella de auditoria.
- Ejecutar preflight documental sin errores.

## Glosario corto
- Sucursal (branch): unidad operativa de la clinica dentro del mismo tenant.
- SOAP: estructura clinica Subjective, Objective, Assessment, Plan.
- BOM: lista de insumos consumidos por servicio.
- Reason required: motivo obligatorio para acciones de alto impacto.

<!-- EOF -->
