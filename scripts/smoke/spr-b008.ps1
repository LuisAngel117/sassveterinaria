param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/9] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") {
  throw "Health check failed"
}

Write-Host "[2/9] Login as superadmin..."
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

$from = (Get-Date).AddDays(-30).ToString("o")
$to = (Get-Date).ToString("o")

Write-Host "[3/9] Reports - appointments..."
$appointments = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/reports/appointments?from=$([uri]::EscapeDataString($from))&to=$([uri]::EscapeDataString($to))&groupBy=day&page=0&size=20" -Headers $headers
if ($null -eq $appointments.total) {
  throw "appointments report missing total"
}

Write-Host "[4/9] Reports - sales..."
$sales = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/reports/sales?from=$([uri]::EscapeDataString($from))&to=$([uri]::EscapeDataString($to))&groupBy=day" -Headers $headers
if ($null -eq $sales.totalFacturado) {
  throw "sales report missing totals"
}

Write-Host "[5/9] Reports - top services..."
$top = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/reports/top-services?from=$([uri]::EscapeDataString($from))&to=$([uri]::EscapeDataString($to))&metric=count&limit=5" -Headers $headers
if ($null -eq $top.items) {
  throw "top-services report missing items"
}

Write-Host "[6/9] Reports - inventory consumption + frequent..."
$inventory = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/reports/inventory-consumption?from=$([uri]::EscapeDataString($from))&to=$([uri]::EscapeDataString($to))&groupBy=product" -Headers $headers
$frequent = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/reports/frequent?from=$([uri]::EscapeDataString($from))&to=$([uri]::EscapeDataString($to))&dimension=client&limit=10" -Headers $headers
if ($null -eq $inventory.totalQty) { throw "inventory report missing totals" }
if ($null -eq $frequent.totalVisits) { throw "frequent report missing totals" }

Write-Host "[7/9] Dashboard..."
$dashboard = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/dashboard" -Headers $headers
if ($null -eq $dashboard.todayAppointmentsCount) {
  throw "dashboard missing todayAppointmentsCount"
}

Write-Host "[8/9] Export CSV..."
$csv = Invoke-WebRequest -Method Get -Uri "$BaseUrl/api/v1/reports/appointments/export.csv?from=$([uri]::EscapeDataString($from))&to=$([uri]::EscapeDataString($to))&groupBy=day" -Headers $headers
if (-not $csv.Content) {
  throw "appointments csv export is empty"
}

Write-Host "[9/9] Export PDF..."
$pdf = Invoke-WebRequest -Method Get -Uri "$BaseUrl/api/v1/reports/sales/export.pdf?from=$([uri]::EscapeDataString($from))&to=$([uri]::EscapeDataString($to))&groupBy=day" -Headers $headers
if (-not $pdf.Content) {
  throw "sales pdf export is empty"
}

Write-Host ""
Write-Host "Smoke SPR-B008 completed"
Write-Host "- Branch: $branchId"
Write-Host "- Appointments total: $($appointments.total)"
Write-Host "- Sales totalFacturado: $($sales.totalFacturado)"
Write-Host "- Inventory totalQty: $($inventory.totalQty)"
