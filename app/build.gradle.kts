import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.compose.screenshot)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.quran.tathbeet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.quran.tathbeet"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

val coverageExclusions = listOf(
    "**/R.class",
    "**/R${'$'}*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/*Preview*.*",
    "**/*ComposableSingletons*.*",
    "**/*${'$'}Lambda${'$'}*.*",
    "**/*${'$'}inlined${'$'}*.*",
    "**/*_Factory.class",
    "**/*_Provide*Factory.class",
    "**/*_Impl.class",
    "**/*_Impl${'$'}*.class",
)

val debugCoverageSources = files(
    "src/main/java",
    "src/main/kotlin",
)

val debugCoverageClasses = files(
    layout.buildDirectory.dir("tmp/kotlin-classes/debug"),
    layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes"),
).asFileTree.matching {
    exclude(coverageExclusions)
}

val debugUnitCoverageData = fileTree(layout.buildDirectory) {
    include(
        "jacoco/testDebugUnitTest.exec",
        "outputs/unit_test_code_coverage/debugUnitTest/**/*.exec",
    )
}

val debugUiCoverageData = fileTree(layout.buildDirectory) {
    include("outputs/code_coverage/debugAndroidTest/connected/**/*.ec")
}

fun JacocoReport.configureDebugCoverageReport() {
    group = "verification"
    description = "Generates a JaCoCo coverage report for the debug variant."

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }

    sourceDirectories.setFrom(debugCoverageSources)
    classDirectories.setFrom(debugCoverageClasses)
}

tasks.register<JacocoReport>("jacocoDebugUnitTestReport") {
    dependsOn("testDebugUnitTest")
    configureDebugCoverageReport()
    description = "Generates a JaCoCo coverage report for debug unit tests."
    executionData.setFrom(debugUnitCoverageData)
}

tasks.register<JacocoReport>("jacocoDebugUiTestReport") {
    dependsOn("connectedDebugAndroidTest")
    configureDebugCoverageReport()
    description = "Generates a JaCoCo coverage report for debug instrumentation tests."
    executionData.setFrom(debugUiCoverageData)
}

tasks.register<JacocoReport>("jacocoDebugCombinedReport") {
    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")
    configureDebugCoverageReport()
    description = "Generates a JaCoCo coverage report for debug unit and instrumentation tests."
    executionData.setFrom(debugUnitCoverageData, debugUiCoverageData)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.room.testing)

    screenshotTestImplementation(platform(libs.androidx.compose.bom))
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling.preview)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
}
