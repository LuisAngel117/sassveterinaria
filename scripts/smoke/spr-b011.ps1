param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Write-ProblemDetails {
  param([Parameter(Mandatory = $true)]$ErrorRecord)

  $raw = $ErrorRecord.ErrorDetails.Message
  if ([string]::IsNullOrWhiteSpace($raw)) {
    try {
      $raw = [string]$ErrorRecord.Exception.Response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    } catch {
      $raw = $null
    }
  }

  if (-not [string]::IsNullOrWhiteSpace($raw)) {
    try {
      $problem = $raw | ConvertFrom-Json
      Write-Host ("  - problem.title: " + $problem.title)
      Write-Host ("  - problem.status: " + $problem.status)
      Write-Host ("  - problem.detail: " + $problem.detail)
      if ($problem.errorCode) {
        Write-Host ("  - problem.errorCode: " + $problem.errorCode)
      }
      return
    } catch {
    }
  }

  try {
    $statusCode = [int]$ErrorRecord.Exception.Response.StatusCode
    Write-Host ("  - HTTP status: " + $statusCode)
  } catch {
  }
  Write-Host ("  - Error: " + $ErrorRecord.Exception.Message)
}

function Invoke-JsonRequest {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Uri,
    [hashtable]$Headers = @{},
    $Body = $null
  )

  try {
    if ($null -eq $Body) {
      return Invoke-RestMethod -Method $Method -Uri $Uri -Headers $Headers
    }
    $jsonBody = $Body | ConvertTo-Json -Depth 10
    return Invoke-RestMethod -Method $Method -Uri $Uri -Headers $Headers -ContentType "application/json" -Body $jsonBody
  } catch {
    Write-Host ("[FAIL] " + $Method + " " + $Uri)
    Write-ProblemDetails -ErrorRecord $_
    throw
  }
}

function Assert-True {
  param(
    [Parameter(Mandatory = $true)][bool]$Condition,
    [Parameter(Mandatory = $true)][string]$Message
  )

  if (-not $Condition) {
    throw $Message
  }
}

function Login {
  param(
    [Parameter(Mandatory = $true)][string]$Username,
    [Parameter(Mandatory = $true)][string]$Password
  )

  $response = Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/auth/login" -Body @{
    username = $Username
    password = $Password
  }

  if ($response.challengeRequired -eq $true) {
    throw ("El usuario '" + $Username + "' requiere challenge 2FA; el smoke B011 necesita acceso directo.")
  }

  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$response.accessToken)) -Message ("Login sin accessToken para '" + $Username + "'.")
  return $response
}

Write-Host "[1/7] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
Assert-True -Condition ($health.status -eq "UP") -Message "Health check fallido."

Write-Host "[2/7] Login RECEPCION y branch dinámico..."
$recepcionLogin = Login -Username "recepcion" -Password "Recepcion123!"
$branchHeader = [string]$recepcionLogin.branch.id
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($branchHeader)) -Message "Login RECEPCION no devolvio branch.id."

$recepcionHeaders = @{
  Authorization = "Bearer $($recepcionLogin.accessToken)"
  "X-Branch-Id" = $branchHeader
}

$me = Invoke-JsonRequest -Method Get -Uri "$BaseUrl/api/v1/me" -Headers $recepcionHeaders
$branchId = [string]$me.branchId
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($branchId)) -Message "/api/v1/me no devolvio branchId."
$recepcionHeaders["X-Branch-Id"] = $branchId

$rooms = @(Invoke-JsonRequest -Method Get -Uri "$BaseUrl/api/v1/rooms" -Headers $recepcionHeaders)
Assert-True -Condition ($rooms.Count -gt 0) -Message "No existen salas demo para crear cita."
$room = $rooms[0]

$services = @(Invoke-JsonRequest -Method Get -Uri "$BaseUrl/api/v1/services?active=true" -Headers $recepcionHeaders)
Assert-True -Condition ($services.Count -gt 0) -Message "No existen servicios demo activos."
$service = $services[0]

$clientsPage = Invoke-JsonRequest -Method Get -Uri "$BaseUrl/api/v1/clients?page=0&size=20" -Headers $recepcionHeaders
$clients = @($clientsPage.content)
$client = $null
if ($clients.Count -gt 0) {
  $client = $clients[0]
} else {
  $seed = [DateTimeOffset]::Now.ToUnixTimeSeconds()
  $client = Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/clients" -Headers $recepcionHeaders -Body @{
    fullName = "Cliente Smoke B011"
    identification = "SMK-$seed"
    phone = "0991001001"
    email = "smoke.b011@example.com"
    address = "Direccion smoke"
    notes = "Creado por spr-b011.ps1"
  }
}

