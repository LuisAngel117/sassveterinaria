# 06A - Dominio (modelo de datos) - Parte A

## Entidades y campos

### branch
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| code | varchar(20) | unico |
| name | varchar(120) | requerido |
| is_active | boolean | default true |
| created_at | timestamptz | requerido |

### app_user
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| email | varchar(160) | unico |
| full_name | varchar(160) | requerido |
| password_hash | varchar(255) | requerido |
| role_code | varchar(30) | requerido |
| is_active | boolean | default true |
| locked_until | timestamptz | nullable |
| created_at | timestamptz | requerido |

### user_branch
| Campo | Tipo | Regla |
|---|---|---|
| user_id | uuid | FK -> app_user.id |
| branch_id | uuid | FK -> branch.id |
| is_default | boolean | default false |

### client
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id |
| full_name | varchar(160) | requerido |
| phone | varchar(30) | nullable |
| email | varchar(160) | nullable |
| address | varchar(255) | nullable |
| created_at | timestamptz | requerido |

### pet
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id |
| client_id | uuid | FK -> client.id |
| name | varchar(120) | requerido |
| species | varchar(80) | requerido |
| breed | varchar(120) | nullable |
| sex | varchar(20) | nullable |
| birth_date | date | nullable |
| weight_kg | numeric(6,2) | nullable |
| alerts | text | nullable |
| created_at | timestamptz | requerido |

### room
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id |
| name | varchar(80) | requerido |
| is_active | boolean | default true |

### appointment
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id |
| room_id | uuid | FK -> room.id |
| client_id | uuid | FK -> client.id |
| pet_id | uuid | FK -> pet.id |
| veterinarian_id | uuid | FK -> app_user.id |
| starts_at | timestamptz | requerido |
| ends_at | timestamptz | requerido |
| status | varchar(30) | requerido |
| reason | varchar(255) | nullable |
| notes | text | nullable |
| created_at | timestamptz | requerido |

### soap_note
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id |
| appointment_id | uuid | FK -> appointment.id |
| subjective | text | requerido |
| objective | text | requerido |
| assessment | text | requerido |
| plan | text | requerido |
| status | varchar(30) | requerido |
| closed_at | timestamptz | nullable |
| created_by | uuid | FK -> app_user.id |
| updated_at | timestamptz | requerido |

### soap_attachment
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| soap_note_id | uuid | FK -> soap_note.id |
| file_name | varchar(255) | requerido |
| mime_type | varchar(100) | requerido |
| size_bytes | bigint | max 10485760 |
| storage_path | varchar(255) | requerido |
| created_at | timestamptz | requerido |

### service_catalog
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| code | varchar(30) | unico |
| name | varchar(120) | requerido |
| default_price | numeric(12,2) | requerido |
| is_active | boolean | default true |

### product
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| code | varchar(30) | unico |
| name | varchar(160) | requerido |
| unit | varchar(20) | requerido |
| is_active | boolean | default true |

### inventory_balance
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id |
| product_id | uuid | FK -> product.id |
| qty_on_hand | numeric(14,3) | requerido |
| avg_cost | numeric(12,4) | requerido |
| updated_at | timestamptz | requerido |

### inventory_movement
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id |
| product_id | uuid | FK -> product.id |
| movement_type | varchar(20) | IN, OUT, ADJUST |
| qty | numeric(14,3) | requerido |
| unit_cost | numeric(12,4) | nullable |
| reason | varchar(255) | nullable |
| reference_type | varchar(50) | nullable |
| reference_id | uuid | nullable |
| created_by | uuid | FK -> app_user.id |
| created_at | timestamptz | requerido |

### service_bom
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| service_id | uuid | FK -> service_catalog.id |
| product_id | uuid | FK -> product.id |
| qty_required | numeric(14,3) | requerido |

### invoice
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id |
| appointment_id | uuid | FK -> appointment.id nullable |
| invoice_number | varchar(40) | unico por branch |
| status | varchar(20) | requerido |
| subtotal | numeric(12,2) | requerido |
| tax_rate | numeric(6,4) | requerido |
| tax_amount | numeric(12,2) | requerido |
| total | numeric(12,2) | requerido |
| cancellation_reason | varchar(255) | nullable |
| created_by | uuid | FK -> app_user.id |
| created_at | timestamptz | requerido |

### invoice_item
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| invoice_id | uuid | FK -> invoice.id |
| item_type | varchar(20) | SERVICE, PRODUCT |
| item_id | uuid | requerido |
| description | varchar(200) | requerido |
| qty | numeric(12,3) | requerido |
| unit_price | numeric(12,2) | requerido |
| line_total | numeric(12,2) | requerido |

### audit_event
| Campo | Tipo | Regla |
|---|---|---|
| id | uuid | PK |
| branch_id | uuid | FK -> branch.id nullable |
| actor_id | uuid | FK -> app_user.id |
| action_code | varchar(80) | requerido |
| entity_name | varchar(80) | requerido |
| entity_id | uuid | requerido |
| reason | varchar(255) | nullable |
| before_json | jsonb | nullable |
| after_json | jsonb | nullable |
| created_at | timestamptz | requerido |

## Relaciones y FKs principales
- `user_branch.user_id -> app_user.id`
- `user_branch.branch_id -> branch.id`
- `client.branch_id -> branch.id`
- `pet.client_id -> client.id`
- `appointment.pet_id -> pet.id`
- `appointment.room_id -> room.id`
- `soap_note.appointment_id -> appointment.id`
- `soap_attachment.soap_note_id -> soap_note.id`
- `inventory_balance(branch_id, product_id)` unico
- `inventory_movement.reference_id` apunta a factura/cita/ajuste segun `reference_type`
- `invoice_item.invoice_id -> invoice.id`
- `audit_event.actor_id -> app_user.id`

## Reglas de integridad
1. `appointment.ends_at > appointment.starts_at`.
2. No-solape por `room_id` en el rango `[starts_at, ends_at]`.
3. `soap_attachment.size_bytes <= 10485760`.
4. `invoice.total = subtotal + tax_amount`.
5. `inventory_balance.qty_on_hand` no puede ser negativo salvo permiso explicito de ajuste.
6. `invoice.status = VOID` requiere `cancellation_reason`.
7. Eventos sensibles generan fila en `audit_event`.

<!-- EOF -->
