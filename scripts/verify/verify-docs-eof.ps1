# Verifica que TODOS los .md bajo docs/** terminan EXACTO con "<!-- EOF -->"
# Linux-friendly: evita truncado accidental y fuerza consistencia.
# Exit 0 = OK, Exit 1 = FAIL

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

param(
  [Parameter(Mandatory = $false)]
  [string] $DocsRoot = "docs"
)

if (-not (Test-Path -Path $DocsRoot)) {
  Write-Host "FAIL: No existe la carpeta '$DocsRoot'."
  exit 1
}

$mdFiles = Get-ChildItem -Path $DocsRoot -Recurse -File -Filter "*.md" | Sort-Object FullName
if ($mdFiles.Count -eq 0) {
  Write-Host "FAIL: No se encontraron archivos .md bajo '$DocsRoot'."
  exit 1
}

$bad = @()

foreach ($f in $mdFiles) {
  $lines = Get-Content -Path $f.FullName -ErrorAction Stop
  $lastNonEmpty = $null

  foreach ($line in ($lines | ForEach-Object { $_.TrimEnd() })) {
    if ($line -ne "") { $lastNonEmpty = $line }
  }

  if ($null -eq $lastNonEmpty) {
    $bad += $f.FullName
    continue
  }

  if ($lastNonEmpty -ne "<!-- EOF -->") {
    $bad += $f.FullName
  }
}

if ($bad.Count -gt 0) {
  Write-Host "FAIL: Los siguientes archivos NO terminan con '<!-- EOF -->':"
  $bad | ForEach-Object { Write-Host " - $_" }
  exit 1
}

Write-Host "OK: Todos los docs .md bajo '$DocsRoot' terminan con '<!-- EOF -->'."
exit 0
