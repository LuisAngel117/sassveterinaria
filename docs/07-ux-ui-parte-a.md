# 07A - UX/UI vendible - Parte A

## Principios UX
1. Claridad operativa primero: menos pasos para tareas de alta frecuencia.
2. Contexto visible: sucursal activa y rol siempre visibles.
3. Errores accionables: cada fallo explica causa y siguiente accion.
4. Consistencia: mismos patrones para crear, editar, confirmar y anular.
5. Velocidad de demo: flujo principal visible en menos de 3 clics por pantalla.

## Mapa de pantallas
1. Login
2. Verificacion 2FA (roles altos)
3. Seleccion de sucursal
4. Dashboard operativo
5. Agenda semanal
6. Clientes
7. Pacientes
8. Consulta SOAP
9. Facturacion
10. Inventario
11. Auditoria
12. Configuracion (roles/permisos/IVA)

## Roles y navegacion
- SUPERADMIN: acceso total, incluyendo configuracion global y auditoria.
- ADMIN: operacion completa de sucursal, sin ajustes globales de sistema.
- RECEPCION: agenda, clientes, pacientes, check-in, facturacion basica.
- VETERINARIO: agenda propia, consulta SOAP y cierre clinico.

## Reglas de navegacion
- Sidebar por modulos y topbar con sucursal activa.
- Cambio de sucursal invalida caches branch-scoped y recarga vistas.
- Acciones sensibles muestran modal de confirmacion con campo motivo.

<!-- EOF -->
