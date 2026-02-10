param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/9] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") {
  throw "Health check failed"
}

Write-Host "[2/9] Login as veterinario..."
$loginBody = @{
  username = "veterinario"
  password = "Veterinario123!"
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

Write-Host "[3/9] Resolve demo pet and service..."
$clients = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/clients?q=Luis%20Demo" -Headers $headers
if (-not $clients.content -or $clients.content.Count -lt 1) {
  throw "No demo client found for smoke"
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

Write-Host "[4/9] Create visit (walk-in)..."
$createVisitBody = @{
  petId = $petId
  serviceId = $serviceId
  sReason = "Control general de rutina"
  sAnamnesis = "Paciente activo, apetito normal"
  oFindings = "Sin hallazgos relevantes"
} | ConvertTo-Json

$visit = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits" -Headers $headers -Body $createVisitBody -ContentType "application/json"
if ($visit.status -ne "OPEN") {
  throw "Visit was expected OPEN"
}

Write-Host "[5/9] Patch SOAP..."
$patchVisitBody = @{
  aDiagnosis = "Paciente estable"
  pTreatment = "Continuar control preventivo"
  pInstructions = "Revisar en 30 dias"
} | ConvertTo-Json

$patchedVisit = Invoke-RestMethod -Method Patch -Uri "$BaseUrl/api/v1/visits/$($visit.id)" -Headers $headers -Body $patchVisitBody -ContentType "application/json"
if ($patchedVisit.aDiagnosis -ne "Paciente estable") {
  throw "Visit patch failed"
}

Write-Host "[6/9] Create prescription..."
$rxBody = @{
  medication = "Multivitaminico"
  dose = "1"
  unit = "tableta"
  frequency = "cada 24h"
  duration = "5 dias"
  route = "oral"
  notes = "Administrar con alimento"
} | ConvertTo-Json

$rx = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/prescriptions" -Headers $headers -Body $rxBody -ContentType "application/json"

Write-Host "[7/9] Upload attachment..."
$tempPng = Join-Path $env:TEMP ("spr-b005-" + [guid]::NewGuid().ToString() + ".png")
$pngBytes = [Convert]::FromBase64String("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8Xw8AAoMBgQJ3WfEAAAAASUVORK5CYII=")
[System.IO.File]::WriteAllBytes($tempPng, $pngBytes)

$uploadResponseJson = & curl.exe -sS -X POST "$BaseUrl/api/v1/visits/$($visit.id)/attachments" `
  -H "Authorization: Bearer $token" `
  -H "X-Branch-Id: $branchId" `
  -F "file=@$tempPng;type=image/png"

if ([string]::IsNullOrWhiteSpace($uploadResponseJson)) {
  throw "Attachment upload returned empty response"
}
$attachment = $uploadResponseJson | ConvertFrom-Json

Write-Host "[8/9] Close visit..."
$closedVisit = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/close" -Headers $headers
if ($closedVisit.status -ne "CLOSED") {
  throw "Visit close failed"
}

Write-Host "[9/9] Reopen visit with reason..."
$reopenBody = @{
  reason = "Correccion clinica posterior a revision"
} | ConvertTo-Json
$reopenedVisit = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/reopen" -Headers $headers -Body $reopenBody -ContentType "application/json"
if ($reopenedVisit.status -ne "OPEN") {
  throw "Visit reopen failed"
}

Remove-Item -Path $tempPng -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "Smoke SPR-B005 completed"
Write-Host "- Branch: $branchId"
Write-Host "- Visit id: $($visit.id)"
Write-Host "- Prescription id: $($rx.id)"
Write-Host "- Attachment id: $($attachment.id)"
Write-Host "- Final visit status: $($reopenedVisit.status)"
