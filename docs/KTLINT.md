# ktlint - Formateo de código Kotlin

Este proyecto usa [ktlint](https://ktlint.github.io/) para mantener un estilo de código consistente en todos los archivos Kotlin.

## 🚀 Configuración inicial

### 1. Sincronizar Gradle
```bash
./gradlew build
```

### 2. Instalar hook pre-commit

El hook pre-commit verifica automáticamente el código antes de cada commit.

#### **Opción A: Con Gradle (Recomendado)**
```bash
./gradlew installGitHooks
```

#### **Opción B: Scripts manuales**

**En Windows (PowerShell):**
```powershell
.\install-hooks.ps1
```

**En Linux/Mac:**
```bash
chmod +x install-hooks.sh
./install-hooks.sh
```

> **💡 Nota:** El hook pre-commit no se instala automáticamente por razones de seguridad de Git.

## 📝 Comandos útiles

### Verificar código (sin modificar archivos)
```bash
./gradlew ktlintCheck
```

### Auto-corregir problemas de formato
```bash
./gradlew ktlintFormat
```

### Verificar solo el módulo app
```bash
./gradlew :app:ktlintCheck
```

### Auto-corregir solo el módulo app
```bash
./gradlew :app:ktlintFormat
```

### Verificar si los Git hooks están instalados
```bash
./gradlew checkGitHooksInstalled
```

## 🤔 ¿Por qué no se instala automáticamente el hook?

Por razones de **seguridad**, Git no permite que los hooks se ejecuten automáticamente desde el repositorio. Los hooks pueden ejecutar código arbitrario, por lo que requieren instalación manual explícita.

## 💡 Tips

- Si el hook pre-commit falla, puedes auto-corregir con: `./gradlew ktlintFormat`
- Para hacer commit sin verificar (no recomendado): `git commit --no-verify`
- ktlint está configurado para excluir archivos en `build/`
- El formato sigue las convenciones oficiales de Kotlin

## 🎨 Configuración

La configuración de ktlint se encuentra en `build.gradle.kts` (raíz):
- Android mode habilitado
- Ignora archivos en build/
- Falla el build si hay errores

## 📚 Recursos

- [Documentación oficial de ktlint](https://ktlint.github.io/)
- [Guía de estilo de Kotlin](https://kotlinlang.org/docs/coding-conventions.html)
- [Plugin Gradle de ktlint](https://github.com/JLLeitschuh/ktlint-gradle)
