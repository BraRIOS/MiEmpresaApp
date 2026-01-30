# Script para instalar los git hooks en Windows

$HooksDir = ".githooks"
$GitHooksDir = ".git\hooks"

# Verificar que estamos en la raiz del proyecto
if (-not (Test-Path ".git")) {
    Write-Host "Error: Este script debe ejecutarse desde la raiz del repositorio git" -ForegroundColor Red
    exit 1
}

# Crear directorio de hooks si no existe
if (-not (Test-Path $GitHooksDir)) {
    New-Item -ItemType Directory -Path $GitHooksDir | Out-Null
}

# Copiar hooks
Write-Host "Instalando git hooks..." -ForegroundColor Cyan
if (Test-Path $HooksDir) {
    Get-ChildItem -Path $HooksDir | ForEach-Object {
        $hookName = $_.Name
        Copy-Item -Path $_.FullName -Destination "$GitHooksDir\$hookName" -Force
        Write-Host "Hook instalado: $hookName" -ForegroundColor Green
    }
    Write-Host ""
    Write-Host "Git hooks instalados exitosamente!" -ForegroundColor Green
    Write-Host "Los archivos se verificaran automaticamente antes de cada commit" -ForegroundColor Yellow
} else {
    Write-Host "Error: Directorio $HooksDir no encontrado" -ForegroundColor Red
    exit 1
}
