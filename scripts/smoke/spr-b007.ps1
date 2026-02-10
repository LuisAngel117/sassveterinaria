param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/11] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") {
  throw "Health check failed"
}

Write-Host "[2/11] Login as superadmin..."
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

Write-Host "[3/11] Resolve units..."
$units = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/units" -Headers $headers
$unit = $units | Where-Object { $_.code -eq "UN" } | Select-Object -First 1
if (-not $unit) {
  throw "Unit UN not found"
}

Write-Host "[4/11] Create demo product..."
$sku = "SMK-B007-" + (Get-Random -Minimum 1000 -Maximum 9999)
$createProductBody = @{
  sku = $sku
  name = "Producto Smoke B007 $sku"
  unitId = $unit.id
  minQty = 3
} | ConvertTo-Json
$product = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/products" -Headers $headers -Body $createProductBody -ContentType "application/json"

Write-Host "[5/11] Validate initial stock..."
$stock = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/products/$($product.id)/stock" -Headers $headers
if ([decimal]$stock.onHandQty -ne 0) {
  throw "Initial stock must be 0"
}

Write-Host "[6/11] Register IN movement..."
$movementInBody = @{
  productId = $product.id
  type = "IN"
  qty = 10
  unitCost = 4.25
} | ConvertTo-Json
$movementIn = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/stock/movements" -Headers $headers -Body $movementInBody -ContentType "application/json"
if ($movementIn.type -ne "IN") {
  throw "Expected movement type IN"
}

Write-Host "[7/11] Resolve demo pet/service..."
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
$service = $services | Where-Object { $_.name -eq "Vacunacion" } | Select-Object -First 1
if (-not $service) {
  throw "Service Vacunacion not found"
}

Write-Host "[8/11] Replace service BOM..."
$bomBody = @{
  items = @(
    @{
      productId = $product.id
      qty = 2
    }
  )
} | ConvertTo-Json -Depth 5
$bom = Invoke-RestMethod -Method Put -Uri "$BaseUrl/api/v1/services/$($service.id)/bom" -Headers $headers -Body $bomBody -ContentType "application/json"
if (-not $bom -or @($bom).Count -lt 1) {
  throw "BOM should contain at least one item"
}

Write-Host "[9/11] Create visit and consume BOM..."
$visitBody = @{
  petId = $petId
  serviceId = $service.id
  sReason = "Visita de prueba para consumo inventario"
  sAnamnesis = "Paciente estable para flujo smoke"
} | ConvertTo-Json
$visit = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits" -Headers $headers -Body $visitBody -ContentType "application/json"
$consumeBody = @{
  mode = "BOM_ONLY"
} | ConvertTo-Json
$consume = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/inventory/consume" -Headers $headers -Body $consumeBody -ContentType "application/json"
if (-not $consume.movements -or @($consume.movements).Count -lt 1) {
  throw "Consume should create movements"
}

Write-Host "[10/11] Validate invoice PRODUCT is blocked without stock override..."
$blocked = $false
try {
  $invoiceBlockedBody = @{
    items = @(
      @{
        itemType = "PRODUCT"
        itemId = $product.id
        description = "Producto Smoke B007"
        qty = 999
        unitPrice = 12.00
        discountAmount = 0
      }
    )
    discountAmount = 0
  } | ConvertTo-Json -Depth 6
  $null = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/invoices" -Headers $headers -Body $invoiceBlockedBody -ContentType "application/json"
} catch {
  $blocked = $true
}
if (-not $blocked) {
  throw "Expected invoice creation to fail with insufficient_stock"
}

Write-Host "[11/11] Create invoice with override + reason..."
$invoiceOverrideBody = @{
  items = @(
    @{
      itemType = "PRODUCT"
      itemId = $product.id
      description = "Producto Smoke B007 Override"
      qty = 999
      unitPrice = 12.00
      discountAmount = 0
      override = $true
      reason = "Override autorizado para validar escenario smoke"
    }
  )
  discountAmount = 0
} | ConvertTo-Json -Depth 6
$invoice = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/invoices" -Headers $headers -Body $invoiceOverrideBody -ContentType "application/json"
if (-not $invoice.invoice.id) {
  throw "Invoice override flow did not create invoice"
}

Write-Host ""
Write-Host "Smoke SPR-B007 completed"
Write-Host "- Branch: $branchId"
Write-Host "- Product id: $($product.id)"
Write-Host "- Visit id: $($visit.id)"
Write-Host "- Invoice id (override): $($invoice.invoice.id)"
