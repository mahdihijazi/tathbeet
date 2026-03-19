import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.compose.screenshot)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    jacoco
}

fun placeholderBuildValue(
    key: String,
    fallback: String,
): String =
    providers.gradleProperty(key)
        .orElse(providers.environmentVariable(key))
        .orElse(fallback)
        .get()

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

        buildConfigField(
            "String",
            "FIREBASE_API_KEY",
            "\"${placeholderBuildValue("FIREBASE_API_KEY", "TODO_FIREBASE_API_KEY")}\"",
        )
        buildConfigField(
            "String",
            "FIREBASE_APPLICATION_ID",
            "\"${placeholderBuildValue("FIREBASE_APPLICATION_ID", "TODO_FIREBASE_APPLICATION_ID")}\"",
        )
        buildConfigField(
            "String",
            "FIREBASE_PROJECT_ID",
            "\"${placeholderBuildValue("FIREBASE_PROJECT_ID", "TODO_FIREBASE_PROJECT_ID")}\"",
        )
        buildConfigField(
            "String",
            "FIREBASE_STORAGE_BUCKET",
            "\"${placeholderBuildValue("FIREBASE_STORAGE_BUCKET", "TODO_FIREBASE_STORAGE_BUCKET")}\"",
        )
        buildConfigField(
            "String",
            "FIREBASE_AUTH_DOMAIN",
            "\"${placeholderBuildValue("FIREBASE_AUTH_DOMAIN", "tathbeet-b40d5.firebaseapp.com")}\"",
        )
        buildConfigField(
            "String",
            "FIREBASE_AUTH_HOST",
            "\"${placeholderBuildValue("FIREBASE_AUTH_HOST", "tathbeet-b40d5.firebaseapp.com")}\"",
        )
        buildConfigField(
            "String",
            "FIREBASE_ANDROID_PACKAGE_NAME",
            "\"${placeholderBuildValue("FIREBASE_ANDROID_PACKAGE_NAME", "com.quran.tathbeet")}\"",
        )
        buildConfigField(
            "String",
            "FIREBASE_EMAIL_LINK_URL",
            "\"${placeholderBuildValue("FIREBASE_EMAIL_LINK_URL", "https://tathbeet-b40d5.firebaseapp.com/finishSignIn/")}\"",
        )

        manifestPlaceholders["firebaseAuthHost"] =
            placeholderBuildValue("FIREBASE_AUTH_HOST", "tathbeet-b40d5.firebaseapp.com")
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
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
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
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
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
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
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

private data class ScreenshotCoverageTarget(
    val group: String,
    val id: String,
) {
    val key: String = "$group:$id"
}

private data class ScreenshotCoverageObservation(
    val target: ScreenshotCoverageTarget,
    val filePath: String,
    val functionName: String,
)

abstract class ScreenshotCoverageReportTask : DefaultTask() {
    @get:InputDirectory
    abstract val screenshotSourceDirectory: DirectoryProperty

    @get:InputFile
    abstract val targetManifest: RegularFileProperty

    @get:OutputDirectory
    abstract val reportDirectory: DirectoryProperty

    @TaskAction
    fun generateReport() {
        val expectedTargets = parseTargetManifest(targetManifest.get().asFile)
        val actualTargets = parseScreenshotTests(screenshotSourceDirectory.get().asFile)

        val expectedKeys = expectedTargets.map { it.key }.toSet()
        val actualKeys = actualTargets.map { it.target.key }.toSet()
        val coveredCount = expectedKeys.intersect(actualKeys).size
        val missingTargets = expectedTargets.filter { it.key !in actualKeys }
        val unexpectedTargets = actualTargets.filter { it.target.key !in expectedKeys }
        val duplicateTargets = actualTargets.groupBy { it.target.key }
            .filterValues { observations -> observations.size > 1 }
            .toSortedMap()

        val byGroup = expectedTargets.groupBy { it.group }
            .toSortedMap()
            .map { (group, targets) ->
                val coveredInGroup = targets.count { it.key in actualKeys }
                "$group: $coveredInGroup/${targets.size} (${formatPercent(coveredInGroup, targets.size)})"
            }

        val report = buildString {
            appendLine("Screenshot coverage report")
            appendLine()
            appendLine("Expected targets: ${expectedTargets.size}")
            appendLine("Covered targets: $coveredCount")
            appendLine("Coverage: ${formatPercent(coveredCount, expectedTargets.size)}")
            appendLine()
            appendLine("By group:")
            byGroup.forEach { summary -> appendLine("- $summary") }
            appendLine()
            appendLine("Missing targets:")
            if (missingTargets.isEmpty()) {
                appendLine("- none")
            } else {
                missingTargets.forEach { target -> appendLine("- ${target.key}") }
            }
            appendLine()
            appendLine("Unexpected targets:")
            if (unexpectedTargets.isEmpty()) {
                appendLine("- none")
            } else {
                unexpectedTargets
                    .sortedWith(compareBy({ it.target.group }, { it.target.id }))
                    .forEach { observation ->
                        appendLine("- ${observation.target.key} (${observation.filePath}::${observation.functionName})")
                    }
            }
            appendLine()
            appendLine("Duplicate targets:")
            if (duplicateTargets.isEmpty()) {
                appendLine("- none")
            } else {
                duplicateTargets.forEach { (targetKey, observations) ->
                    appendLine("- $targetKey")
                    observations.forEach { observation ->
                        appendLine("  ${observation.filePath}::${observation.functionName}")
                    }
                }
            }
        }

        val outputDirectory = reportDirectory.get().asFile
        val htmlReportFile = outputDirectory.resolve("index.html")
        outputDirectory.mkdirs()
        outputDirectory.resolve("report.txt").delete()
        htmlReportFile.writeText(
            """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="utf-8"><title>Screenshot Coverage</title><style>body{font-family:ui-monospace,SFMono-Regular,Menlo,monospace;background:#f6f4ef;color:#1f1f1f;margin:0;padding:32px}main{max-width:960px;margin:0 auto}pre{white-space:pre-wrap;background:#fff;border:1px solid #d8d1c4;border-radius:12px;padding:24px;line-height:1.5;overflow-wrap:anywhere}</style></head>
            <body><main><h1>Screenshot Coverage Report</h1><pre>${escapeHtml(report)}</pre></main></body>
            </html>
            """.trimIndent(),
        )
        logger.lifecycle(report)
        logger.lifecycle("Saved screenshot coverage HTML report to ${htmlReportFile.invariantSeparatorsPath}")
    }

