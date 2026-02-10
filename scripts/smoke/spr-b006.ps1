param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/12] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") {
  throw "Health check failed"
}

Write-Host "[2/12] Login as superadmin..."
$loginBody = @{
  username = "superadmin"
  password = "SuperAdmin123!"
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/login" -Body $loginBody -ContentType "application/json"
$token = $login.accessToken
$branchId = $login.branch.id
if ([string]::IsNullOrWhiteSpace($token)) {
  throw "Login did not return accessToken"
}

$headers = @{
  Authorization = "Bearer $token"
  "X-Branch-Id" = $branchId
}

Write-Host "[3/12] Resolve demo pet and service..."
$clients = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/clients?q=Luis%20Demo" -Headers $headers
if (-not $clients.content -or $clients.content.Count -lt 1) {
  throw "No demo client found"
}
$clientId = $clients.content[0].id

$pets = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/clients/$clientId/pets" -Headers $headers
if (-not $pets -or @($pets).Count -lt 1) {
  throw "No pet found for demo client"
}
$petId = $pets[0].id

$services = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/services" -Headers $headers
if (-not $services -or @($services).Count -lt 1) {
  throw "No services available"
}
$serviceId = $services[0].id

Write-Host "[4/12] Create visit (walk-in)..."
$createVisitBody = @{
  petId = $petId
  serviceId = $serviceId
  sReason = "Atencion para facturacion demo"
  sAnamnesis = "Paciente en control estable"
} | ConvertTo-Json

$visit = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits" -Headers $headers -Body $createVisitBody -ContentType "application/json"
if ($visit.status -ne "OPEN") {
  throw "Visit was expected OPEN"
}

Write-Host "[5/12] Tax config read/update..."
$tax = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/config/tax" -Headers $headers
$taxRateTarget = 0.1200
$taxUpdateBody = @{
  taxRate = $taxRateTarget
  reason = "Ajuste temporal de IVA para smoke B006"
} | ConvertTo-Json
$taxUpdated = Invoke-RestMethod -Method Put -Uri "$BaseUrl/api/v1/config/tax" -Headers $headers -Body $taxUpdateBody -ContentType "application/json"

Write-Host "[6/12] Create invoice from visit..."
$createInvoiceBody = @{
  items = @(
    @{
      itemType = "SERVICE"
      itemId = $serviceId
      qty = 1
      discountAmount = 0
    }
  )
  discountAmount = 0
} | ConvertTo-Json -Depth 5

$invoiceDetail = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/invoices" -Headers $headers -Body $createInvoiceBody -ContentType "application/json"
$invoiceId = $invoiceDetail.invoice.id
if ($invoiceDetail.invoice.status -ne "PENDING") {
  throw "Invoice should start as PENDING"
}

Write-Host "[7/12] Apply invoice discount + add/patch item..."
$patchInvoiceBody = @{
  discountAmount = 1.00
  reason = "Descuento comercial aprobado por supervisor"
} | ConvertTo-Json
$invoiceDetail = Invoke-RestMethod -Method Patch -Uri "$BaseUrl/api/v1/invoices/$invoiceId" -Headers $headers -Body $patchInvoiceBody -ContentType "application/json"

$productItemBody = @{
  itemType = "PRODUCT"
  itemId = ([guid]::NewGuid().ToString())
  description = "Insumo demo"
  qty = 1
  unitPrice = 2.50
  discountAmount = 0
} | ConvertTo-Json

$invoiceDetail = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/invoices/$invoiceId/items" -Headers $headers -Body $productItemBody -ContentType "application/json"
$itemId = $invoiceDetail.items[-1].id

$patchItemBody = @{
  unitPrice = 3.00
  reason = "Actualizacion de precio por tarifa vigente"
} | ConvertTo-Json
$invoiceDetail = Invoke-RestMethod -Method Patch -Uri "$BaseUrl/api/v1/invoice-items/$itemId" -Headers $headers -Body $patchItemBody -ContentType "application/json"

Write-Host "[8/12] Register partial/mixed payments..."
$firstPaymentBody = @{
  method = "CASH"
  amount = 5.00
  reference = "CAJA-DEMO"
} | ConvertTo-Json
$null = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/invoices/$invoiceId/payments" -Headers $headers -Body $firstPaymentBody -ContentType "application/json"

$invoiceDetail = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/invoices/$invoiceId" -Headers $headers
$remaining = [decimal]$invoiceDetail.invoice.total - [decimal]$invoiceDetail.invoice.paidTotal
if ($remaining -le 0) {
  $remaining = 0.01
}
$secondPaymentBody = @{
  method = "TRANSFER"
  amount = $remaining
  reference = "TRX-B006"
} | ConvertTo-Json
$null = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/invoices/$invoiceId/payments" -Headers $headers -Body $secondPaymentBody -ContentType "application/json"

$invoiceDetail = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/invoices/$invoiceId" -Headers $headers
if ($invoiceDetail.invoice.status -ne "PAID") {
  throw "Invoice should be PAID after payments"
}

Write-Host "[9/12] Export invoice CSV/PDF..."
$csv = Invoke-WebRequest -Method Get -Uri "$BaseUrl/api/v1/invoices/$invoiceId/export.csv" -Headers $headers
$pdf = Invoke-WebRequest -Method Get -Uri "$BaseUrl/api/v1/invoices/$invoiceId/export.pdf" -Headers $headers
if (-not $csv.Content) { throw "CSV export is empty" }
if (-not $pdf.Content) { throw "PDF export is empty" }

Write-Host "[10/12] Export visit instructions PDF..."
$instructions = Invoke-WebRequest -Method Get -Uri "$BaseUrl/api/v1/visits/$($visit.id)/instructions.pdf" -Headers $headers
if (-not $instructions.Content) { throw "Instructions PDF export is empty" }

Write-Host "[11/12] Void invoice with reason..."
$voidBody = @{
  reason = "Anulacion de prueba para validacion sprint B006"
} | ConvertTo-Json
$voided = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/invoices/$invoiceId/void" -Headers $headers -Body $voidBody -ContentType "application/json"
if ($voided.status -ne "VOID") {
  throw "Invoice should be VOID after void endpoint"
}

Write-Host "[12/12] Validate VOID blocks new payments..."
$blocked = $false
try {
  $extraPaymentBody = @{
    method = "CARD"
    amount = 1.00
    reference = "POST-VOID"
  } | ConvertTo-Json
  $null = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/invoices/$invoiceId/payments" -Headers $headers -Body $extraPaymentBody -ContentType "application/json"
} catch {
  $blocked = $true
}
if (-not $blocked) {
  throw "Expected payment to fail for VOID invoice"
}

Write-Host ""
Write-Host "Smoke SPR-B006 completed"
Write-Host "- Branch: $branchId"
Write-Host "- Visit id: $($visit.id)"
Write-Host "- Invoice id: $invoiceId"
Write-Host "- Tax rate updated to: $($taxUpdated.taxRate)"
Write-Host "- Final invoice status: $($voided.status)"
