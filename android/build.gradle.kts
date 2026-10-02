plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

allprojects {
    val baseBuildDir = File(System.getProperty("user.home"), ".gradle-build/shevault")
    val relativeProjectPath = project.path.trimStart(':').replace(':', '/')
    layout.buildDirectory.set(File(baseBuildDir, if (relativeProjectPath.isEmpty()) "root" else relativeProjectPath))
}

