package dev.bartuzen.qbitcontroller

import org.gradle.api.JavaVersion as GradleJavaVersion

object Versions {
    const val AppVersion = "2.2.1"
    const val AppVersionCode = 29

    object Android {
        const val CompileSdk = 36
        const val TargetSdk = 36
        const val MinSdk = 23

        val JavaVersion = GradleJavaVersion.VERSION_21
        const val JvmTarget = "21"
    }
}
