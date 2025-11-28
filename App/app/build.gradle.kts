import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
}



android {
    namespace = "com.lau.portin"

    // 1. Load local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { reader ->
            localProperties.load(reader)
        }
    }

    compileSdk = 36 // Note: The 'version =' and 'release()' parts were likely incorrect syntax.

    defaultConfig {
        applicationId = "com.lau.portin"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 3. Create a build config field
        // The first string "API_KEY" is the name of the variable in your Java/Kotlin code.
        // The second string gets the value from local.properties.
        buildConfigField("String", "API_KEY", "\"${localProperties.getProperty("API_KEY")}\"")
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
    packagingOptions {
        pickFirst("com/tom_roush/pdfbox/resources/glyphlist/*'")
        pickFirst("com/tom_roush/pdfbox/resources/ttf/*")
    }
    // 2. Define the key in buildFeatures
    buildFeatures {
        buildConfig = true
    }
}


dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
}