// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.google.mobile.services) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}

// Tarea para instalar Git hooks automáticamente
tasks.register("installGitHooks") {
    group = "git hooks"
    description = "Instala los Git hooks desde .githooks/"

    doLast {
        val hooksDir = File(rootDir, ".githooks")
        val gitHooksDir = File(rootDir, ".git/hooks")

        if (!File(rootDir, ".git").exists()) {
            logger.warn("⚠️  No es un repositorio Git. Saltando instalación de hooks.")
            return@doLast
        }

        if (!hooksDir.exists()) {
            logger.error("❌ Directorio .githooks no encontrado")
            return@doLast
        }

        gitHooksDir.mkdirs()

        var installed = 0
        hooksDir.listFiles()?.forEach { hookFile ->
            if (hookFile.isFile) {
                val targetFile = File(gitHooksDir, hookFile.name)
                hookFile.copyTo(targetFile, overwrite = true)
                targetFile.setExecutable(true)
                installed++
                logger.lifecycle("✅ Hook instalado: ${hookFile.name}")
            }
        }

        if (installed > 0) {
            logger.lifecycle("🎉 Se instalaron $installed hook(s) exitosamente!")
            logger.lifecycle("   Los archivos se verificarán automáticamente antes de cada commit")
        }
    }
}

// Tarea para verificar si los hooks están instalados
tasks.register("checkGitHooksInstalled") {
    group = "git hooks"
    description = "Verifica si los Git hooks están instalados"

    doLast {
        val preCommitHook = File(rootDir, ".git/hooks/pre-commit")
        if (preCommitHook.exists()) {
            logger.lifecycle("✅ Los Git hooks están instalados correctamente")
        } else {
            logger.warn("⚠️  Los Git hooks NO están instalados")
            logger.warn("   Ejecuta: ./gradlew installGitHooks")
        }
    }
}
