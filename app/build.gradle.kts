import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

// Datos de la firma: keystore.properties en la raíz del proyecto (copiar keystore.properties.ejemplo)
val keystoreArchivo = rootProject.file("keystore.properties")
val keystoreDatos = Properties()
if (keystoreArchivo.exists()) {
    keystoreArchivo.inputStream().use { keystoreDatos.load(it) }
}

android {
    namespace = "cl.duoc.bitacora_escolar"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "cl.duoc.bitacora_escolar"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma del APK (guía "Firma y versionado de una APK"):
    // las contraseñas NO van aquí, se leen de keystore.properties (que está fuera de Git).
    signingConfigs {
        create("release") {
            if (keystoreArchivo.exists()) {
                storeFile = rootProject.file(keystoreDatos.getProperty("storeFile"))
                storePassword = keystoreDatos.getProperty("storePassword")
                keyAlias = keystoreDatos.getProperty("keyAlias")
                keyPassword = keystoreDatos.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // Sin keystore.properties el APK release se genera sin firmar
            if (keystoreArchivo.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.coil.compose)

    // Retrofit + Gson (microservicio y API externa)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Pruebas unitarias de los ViewModel (Dispatchers.setMain y runTest)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Kover: reporte de cobertura de las pruebas unitarias
// Ejecutar: ./gradlew :app:koverHtmlReportDebug
// Se mide la LÓGICA de la app: validaciones, utilidades, sesión, repositorios y ViewModels
// (las pantallas de Compose, Room y Retrofit se prueban ejecutando la app).
kover {
    reports {
        filters {
            includes {
                classes(
                    "cl.duoc.bitacora_escolar.util.ValidacionesKt",
                    "cl.duoc.bitacora_escolar.util.FechasKt",
                    "cl.duoc.bitacora_escolar.util.IndicadoresKt",
                    "cl.duoc.bitacora_escolar.data.Sesion",
                    "cl.duoc.bitacora_escolar.data.*Repository*",
                    "cl.duoc.bitacora_escolar.ui.*ViewModel*"
                )
            }
        }
    }
}

// APK con nombre versionado: BitacoraEscolar-v1.0-build1.apk
// Ejecutar: .\gradlew :app:exportarApkRelease  → queda en app/build/apk-exportadas/
val app = "BitacoraEscolar"
val version = android.defaultConfig.versionName ?: "1.0"
val build = android.defaultConfig.versionCode ?: 1

tasks.register<Copy>("exportarApkRelease") {
    // Esta tarea solo copia y renombra el APK: se marca para que Gradle no la guarde en su caché de configuración
    notCompatibleWithConfigurationCache("Copia y renombra el APK release")
    val nombreApk = "$app-v$version-build$build.apk"
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("app-release.apk")
        rename("app-release.apk", nombreApk)
    }
    into(layout.buildDirectory.dir("apk-exportadas"))
}