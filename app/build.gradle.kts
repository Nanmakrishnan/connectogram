plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.connectogram"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.example.connectogram"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


}



dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.squareup.picasso:picasso:2.8")


    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))

    implementation ("commons-io:commons-io:2.11.0")
    //pdfview to
    //show the pf
    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // implementation ("com.google.firebase:firebase-messaging:22.0.0")
    implementation("com.google.firebase:firebase-messaging:20.0.0")
    implementation("com.google.firebase:firebase-core:19.0.0")
    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.mikhaellopez:circularimageview:4.3.1")
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // For control over item selection of both touch and mouse driven selection

        implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.burhanrashid52:photoeditor:3.0.2")


    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("com.google.android.gms:play-services-measurement-sdk-api:21.0.0")
//    implementation("com.google.android.gms:play-services-measurement-base:21.5.1") {
//        exclude(group = "com.google.firebase", module = "firebase-analytics")
//    }






}