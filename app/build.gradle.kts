import org.gradle.api.Project

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.compose.compiler)
}

kapt {
    correctErrorTypes = true
}

fun Project.optionalBuildValue(name: String): String? =
    providers.gradleProperty(name).orNull ?: providers.environmentVariable(name).orNull

val configuredVersionCode = project.optionalBuildValue("ANDROID_VERSION_CODE")?.toIntOrNull() ?: 1
val configuredVersionName = project.optionalBuildValue("ANDROID_VERSION_NAME") ?: "0.1.0"
val releaseDebuggable = project.optionalBuildValue("ANDROID_RELEASE_DEBUGGABLE")
    ?.trim()
    ?.equals("true", ignoreCase = true) == true

val signingStoreFilePath = project.optionalBuildValue("ANDROID_SIGNING_STORE_FILE")
val signingStorePassword = project.optionalBuildValue("ANDROID_SIGNING_STORE_PASSWORD")
val signingKeyAlias = project.optionalBuildValue("ANDROID_SIGNING_KEY_ALIAS")
val signingKeyPassword = project.optionalBuildValue("ANDROID_SIGNING_KEY_PASSWORD")
val hasStableSigning = listOf(
    signingStoreFilePath,
    signingStorePassword,
    signingKeyAlias,
    signingKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.liuyi.trainer"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.liuyi.trainer"
        minSdk = 26
        targetSdk = 36
        versionCode = configuredVersionCode
        versionName = configuredVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (hasStableSigning) {
            create("stable") {
                storeFile = file(signingStoreFilePath!!)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isDebuggable = releaseDebuggable
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasStableSigning) {
                signingConfig = signingConfigs.getByName("stable")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
