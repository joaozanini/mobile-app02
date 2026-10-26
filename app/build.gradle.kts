plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "br.com.autolog"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.com.autolog"
        minSdk = 29          // API 29 (Android 10), conforme solicitado na disciplina
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Testes unitários (JVM). Robolectric permite testar o SQLite sem emulador.
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