$pets = @(Invoke-JsonRequest -Method Get -Uri "$BaseUrl/api/v1/clients/$($client.id)/pets" -Headers $recepcionHeaders)
$pet = $null
if ($pets.Count -gt 0) {
  $pet = $pets[0]
} else {
  $seed = [DateTimeOffset]::Now.ToUnixTimeSeconds()
  $pet = Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/clients/$($client.id)/pets" -Headers $recepcionHeaders -Body @{
    internalCode = "PET-SMOKE-$seed"
    name = "Moka Smoke"
    species = "Canino"
    breed = "Mestizo"
    sex = "F"
    alerts = "Ninguna"
    history = "Creada por spr-b011.ps1"
  }
}

Write-Host "[3/7] Crear cita (RECEPCION)..."
$appointmentStartsAt = [DateTimeOffset]::Now.AddHours(2).ToString("o")
$appointment = Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/appointments" -Headers $recepcionHeaders -Body @{
  roomId = $room.id
  serviceId = $service.id
  startsAt = $appointmentStartsAt
  reason = "Smoke SPR-B011"
  notes = "Flujo core demo"
  clientId = $client.id
  petId = $pet.id
}
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$appointment.id)) -Message "No se obtuvo appointment.id."

Write-Host "[4/7] Login VETERINARIO + atender/cerrar..."
$vetLogin = Login -Username "veterinario" -Password "Veterinario123!"
$vetHeaders = @{
  Authorization = "Bearer $($vetLogin.accessToken)"
  "X-Branch-Id" = $branchId
}

Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/appointments/$($appointment.id)/confirm" -Headers $vetHeaders | Out-Null
Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/appointments/$($appointment.id)/start" -Headers $vetHeaders | Out-Null

$visit = Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/visits" -Headers $vetHeaders -Body @{
  petId = $pet.id
  serviceId = $service.id
  appointmentId = $appointment.id
  sReason = "Consulta de control demo"
  sAnamnesis = "Paciente activo, sin signos de alarma."
}
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$visit.id)) -Message "No se obtuvo visit.id."

Invoke-JsonRequest -Method Patch -Uri "$BaseUrl/api/v1/visits/$($visit.id)" -Headers $vetHeaders -Body @{
  oFindings = "Examen fisico sin hallazgos de riesgo."
  aDiagnosis = "Control preventivo"
  pTreatment = "Observacion clinica"
  pInstructions = "Control en 6 meses"
} | Out-Null

$closedVisit = Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/close" -Headers $vetHeaders
Assert-True -Condition ($closedVisit.status -eq "CLOSED") -Message "La visita no quedo en estado CLOSED."

$closedAppointment = Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/appointments/$($appointment.id)/close" -Headers $vetHeaders
Assert-True -Condition ($closedAppointment.status -eq "CLOSED") -Message "La cita no quedo en estado CLOSED."

Write-Host "[5/7] Facturar y pagar (RECEPCION)..."
$invoiceDetail = Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/visits/$($visit.id)/invoices" -Headers $recepcionHeaders -Body @{
  items = @(
    @{
      itemType = "SERVICE"
      itemId = $service.id
      description = "Servicio smoke B011"
      qty = 1
      discountAmount = 0
    }
  )
  discountAmount = 0
}

$invoiceId = [string]$invoiceDetail.invoice.id
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($invoiceId)) -Message "No se obtuvo invoice.id."

$invoiceTotal = [decimal]$invoiceDetail.invoice.total
Assert-True -Condition ($invoiceTotal -gt 0) -Message "La factura genero total no valido."

Invoke-JsonRequest -Method Post -Uri "$BaseUrl/api/v1/invoices/$invoiceId/payments" -Headers $recepcionHeaders -Body @{
  method = "CASH"
  amount = $invoiceTotal
  reference = "SPR-B011-SMOKE"
} | Out-Null

Write-Host "[6/7] Validaciones finales..."
$invoiceFinal = Invoke-JsonRequest -Method Get -Uri "$BaseUrl/api/v1/invoices/$invoiceId" -Headers $recepcionHeaders
Assert-True -Condition ($invoiceFinal.invoice.status -eq "PAID") -Message "La factura no quedo en estado PAID."

$paidTotal = [decimal]$invoiceFinal.invoice.paidTotal
Assert-True -Condition ($paidTotal -ge $invoiceTotal) -Message "paidTotal es menor al total de la factura."

Write-Host "[7/7] Smoke SPR-B011 OK"
Write-Host ("- branchId: " + $branchId)
Write-Host ("- roomId: " + $room.id)
Write-Host ("- serviceId: " + $service.id)
Write-Host ("- clientId: " + $client.id)
Write-Host ("- petId: " + $pet.id)
Write-Host ("- appointmentId: " + $appointment.id)
Write-Host ("- visitId: " + $visit.id)
Write-Host ("- invoiceId: " + $invoiceId)
