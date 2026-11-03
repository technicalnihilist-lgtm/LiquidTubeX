plugins {
    id("com.android.application")
}

// The package name stays in app/AndroidManifest.xml so build.sh (aapt2 link)
// keeps working unchanged. AGP 7.4 reads it from there.
android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.liquidtubex.app"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Keeps the existing no-Gradle layout: sources, resources and the web UI
    // stay exactly where build.sh expects them.
    sourceSets["main"].apply {
        manifest.srcFile("AndroidManifest.xml")
        java.srcDirs("java")
        res.srcDirs("res")
        assets.srcDirs("assets")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // NewPipeExtractor uses java.time, which needs desugaring on API 21.
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    packagingOptions {
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt"
        )
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Version comes from gradle.properties (extractorVersion).
    // If the includeBuild() block in settings.gradle.kts is enabled, Gradle
    // substitutes this module with the locally built :extractor project and the
    // version string is ignored.
    implementation("net.newpipe:extractor:${property("extractorVersion")}")
}
