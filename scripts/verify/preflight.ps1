$ErrorActionPreference = 'Stop'

$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
Set-Location -Path $repoRoot

$requiredDirs = @(
  'docs',
  'docs/status',
  'docs/log',
  'docs/quality',
  'docs/traceability',
  'docs/state',
  'docs/handoff',
  'docs/decisions',
  'docs/rfcs',
  'docs/sprints',
  'scripts/verify'
)

$requiredFiles = @(
  'docs/project-lock.md',
  'AGENTS.md',
  'README.md',
  'scripts/verify/verify-docs-eof.ps1',
  'scripts/verify/preflight.ps1',
  'docs/00-indice.md',
  'docs/01-brief.md',
  'docs/02-brd.md',
  'docs/03-arquitectura.md',
  'docs/04-convenciones.md',
  'docs/05-seguridad.md',
  'docs/06-dominio-parte-a.md',
  'docs/06-dominio-parte-b.md',
  'docs/07-ux-ui-parte-a.md',
  'docs/07-ux-ui-parte-b.md',
  'docs/08-runbook.md',
  'docs/09-stage-release.md',
  'docs/10-permisos.md',
  'docs/11-entrega.md',
  'docs/changelog.md',
  'docs/status/status.md',
  'docs/log/log.md',
  'docs/quality/definition-of-ready.md',
  'docs/quality/definition-of-done.md',
  'docs/traceability/rtm.md',
  'docs/state/state.md',
  'docs/handoff/handoff-back-to-front.md',
  'docs/decisions/adr-0001-stack.md',
  'docs/decisions/adr-0002-arquitectura.md',
  'docs/decisions/adr-0003-tenancy-scoping.md',
  'docs/decisions/adr-0004-seguridad-auth.md',
  'docs/decisions/adr-0005-auditoria.md',
  'docs/decisions/adr-0006-ux-principios.md',
  'docs/decisions/adr-0007-walking-skeleton.md',
  'docs/rfcs/rfc-0001-template.md',
  'docs/sprints/spr-master-back.md',
  'docs/sprints/spr-master-front.md'
)

$hasError = $false

foreach ($dir in $requiredDirs) {
  $fullDir = Join-Path $repoRoot $dir
  if (-not (Test-Path -LiteralPath $fullDir -PathType Container)) {
    Write-Host "[FAIL] Falta carpeta: $dir" -ForegroundColor Red
    $hasError = $true
  }
}

foreach ($file in $requiredFiles) {
  $fullFile = Join-Path $repoRoot $file
  if (-not (Test-Path -LiteralPath $fullFile -PathType Leaf)) {
    Write-Host "[FAIL] Falta archivo: $file" -ForegroundColor Red
    $hasError = $true
  }
}

$verifyScript = Join-Path $PSScriptRoot 'verify-docs-eof.ps1'
& pwsh -NoProfile -File $verifyScript
if ($LASTEXITCODE -ne 0) {
  $hasError = $true
}

$statusPath = Join-Path $repoRoot 'docs/status/status.md'
$statusRaw = [System.IO.File]::ReadAllText($statusPath)
if ($statusRaw -notmatch '\|\s*T1\s*\|.*\|\s*READY_FOR_VALIDATION\s*\|') {
  Write-Host '[FAIL] docs/status/status.md no contiene fila T1 en READY_FOR_VALIDATION' -ForegroundColor Red
  $hasError = $true
}

$logPath = Join-Path $repoRoot 'docs/log/log.md'
$logRaw = [System.IO.File]::ReadAllText($logPath)
if ($logRaw -notmatch 'T1') {
  Write-Host '[FAIL] docs/log/log.md no contiene entrada para T1' -ForegroundColor Red
  $hasError = $true
}

if ($hasError) {
  Write-Host '[FAIL] Preflight con errores' -ForegroundColor Red
  exit 1
}

Write-Host '[OK] Preflight completo sin errores' -ForegroundColor Green
exit 0