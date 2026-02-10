param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/9] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") {
  throw "Health check failed"
}

Write-Host "[2/9] Login admin..."
$adminLoginBody = @{
  username = "admin"
  password = "Admin123!"
} | ConvertTo-Json

$adminLogin = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/login" -Body $adminLoginBody -ContentType "application/json"
$adminToken = $adminLogin.accessToken
$branchId = $adminLogin.branch.id

if ([string]::IsNullOrWhiteSpace($adminToken)) {
  throw "Admin login did not return accessToken"
}

$adminHeaders = @{
  Authorization = "Bearer $adminToken"
  "X-Branch-Id" = $branchId
}

Write-Host "[3/9] Create service as admin..."
$suffix = (Get-Date).ToString("yyyyMMddHHmmss")
$serviceName = "Servicio Smoke $suffix"
$createBody = @{
  name = $serviceName
  durationMinutes = 35
  priceBase = 22.50
} | ConvertTo-Json

$createdService = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/services" -Headers $adminHeaders -Body $createBody -ContentType "application/json"

Write-Host "[4/9] Login recepcion..."
$recepLoginBody = @{
  username = "recepcion"
  password = "Recepcion123!"
} | ConvertTo-Json

$recepLogin = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/login" -Body $recepLoginBody -ContentType "application/json"
$recepToken = $recepLogin.accessToken

$recepHeaders = @{
  Authorization = "Bearer $recepToken"
  "X-Branch-Id" = $branchId
}

Write-Host "[5/9] Recepcion list services (read allowed)..."
$listAsRecep = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/services?q=$([uri]::EscapeDataString($suffix))" -Headers $recepHeaders
if (@($listAsRecep).Count -lt 1) {
  throw "Recepcion list did not return created service"
}

Write-Host "[6/9] Recepcion create service must fail (403)..."
$forbiddenResponse = Invoke-WebRequest -Method Post -Uri "$BaseUrl/api/v1/services" -Headers $recepHeaders -Body $createBody -ContentType "application/json" -SkipHttpErrorCheck
if ($forbiddenResponse.StatusCode -ne 403) {
  throw "Expected 403 for recepcion create, got $($forbiddenResponse.StatusCode)"
}

Write-Host "[7/9] Admin update duration..."
$updateDurationBody = @{
  durationMinutes = 40
} | ConvertTo-Json

$updatedDuration = Invoke-RestMethod -Method Patch -Uri "$BaseUrl/api/v1/services/$($createdService.id)" -Headers $adminHeaders -Body $updateDurationBody -ContentType "application/json"
if ($updatedDuration.durationMinutes -ne 40) {
  throw "Duration update failed"
}

Write-Host "[8/9] Admin update price without reason must fail (422)..."
$updatePriceNoReasonBody = @{
  priceBase = 25.00
} | ConvertTo-Json

$missingReasonResponse = Invoke-WebRequest -Method Patch -Uri "$BaseUrl/api/v1/services/$($createdService.id)" -Headers $adminHeaders -Body $updatePriceNoReasonBody -ContentType "application/json" -SkipHttpErrorCheck
if ($missingReasonResponse.StatusCode -ne 422) {
  throw "Expected 422 for price update without reason, got $($missingReasonResponse.StatusCode)"
}

Write-Host "[9/9] Admin update price with reason + verify list..."
$updatePriceBody = @{
  priceBase = 25.00
  reason = "Ajuste de tarifa por demo local"
} | ConvertTo-Json

$updatedPrice = Invoke-RestMethod -Method Patch -Uri "$BaseUrl/api/v1/services/$($createdService.id)" -Headers $adminHeaders -Body $updatePriceBody -ContentType "application/json"
$verifyList = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/services?q=$([uri]::EscapeDataString($suffix))" -Headers $adminHeaders

Write-Host ""
Write-Host "Smoke SPR-B004 completed"
Write-Host "- Branch: $branchId"
Write-Host "- Created service id: $($createdService.id)"
Write-Host "- Recepcion list count: $(@($listAsRecep).Count)"
Write-Host "- Updated duration: $($updatedDuration.durationMinutes)"
Write-Host "- Updated price: $($updatedPrice.priceBase)"
Write-Host "- Verify list count: $(@($verifyList).Count)"
