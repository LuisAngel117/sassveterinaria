param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/4] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") {
  throw "Health check failed"
}

Write-Host "[2/4] Login as recepcion..."
$loginBody = @{
  username = "recepcion"
  password = "Recepcion123!"
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/login" -Body $loginBody -ContentType "application/json"
$accessToken = $login.accessToken
$branchId = $login.branch.id

if ([string]::IsNullOrWhiteSpace($accessToken)) {
  throw "Login did not return accessToken"
}

Write-Host "[3/4] Create appointment..."
$now = [DateTimeOffset]::UtcNow.AddMinutes(10)
$end = $now.AddMinutes(30)

$appointmentBody = @{
  startsAt = $now.ToString("o")
  endsAt = $end.ToString("o")
  status = "RESERVADO"
  reason = "Control demo"
  notes = "Creada por smoke SPR-B001"
} | ConvertTo-Json

$headers = @{
  Authorization = "Bearer $accessToken"
  "X-Branch-Id" = $branchId
}

$created = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/appointments" -Headers $headers -Body $appointmentBody -ContentType "application/json"

Write-Host "[4/4] List appointments..."
$list = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/appointments" -Headers $headers

Write-Host "\nSmoke completed"
Write-Host "- Branch: $branchId"
Write-Host "- Created appointment id: $($created.id)"
Write-Host "- Total appointments listed: $(@($list).Count)"
