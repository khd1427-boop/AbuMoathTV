import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Release signing: read from keystore.properties (local) or environment variables (CI).
// Passwords are never committed to the repository.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun signingValue(key: String, env: String): String? = keystoreProps.getProperty(key) ?: System.getenv(env)

android {
    namespace = "com.abumoath.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.abumoath.tv"
        // Xiaomi Mi TV Stick (MDZ-24-AA) runs Android TV 9 (API 28). minSdk 24 keeps wider TV compatibility.
        minSdk = 24
        targetSdk = 35
        // Every GitHub build gets a higher version number automatically, so updates install over the old app.
        versionCode = (project.findProperty("versionCodeOverride") as String?)?.toIntOrNull() ?: 1
        versionName = "1.0.0"
        resourceConfigurations += listOf("ar", "en")
    }

    // Personal-use signing key shipped with the project (keystore/abumoath.jks) so every build - on GitHub or
    // on your PC - has the SAME signature and updates install over the previous version.
    // To use your own private key instead, set ABU_KEYSTORE_PATH / ABU_KEYSTORE_PASSWORD / ABU_KEY_ALIAS / ABU_KEY_PASSWORD.
    signingConfigs {
        create("release") {
            storeFile = file(signingValue("storeFile", "ABU_KEYSTORE_PATH") ?: "${rootDir}/keystore/abumoath.jks")
            storePassword = signingValue("storePassword", "ABU_KEYSTORE_PASSWORD") ?: "abumoath2026"
            keyAlias = signingValue("keyAlias", "ABU_KEY_ALIAS") ?: "abumoath"
            keyPassword = signingValue("keyPassword", "ABU_KEY_PASSWORD") ?: "abumoath2026"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/versions/9/previous-compilation-data.bin")
    }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                if (buildType.name == "release") "AbuMoathTV.apk" else "AbuMoathTV-debug.apk"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.tv.material)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.media3.datasource.okhttp)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    testImplementation(libs.junit)
}
