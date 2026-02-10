param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/8] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") {
  throw "Health check failed"
}

Write-Host "[2/8] Login as admin..."
$loginBody = @{
  username = "admin"
  password = "Admin123!"
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/login" -Body $loginBody -ContentType "application/json"
$accessToken = $login.accessToken
$branchId = $login.branch.id

if ([string]::IsNullOrWhiteSpace($accessToken)) {
  throw "Login did not return accessToken"
}

$headers = @{
  Authorization = "Bearer $accessToken"
  "X-Branch-Id" = $branchId
}

Write-Host "[3/8] Create room..."
$roomBody = @{
  name = "Sala A SPR-B002"
} | ConvertTo-Json

$room = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/rooms" -Headers $headers -Body $roomBody -ContentType "application/json"

Write-Host "[4/8] Read services..."
$services = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/services" -Headers $headers
if (-not $services -or $services.Count -lt 1) {
  throw "No active services available for branch $branchId"
}

$serviceId = $services[0].id

Write-Host "[5/8] Create base appointment..."
$startsAt = [DateTimeOffset]::UtcNow.AddHours(2)
$appointmentBody = @{
  roomId = $room.id
  serviceId = $serviceId
  startsAt = $startsAt.ToString("o")
  reason = "Consulta de control"
  notes = "Creada por smoke SPR-B002"
} | ConvertTo-Json

$apptA = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/appointments" -Headers $headers -Body $appointmentBody -ContentType "application/json"

Write-Host "[6/8] Validate overlap blocked without overbook..."
$overlapStart = $startsAt.AddMinutes(10)
$overlapBody = @{
  roomId = $room.id
  serviceId = $serviceId
  startsAt = $overlapStart.ToString("o")
  reason = "Intento sin sobre-cupo"
} | ConvertTo-Json

$overlapResponse = Invoke-WebRequest -Method Post -Uri "$BaseUrl/api/v1/appointments" -Headers $headers -Body $overlapBody -ContentType "application/json" -SkipHttpErrorCheck
if ($overlapResponse.StatusCode -ne 422) {
  throw "Expected 422 for overlap without overbook, got $($overlapResponse.StatusCode)"
}

$overlapError = $overlapResponse.Content | ConvertFrom-Json
if ($overlapError.errorCode -ne "APPT_OVERLAP") {
  throw "Expected APPT_OVERLAP, got $($overlapError.errorCode)"
}

Write-Host "[7/8] Create overlap with overbook reason..."
$overbookBody = @{
  roomId = $room.id
  serviceId = $serviceId
  startsAt = $overlapStart.ToString("o")
  overbookReason = "Sobre-cupo aprobado por administracion"
} | ConvertTo-Json

$apptB = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/appointments" -Headers $headers -Body $overbookBody -ContentType "application/json"
if (-not $apptB.isOverbook) {
  throw "Expected appointment to be marked as overbook"
}

Write-Host "[8/8] Check-in + week list..."
$null = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/appointments/$($apptA.id)/checkin" -Headers $headers

$from = $startsAt.AddHours(-2).ToString("o")
$to = $startsAt.AddHours(24).ToString("o")
$weekList = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/appointments?from=$([uri]::EscapeDataString($from))&to=$([uri]::EscapeDataString($to))&roomId=$($room.id)" -Headers $headers

Write-Host ""
Write-Host "Smoke SPR-B002 completed"
Write-Host "- Branch: $branchId"
Write-Host "- Room: $($room.id)"
Write-Host "- Service used: $serviceId"
Write-Host "- Appointment A: $($apptA.id)"
Write-Host "- Appointment B (overbook): $($apptB.id)"
Write-Host "- Week list count: $(@($weekList).Count)"
