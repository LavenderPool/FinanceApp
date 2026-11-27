plugins {
    application
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.example.financeapp.practice.CoroutinePracticeKt")
    applicationDefaultJvmArgs = listOf("-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
