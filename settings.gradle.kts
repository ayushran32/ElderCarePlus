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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()        // 🔥 REQUIRED FOR FIREBASE
        mavenCentral()  // 🔥 REQUIRED FOR KTX / BOM
    }
}

rootProject.name = "ElderCarePlus"
include(":app")
