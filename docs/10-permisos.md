# 10 - Permisos

## Roles iniciales
- OWNER_CLINICA
- ADMIN_CLINICA
- RECEPCION
- MEDICO
- SOPORTE

## Matriz base
| Accion | OWNER_CLINICA | ADMIN_CLINICA | RECEPCION | MEDICO | SOPORTE |
|---|---|---|---|---|---|
| Gestionar usuarios | X | X |  |  |  |
| Gestionar agenda | X | X | X | X |  |
| Registrar pacientes | X | X | X | X |  |
| Ver auditoria | X | X |  |  | X |

## Reglas
- Ningun rol opera fuera de su tenant.
- Acciones criticas requieren registro de auditoria.
- Cambios de permisos deben documentarse.

<!-- EOF -->