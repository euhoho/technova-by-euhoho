<#
Remove `application.properties` from git history using git-filter-repo.
Run from repository root:
  powershell -ExecutionPolicy Bypass -File .\scripts\remove-application-properties.ps1

This script will:
 - create backups (bundle + mirror)
 - try to install git-filter-repo if missing (via pip)
 - run git-filter-repo to remove matching files
 - run GC and force-push the rewritten history

WARNING: This rewrites history. Make a backup and inform collaborators.
#>

param(
  [switch]$AutoConfirm
)

function Abort($msg){ Write-Host $msg -ForegroundColor Red; exit 1 }

if (-not (Get-Command git -ErrorAction SilentlyContinue)) { Abort "git no encontrado. Instala git y vuelve a intentar." }

$root = (git rev-parse --show-toplevel) 2>$null
if (-not $?) { Abort "No estás en un repositorio git." }
Set-Location $root

if ((git status --porcelain) -ne "") {
  Write-Host "Hay cambios no commitados. Haz stash o commit antes de continuar." -ForegroundColor Yellow
  if (-not $AutoConfirm) { Abort "Abortado: limpia el working tree y vuelve a ejecutar." }
}

Write-Host "Creando backup bundle..." -ForegroundColor Cyan
git bundle create "../technova-backup.bundle" --all
Write-Host "Creando mirror bare backup..." -ForegroundColor Cyan
git clone --mirror . "../technova-mirror-backup.git"

if (-not (Get-Command git-filter-repo -ErrorAction SilentlyContinue)) {
  Write-Host "git-filter-repo no encontrado. Intentando instalar con pip..." -ForegroundColor Yellow
  if (Get-Command pip -ErrorAction SilentlyContinue) {
    pip install --user git-filter-repo
    if (-not (Get-Command git-filter-repo -ErrorAction SilentlyContinue)) {
      Write-Host "Instalación completada pero git-filter-repo no está en PATH. Asegúrate de reiniciar la sesión o añade la ruta de Scripts de Python a PATH." -ForegroundColor Yellow
    }
  } else {
    Write-Host "No se pudo encontrar pip. Instala git-filter-repo manualmente o usa BFG." -ForegroundColor Red
    if (-not $AutoConfirm) { Abort "Instala git-filter-repo y vuelve a ejecutar." }
  }
}

Write-Host "Se eliminarán los siguientes patrones del historial:" -ForegroundColor Cyan
Write-Host " - **/application.properties"
Write-Host " - backend/src/main/resources/application.properties"
Write-Host " - backend/target/classes/application.properties"
if (-not $AutoConfirm) {
  $ok = Read-Host "Continuar? (y/N)"
  if ($ok -ne 'y' -and $ok -ne 'Y') { Abort "Abortado por el usuario." }
}

# Ejecutar git-filter-repo
git filter-repo --invert-paths --path-glob '**/application.properties' --path-glob 'backend/src/main/resources/application.properties' --path-glob 'backend/target/classes/application.properties'

# Limpieza
git reflog expire --expire=now --all
git gc --prune=now --aggressive

Write-Host "Historial purgado localmente. Ahora se hará push forzado al remoto origin." -ForegroundColor Yellow
if (-not $AutoConfirm) {
  $ok = Read-Host "Hacer push --force --all y --force --tags? (y/N)"
  if ($ok -ne 'y' -and $ok -ne 'Y') { Write-Host "Push cancelado. Revisa el repo y empuja cuando estés listo."; exit 0 }
}

git push --force --all
git push --force --tags

Write-Host "Finalizado. Indica a los colaboradores que reclonen o reseteen." -ForegroundColor Green
Write-Host "Instrucciones para colaboradores:"
Write-Host "  git fetch origin; git reset --hard origin/main; git clean -fd"
Write-Host "(Sustituye 'main' por la rama principal de tu repo si es otra.)"
