import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.firebase.crashlitycs)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "by.klnvch.link5dots"
    compileSdk = 36

    signingConfigs {
        create("debugConfig") {
            storeFile = file(keystoreProperties["debugStoreFile"] as String)
            keyAlias = keystoreProperties["debugKeyAlias"] as String
            keyPassword = keystoreProperties["debugKeyPassword"] as String
            storePassword = keystoreProperties["debugStorePassword"] as String
        }
        create("releaseConfig") {
            storeFile = file(keystoreProperties["releaseStoreFile"] as String)
            keyAlias = keystoreProperties["releaseKeyAlias"] as String
            keyPassword = keystoreProperties["releaseKeyPassword"] as String
            storePassword = keystoreProperties["releaseStorePassword"] as String
        }
    }
    defaultConfig {
        applicationId = "by.klnvch.link5dots"
        minSdk = 23
        targetSdk = 36
        versionCode = 48
        versionName = "5.0.0"

        buildConfigField("long", "BUILD_TIME", System.currentTimeMillis().toString() + "L")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    androidResources {
        localeFilters += listOf("en", "be", "de", "es", "fr", "it", "pl", "uk", "fa")
    }
    sourceSets {
        /* androidTest.assets.srcDirs += files("$projectDir/schemas".toString()) */
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs["releaseConfig"]
        }
        debug {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs["debugConfig"]
        }
    }
    compileOptions {
        encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles.addAll(
        project.layout.projectDirectory.file("configuration-file.conf"),
    )
}

dependencies {
    implementation(platform(libs.androidx.compose))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.ui.preview)
    implementation(project(":shared"))
    implementation(libs.androidx.compose.adaptive)
    debugImplementation(libs.androidx.compose.ui)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.dagger)
    implementation(libs.dagger.android)
    implementation(libs.dagger.android.support)
    ksp(libs.dagger.compiler)
    ksp(libs.dagger.android.processor)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.database)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.rules)
}
