plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("app.cash.sqldelight")
    id("org.jetbrains.dokka")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.renaix"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.renaix"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Google Maps API Key
        manifestPlaceholders["MAPS_API_KEY"] = "AIzaSyC5_APswRVmkJs91rK1r5Z3SpJ_MpMvCfY"
        
        vectorDrawables {
            useSupportLibrary = true
        }
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
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM (Bill of Materials)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Ktor Client
    val ktorVersion = "2.3.8"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // SQLDelight
    implementation("app.cash.sqldelight:android-driver:2.0.1")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
    
    // Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Coil (Image Loading)
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Accompanist (Permissions)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    
    // Shimmer Effect
    implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
}

// SQLDelight Configuration
sqldelight {
    databases {
        create("RenaixDatabase") {
            packageName.set("com.renaix.data.local.database")
            dialect("app.cash.sqldelight:sqlite-3-35-dialect:2.0.1")
        }
    }
}

// Dokka Configuration - Documentación automática
tasks.dokkaHtml.configure {
    outputDirectory.set(file("$projectDir/docs"))

    dokkaSourceSets {
        named("main") {
            moduleName.set("Renaix App")
            includes.from("README.md")
            sourceLink {
                localDirectory.set(file("src/main/java"))
                remoteUrl.set(java.net.URI("https://github.com/tu-usuario/renaix/tree/main/app/src/main/java").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}
