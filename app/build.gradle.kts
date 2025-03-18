plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.frigontech.networkdrive"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.frigontech.networkdrive"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        /*chaquopy {
            defaultConfig {
                pip {
                    // install flask
                    install("flask")
                    install("requests")
                    install("werkzeug")
                }
            }
        }*/
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/io.netty.versions.properties")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/license.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/notice.txt")
            excludes.add("META-INF/*.kotlin_module")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.espresso.core)
    implementation(libs.kotlin.reflect)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation Controller
    implementation("androidx.navigation:navigation-compose:2.8.8") // Use latest version
    //Google Materials & Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8") // Use the latest version
    //kotlin server
    implementation("io.ktor:ktor-server-cio:2.3.6") // Latest lightweight Ktor server
    implementation("io.ktor:ktor-serialization-jackson:2.3.6") // Jackson for JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0") // Latest Jackson module
    implementation("io.ktor:ktor-server-content-negotiation:2.3.6")// COntent Negotiator

    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-android:2.3.6") // Android-specific HTTP client
    //google's Json library
    //implementation("com.google.code.gson:gson:2.10.1")
    //SMBJ dependency
    implementation("eu.agno3.jcifs:jcifs-ng:2.1.6")
}