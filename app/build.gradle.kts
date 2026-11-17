import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun localOrEnv(vararg names: String, default: String = ""): String {
    names.forEach { name ->
        localProperties.getProperty(name)?.let { return it }
        System.getenv(name)?.let { return it }
    }
    return default
}

fun quote(value: String): String {
    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$escaped\""
}

android {
    namespace = "com.androidengineers.agent_quickstart_android"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.androidengineers.agent_quickstart_android"
        providers.gradleProperty("droidbuddy.abi").orNull?.let { ndk { abiFilters += it } }
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.0"

        // Developer-owned credentials for a private demo APK. Never commit local.properties.
        buildConfigField("String", "AGORA_APP_ID", quote(localOrEnv("AGORA_APP_ID")))
        buildConfigField("String", "AGORA_APP_CERTIFICATE", quote(localOrEnv("AGORA_APP_CERTIFICATE")))
        buildConfigField("String", "MURF_API_KEY", quote(localOrEnv("MURF_API_KEY")))
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    packaging {
        jniLibs {
            pickFirsts += listOf(
                "**/libaosl.so",
                "**/libagora_rtc_sdk.so",
                "**/libagora_core.so",
                "**/libagora_fdkaac.so",
                "**/libagora_soundtouch.so"
            )
        }
    }
}

dependencies {
    implementation(libs.agora.authentication)
    implementation(libs.okhttp)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.coil.compose)
    implementation(libs.gson)
    implementation(libs.agora.rtc)
    implementation(libs.agora.rtm)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
