$ErrorActionPreference = 'Stop'

$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$docsPath = Join-Path $repoRoot 'docs'

if (-not (Test-Path -LiteralPath $docsPath)) {
  Write-Error "No existe la carpeta docs en: $docsPath"
  exit 1
}

$docsFiles = Get-ChildItem -Path $docsPath -Recurse -File -Filter '*.md' | Sort-Object FullName

if ($docsFiles.Count -eq 0) {
  Write-Error 'No se encontraron archivos .md bajo docs/**'
  exit 1
}

$invalid = @()

foreach ($file in $docsFiles) {
  $raw = [System.IO.File]::ReadAllText($file.FullName)
  if ($raw -notmatch '<!-- EOF -->\r?\n?$') {
    $invalid += $file.FullName
  }
}

if ($invalid.Count -gt 0) {
  Write-Host '[FAIL] Archivos sin terminador EOF exacto:' -ForegroundColor Red
  foreach ($path in $invalid) {
    Write-Host " - $path"
  }
  exit 1
}

Write-Host "[OK] $($docsFiles.Count) archivos markdown en docs/** terminan con <!-- EOF -->" -ForegroundColor Green
exit 0