    private fun parseTargetManifest(file: File): List<ScreenshotCoverageTarget> {
        return file.readLines()
            .mapIndexedNotNull { index, rawLine ->
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#")) {
                    null
                } else {
                    val parts = line.split(":", limit = 2)
                    if (parts.size != 2 || parts.any { it.isBlank() }) {
                        throw InvalidUserDataException(
                            "Invalid screenshot target on line ${index + 1} in ${file.path}: $rawLine",
                        )
                    }
                    ScreenshotCoverageTarget(
                        group = parts[0].trim(),
                        id = parts[1].trim(),
                    )
                }
            }
            .also { targets ->
                val duplicates = targets.groupBy { it.key }
                    .filterValues { entries -> entries.size > 1 }
                    .keys
                if (duplicates.isNotEmpty()) {
                    throw InvalidUserDataException(
                        "Duplicate screenshot targets in ${file.path}: ${duplicates.sorted().joinToString()}",
                    )
                }
            }
    }

    private fun parseScreenshotTests(sourceRoot: File): List<ScreenshotCoverageObservation> {
        val previewBlockPattern = Regex(
            pattern = """@PreviewTest\s*@Preview\((.*?)\)\s*@Composable\s*fun\s+([A-Za-z0-9_]+)\s*\(""",
            options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE),
        )
        val previewNamePattern = Regex("""name\s*=\s*"([^"]+)"""")

        return sourceRoot.walkTopDown()
            .filter { file -> file.isFile && file.extension == "kt" }
            .sortedBy { file -> file.invariantSeparatorsPath }
            .flatMap { file ->
                val relativePath = file.relativeTo(sourceRoot).invariantSeparatorsPath
                val group = inferScreenshotGroup(relativePath)
                previewBlockPattern.findAll(file.readText()).map { match ->
                    val previewBlock = match.groupValues[1]
                    val functionName = match.groupValues[2]
                    val previewName = previewNamePattern.find(previewBlock)?.groupValues?.get(1)
                        ?: throw InvalidUserDataException(
                            "Missing @Preview(name = ...) for screenshot test $functionName in $relativePath",
                        )

                    ScreenshotCoverageObservation(
                        target = ScreenshotCoverageTarget(
                            group = group,
                            id = previewName,
                        ),
                        filePath = relativePath,
                        functionName = functionName,
                    )
                }
            }
            .toList()
    }

    private fun inferScreenshotGroup(relativePath: String): String {
        val segments = relativePath.split("/")
        val uiIndex = segments.indexOf("ui")
        if (uiIndex == -1) {
            return File(relativePath).parentFile?.name ?: "unknown"
        }

        return when (segments.getOrNull(uiIndex + 1)) {
            "components" -> "components"
            "features" -> segments.getOrNull(uiIndex + 2) ?: "features"
            else -> File(relativePath).parentFile?.name ?: "unknown"
        }
    }

    private fun formatPercent(coveredCount: Int, totalCount: Int): String {
        if (totalCount == 0) {
            return "100.0%"
        }

        val percent = coveredCount.toDouble() / totalCount.toDouble() * 100.0
        return String.format(Locale.US, "%.1f%%", percent)
    }

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

tasks.register<ScreenshotCoverageReportTask>("reportScreenshotCoverage") {
    group = "Verification"
    description = "Generate a coverage report for screenshot component and state targets."
    screenshotSourceDirectory.set(layout.projectDirectory.dir("src/screenshotTest/kotlin"))
    targetManifest.set(layout.projectDirectory.file("src/screenshotTest/coverage/targets.txt"))
    reportDirectory.set(layout.buildDirectory.dir("reports/screenshot-coverage"))
}
