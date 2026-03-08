#!/bin/bash
# Script para instalar los git hooks

HOOKS_DIR=".githooks"
GIT_HOOKS_DIR=".git/hooks"

# Verificar que estamos en la raíz del proyecto
if [ ! -d ".git" ]; then
    echo "❌ Error: Este script debe ejecutarse desde la raíz del repositorio git"
    exit 1
fi

# Crear directorio de hooks si no existe
mkdir -p "$GIT_HOOKS_DIR"

# Copiar hooks
echo "📋 Instalando git hooks..."
if [ -d "$HOOKS_DIR" ]; then
    for hook in "$HOOKS_DIR"/*; do
        hook_name=$(basename "$hook")
        cp "$hook" "$GIT_HOOKS_DIR/$hook_name"
        chmod +x "$GIT_HOOKS_DIR/$hook_name"
        echo "✅ Hook instalado: $hook_name"
    done
    echo ""
    echo "🎉 ¡Git hooks instalados exitosamente!"
    echo "   Los archivos se verificarán automáticamente antes de cada commit"
else
    echo "❌ Error: Directorio $HOOKS_DIR no encontrado"
    exit 1
fi
