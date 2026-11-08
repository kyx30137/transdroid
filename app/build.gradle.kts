import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Optional release signing; see keystore.properties.example
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val hasSigningConfig = keystoreProperties.getProperty("storeFile") != null

android {
    namespace = "org.transdroid"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.transdroid"
        minSdk = 29
        targetSdk = 36
        versionCode = 3000019
        versionName = "3.0.0-beta4-step3"
    }

    if (hasSigningConfig) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    flavorDimensions += "version"
    productFlavors {
        // Full version, distributed via transdroid.org and F-Droid
        create("full") {
            dimension = "version"
            applicationIdSuffix = ".full"
        }
        // Lite version (Transdrone), distributed via Google Play
        create("lite") {
            dimension = "version"
            applicationIdSuffix = ".lite"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Sign with the release keystore when keystore.properties provides one (CI, F-Droid);
            // otherwise fall back to the debug key so a locally built release APK installs as-is.
            signingConfig = signingConfigs.getByName(if (hasSigningConfig) "release" else "debug")
            // Reproducible builds: no VCS metadata baked into the APK
            vcsInfo.include = false
        }
    }

    // CI already runs the full lint on every push, so release builds skip their own lint pass,
    // which took several minutes per release
    lint {
        checkReleaseBuilds = false
    }

    // F-Droid requirement: no Google-signed dependency metadata blob in the APK
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Robolectric renders the real Compose UI on the JVM (see TorrentListScreenshotTest), which
    // needs the merged resources and manifest of the variant under test.
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":protocol"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.maxmind.db)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowsizeclass)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // JVM-side screenshot rendering of the real Compose UI; no emulator or KVM needed.
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Robolectric resolves its android-all runtime from Maven at test time. Allow pointing that at a
// mirror without baking one into the repo:
//   ./gradlew :app:testFullDebugUnitTest -Probolectric.repo.url=https://<mirror>/public
tasks.withType<Test>().configureEach {
    providers.gradleProperty("robolectric.repo.url").orNull?.let {
        systemProperty("robolectric.dependency.repo.url", it)
    }
}
