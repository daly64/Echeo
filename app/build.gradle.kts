plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("app.cash.paparazzi")
}

// Numéro de version : augmente à chaque compilation GitHub, pour que chaque APK
// s'installe par-dessus le précédent sans perte de données.
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

// Clé de signature : la clé privée (secrets GitHub) si elle existe,
// sinon la clé de développement du dépôt. Elle doit rester la même d'une version à l'autre.
val releaseKeystore = System.getenv("ECHEO_KEYSTORE_PATH")
val releasePassword = System.getenv("ECHEO_KEYSTORE_PASSWORD")

android {
    namespace = "com.echeo.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.echeo.app"
        minSdk = 26
        targetSdk = 34
        versionCode = buildNumber
        versionName = "0.1.$buildNumber"
    }

    signingConfigs {
        create("echeo") {
            if (!releaseKeystore.isNullOrBlank() && !releasePassword.isNullOrBlank()) {
                storeFile = file(releaseKeystore)
                storePassword = releasePassword
                keyAlias = "echeo"
                keyPassword = releasePassword
            } else {
                storeFile = rootProject.file("keystore/echeo-dev.jks")
                storePassword = "echeo-dev"
                keyAlias = "echeo"
                keyPassword = "echeo-dev"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("echeo")
        }
        debug {
            signingConfig = signingConfigs.getByName("echeo")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    // Schéma de la base exporté pour les migrations futures (mises à jour sans perte de données).
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    val room = "2.6.1"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    testImplementation("junit:junit:4.13.2")
}
