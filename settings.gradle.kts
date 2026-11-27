pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// Keep the hook in the repository and activate it for every clone during Gradle Sync.
// This avoids unversioned, manually maintained files in .git/hooks.
val versionedHooksDir = file(".githooks")
val prePushHook = versionedHooksDir.resolve("pre-push")
if (prePushHook.isFile && file(".git").exists()) {
    if (!prePushHook.setExecutable(true)) {
        logger.warn("Could not mark ${prePushHook.path} as executable")
    }

    val gitConfig = providers.exec {
        workingDir(rootDir)
        commandLine(
            "git",
            "config",
            "--local",
            "core.hooksPath",
            versionedHooksDir.absolutePath,
        )
        isIgnoreExitValue = true
    }
    val output = gitConfig.standardOutput.asText.get().trim()
    check(gitConfig.result.get().exitValue == 0) {
        "Could not activate the versioned Git hooks during Gradle Sync: $output"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FinanceApp"
include(":app")

include(":coroutine-practice")
