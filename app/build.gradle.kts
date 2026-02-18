import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

val localProperties = gradleLocalProperties(rootDir, providers)

fun resolveBuildSecret(name: String): String? =
    providers.gradleProperty(name).orNull
        ?: System.getenv(name)
        ?: localProperties.getProperty(name)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.google.mobile.services)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.brios.miempresa"
    compileSdk = 36

    val releaseStoreFilePath = resolveBuildSecret("RELEASE_STORE_FILE")?.trim().orEmpty()
    val releaseStorePassword = resolveBuildSecret("RELEASE_STORE_PASSWORD")?.trim().orEmpty()
    val releaseKeyAlias = resolveBuildSecret("RELEASE_KEY_ALIAS")?.trim().orEmpty()
    val releaseKeyPassword = resolveBuildSecret("RELEASE_KEY_PASSWORD")?.trim().orEmpty()
    val hasReleaseSigningConfig =
        releaseStoreFilePath.isNotBlank() &&
            releaseStorePassword.isNotBlank() &&
            releaseKeyAlias.isNotBlank() &&
            releaseKeyPassword.isNotBlank()
    val isReleaseTaskRequested =
        project.gradle.startParameter.taskNames
            .joinToString(separator = " ")
            .contains("release", ignoreCase = true)

    signingConfigs {
        create("UnifiedDebugKeystore") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("debug.keystore")
            storePassword = "android"
        }

        create("release") {
            if (!hasReleaseSigningConfig && isReleaseTaskRequested) {
                throw GradleException(
                    "Missing release signing config. Define RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS and RELEASE_KEY_PASSWORD " +
                        "via local.properties, environment variables, or -P Gradle properties.",
                )
            }

            if (hasReleaseSigningConfig) {
                storeFile = rootProject.file(releaseStoreFilePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    defaultConfig {
        applicationId = "com.brios.miempresa"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "2.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val webClientId: String = localProperties.getProperty("WEB_CLIENT_ID") ?: ""
        val sheetsApiKey: String =
            localProperties
                .getProperty("SHEETS_API_KEY")
                ?.trim()
                ?.removeSurrounding("\"")
                ?: ""
        val syncPeriodMinutes: Long = localProperties.getProperty("SYNC_PERIOD_MINUTES")?.toLong() ?: 15
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
        buildConfigField("String", "SHEETS_API_KEY", "\"$sheetsApiKey\"")
        buildConfigField("long", "SYNC_PERIOD_MINUTES", "$syncPeriodMinutes")
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/LICENSE.txt"
        }
    }
}

dependencies {
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.sheets)
    implementation(libs.google.api.services.drive)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.googleid)
    implementation(libs.google.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.compose.foundation)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.coil.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.material)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.emoji2.emojipicker)

    // ZXing for QR code generation (core only - no camera/scanning)
    implementation(libs.zxing.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

val releaseApkFileName = "miempresa-app-release-v${android.defaultConfig.versionName}(${android.defaultConfig.versionCode}).apk"

val renameReleaseApk by tasks.registering(Copy::class) {
    from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
    into(layout.buildDirectory.dir("outputs/apk/release"))
    rename("app-release.apk", releaseApkFileName)
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    finalizedBy(renameReleaseApk)
}
