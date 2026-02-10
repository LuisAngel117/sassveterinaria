param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/8] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") {
  throw "Health check failed"
}

Write-Host "[2/8] Login as recepcion..."
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

$headers = @{
  Authorization = "Bearer $accessToken"
  "X-Branch-Id" = $branchId
}

Write-Host "[3/8] Create client..."
$suffix = (Get-Date).ToString("yyyyMMddHHmmss")
$clientBody = @{
  fullName = "Cliente Smoke $suffix"
  identification = "0102030405"
  phone = "0991001001"
  email = "cliente.$suffix@example.com"
  address = "Direccion smoke"
  notes = "Cliente creado por SPR-B003 smoke"
} | ConvertTo-Json

$client = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/clients" -Headers $headers -Body $clientBody -ContentType "application/json"

Write-Host "[4/8] Search client by name and phone..."
$searchByName = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/clients?q=$([uri]::EscapeDataString($suffix))" -Headers $headers
if ($searchByName.totalElements -lt 1) {
  throw "Client search by name did not find records"
}

$searchByPhone = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/clients?q=0991001001" -Headers $headers
if ($searchByPhone.totalElements -lt 1) {
  throw "Client search by phone did not find records"
}

Write-Host "[5/8] Create pet..."
$internalCode = "PET-$suffix"
$petBody = @{
  internalCode = $internalCode
  name = "Mascota Smoke $suffix"
  species = "Canino"
  breed = "Mestizo"
  sex = "F"
  weightKg = 12.5
  neutered = $true
  alerts = "Ninguna"
  history = "Paciente smoke"
} | ConvertTo-Json

$pet = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/clients/$($client.id)/pets" -Headers $headers -Body $petBody -ContentType "application/json"

Write-Host "[6/8] Validate duplicate internalCode conflict..."
$duplicateResponse = Invoke-WebRequest -Method Post -Uri "$BaseUrl/api/v1/clients/$($client.id)/pets" -Headers $headers -Body $petBody -ContentType "application/json" -SkipHttpErrorCheck
if ($duplicateResponse.StatusCode -ne 409) {
  throw "Expected 409 for duplicate internalCode, got $($duplicateResponse.StatusCode)"
}

$duplicateError = $duplicateResponse.Content | ConvertFrom-Json
if ($duplicateError.errorCode -ne "PET_INTERNAL_CODE_CONFLICT") {
  throw "Expected PET_INTERNAL_CODE_CONFLICT, got $($duplicateError.errorCode)"
}

Write-Host "[7/8] List pets by client..."
$pets = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/clients/$($client.id)/pets" -Headers $headers
if (@($pets).Count -lt 1) {
  throw "Pet list did not return records"
}

Write-Host "[8/8] Detail + patch pet..."
$petDetail = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/pets/$($pet.id)" -Headers $headers
$patchBody = @{
  breed = "Labrador Mix"
  history = "Historia actualizada por smoke"
} | ConvertTo-Json

$updatedPet = Invoke-RestMethod -Method Patch -Uri "$BaseUrl/api/v1/pets/$($pet.id)" -Headers $headers -Body $patchBody -ContentType "application/json"

Write-Host ""
Write-Host "Smoke SPR-B003 completed"
Write-Host "- Branch: $branchId"
Write-Host "- Client id: $($client.id)"
Write-Host "- Pet id: $($pet.id)"
Write-Host "- Search by name total: $($searchByName.totalElements)"
Write-Host "- Search by phone total: $($searchByPhone.totalElements)"
Write-Host "- Pets listed: $(@($pets).Count)"
Write-Host "- Pet detail name: $($petDetail.name)"
Write-Host "- Updated pet breed: $($updatedPet.breed)"
