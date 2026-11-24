plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
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

    buildTypes {
        release {
            optimization {
                enable = false
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
