plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "rmit.aad.cleanup"
    compileSdk = 33

    defaultConfig {
        applicationId = "rmit.aad.cleanup"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildToolsVersion = "33.0.0"

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    /* Firebase  */
    implementation(platform("com.google.firebase:firebase-bom:32.6.0")) // BoM for the Firebase platform
    implementation("com.google.firebase:firebase-database") // Dependency for the Realtime Database library
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")


    /* Jackson */
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.10.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.10.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.4")

    /* GoogleMap */
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:16.0.0")
//    implementation("com.google.android.gms:play-services:11.0.8")

    /* */
    implementation("com.github.ramseth001:TextDrawable:1.1.3")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")
    implementation("com.flaviofaria:kenburnsview:1.0.7")
    implementation("de.hdodenhof:circleimageview:2.2.0")
    implementation("com.mikhaellopez:circularimageview:4.3.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}