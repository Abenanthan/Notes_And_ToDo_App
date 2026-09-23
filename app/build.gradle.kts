plugins {
    // AGP 9 compiles Kotlin itself, so there is no separate kotlin-android plugin.
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.notestodo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.notestodo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // Generates BuildConfig, so the Settings screen can show the version name.
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Room writes a JSON snapshot of each database version here. Commit this folder:
// later phases use it to generate migrations automatically (AutoMigration).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Notifications (NotificationCompat) and permission checks
    implementation(libs.androidx.core.ktx)

    // Stores the theme settings
    implementation(libs.androidx.datastore.preferences)

    // Home-screen widget, built with composables instead of RemoteViews
    implementation(libs.androidx.glance.appwidget)

    // Rich text editing. rc11 is the last build against Compose 1.7; newer releases pull
    // in Compose 1.11+ and would drag the whole app's Compose version up with them.
    implementation(libs.rich.editor)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    // Formatting and attachment icons; R8 strips the unused ones from release builds.
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ViewModel + lifecycle-aware Flow collection
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
