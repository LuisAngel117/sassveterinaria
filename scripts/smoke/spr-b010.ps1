param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Convert-Base32ToBytes {
  param([string]$Input)
  $alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
  $normalized = ($Input.ToUpper().Replace("=", "").Replace(" ", ""))
  $buffer = 0
  $bitsLeft = 0
  $bytes = New-Object System.Collections.Generic.List[byte]
  foreach ($ch in $normalized.ToCharArray()) {
    $val = $alphabet.IndexOf($ch)
    if ($val -lt 0) { throw "Invalid Base32 char: $ch" }
    $buffer = ($buffer -shl 5) -bor $val
    $bitsLeft += 5
    if ($bitsLeft -ge 8) {
      $bitsLeft -= 8
      $byte = ($buffer -shr $bitsLeft) -band 0xFF
      $bytes.Add([byte]$byte)
    }
  }
  return $bytes.ToArray()
}

function Get-TotpCode {
  param([string]$Secret)
  $secretBytes = Convert-Base32ToBytes -Input $Secret
  $counter = [int64]([DateTimeOffset]::UtcNow.ToUnixTimeSeconds() / 30)
  $counterBytes = [BitConverter]::GetBytes([int64]$counter)
  if ([BitConverter]::IsLittleEndian) { [Array]::Reverse($counterBytes) }

  $hmac = New-Object System.Security.Cryptography.HMACSHA1
  $hmac.Key = $secretBytes
  $hash = $hmac.ComputeHash($counterBytes)
  $offset = $hash[$hash.Length - 1] -band 0x0F
  $binary = (($hash[$offset] -band 0x7F) -shl 24) -bor (($hash[$offset + 1] -band 0xFF) -shl 16) -bor (($hash[$offset + 2] -band 0xFF) -shl 8) -bor ($hash[$offset + 3] -band 0xFF)
  $otp = $binary % 1000000
  return $otp.ToString("D6")
}

function Invoke-JsonPost {
  param(
    [string]$Uri,
    [hashtable]$Body,
    [hashtable]$Headers
  )
  return Invoke-RestMethod -Method Post -Uri $Uri -Body ($Body | ConvertTo-Json) -ContentType "application/json" -Headers $Headers
}

Write-Host "[1/6] Health check..."
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
if ($health.status -ne "UP") { throw "Health check failed" }

Write-Host "[2/6] Login normal (recepcion)..."
$loginRecep = Invoke-JsonPost -Uri "$BaseUrl/api/v1/auth/login" -Body @{
  username = "recepcion"
  password = "Recepcion123!"
} -Headers @{}
if ([string]::IsNullOrWhiteSpace($loginRecep.accessToken)) { throw "recepcion login did not return accessToken" }

Write-Host "[3/6] Lockout flow (veterinario wrong password x4 + locked)..."
for ($i = 1; $i -le 4; $i++) {
  try {
    Invoke-JsonPost -Uri "$BaseUrl/api/v1/auth/login" -Body @{
      username = "veterinario"
      password = "WrongPass123!"
    } -Headers @{} | Out-Null
    throw "Expected invalid credentials on attempt $i"
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode.value__ -ne 401) {
      throw "Unexpected status on failed login attempt $i: $($_.Exception.Response.StatusCode.value__)"
    }
  }
}

try {
  Invoke-JsonPost -Uri "$BaseUrl/api/v1/auth/login" -Body @{
    username = "veterinario"
    password = "Veterinario123!"
  } -Headers @{} | Out-Null
  throw "Expected LOCKED response after 4 failed attempts"
} catch {
  if (-not $_.Exception.Response -or $_.Exception.Response.StatusCode.value__ -ne 423) {
    throw "Expected 423 LOCKED, got: $($_.Exception.Response.StatusCode.value__)"
  }
}

Write-Host "[4/6] Rate limit login (unknown user flood -> 429)..."
$rlUser = "rl-spr-b010"
$rateLimited = $false
for ($i = 1; $i -le 12; $i++) {
  try {
    Invoke-JsonPost -Uri "$BaseUrl/api/v1/auth/login" -Body @{
      username = $rlUser
      password = "Nope123!"
    } -Headers @{} | Out-Null
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode.value__ -eq 429) {
      $rateLimited = $true
      break
    }
  }
}
if (-not $rateLimited) { throw "Rate limit did not return 429 for login flood." }

Write-Host "[5/6] 2FA setup/enable/challenge flow (admin)..."
$loginAdmin = Invoke-JsonPost -Uri "$BaseUrl/api/v1/auth/login" -Body @{
  username = "admin"
  password = "Admin123!"
} -Headers @{}

if ($loginAdmin.challengeRequired -eq $true) {
  Write-Host "Admin already requires 2FA challenge. Manual code entry may be needed if secret is unknown."
} else {
  if ([string]::IsNullOrWhiteSpace($loginAdmin.accessToken)) { throw "admin login did not return accessToken" }
  $adminHeaders = @{ Authorization = "Bearer $($loginAdmin.accessToken)" }

  $setup = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/2fa/setup" -Headers $adminHeaders
  if ([string]::IsNullOrWhiteSpace($setup.secret)) { throw "2FA setup did not return secret" }

  $code = Get-TotpCode -Secret $setup.secret
  Invoke-JsonPost -Uri "$BaseUrl/api/v1/auth/2fa/enable" -Body @{ code = $code } -Headers $adminHeaders | Out-Null

  $loginAdmin2 = Invoke-JsonPost -Uri "$BaseUrl/api/v1/auth/login" -Body @{
    username = "admin"
    password = "Admin123!"
  } -Headers @{}
  if ($loginAdmin2.challengeRequired -ne $true) { throw "Expected 2FA challenge after enable" }

  $code2 = Get-TotpCode -Secret $setup.secret
  $final = Invoke-JsonPost -Uri "$BaseUrl/api/v1/auth/login/2fa" -Body @{
    challengeToken = $loginAdmin2.challengeToken
    code = $code2
  } -Headers @{}
  if ([string]::IsNullOrWhiteSpace($final.accessToken)) { throw "2FA completion did not return tokens" }
}

Write-Host "[6/6] Smoke SPR-B010 completed"
Write-Host "- recepcion token acquired: OK"
Write-Host "- lockout flow: OK"
Write-Host "- rate limit login: OK"
Write-Host "- 2FA flow: attempted"
