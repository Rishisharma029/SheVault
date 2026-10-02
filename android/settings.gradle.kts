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
        google()
        mavenCentral()
    }
}

rootProject.name = "SheVault"

include(":app")

// Core modules
include(":core:design")
include(":core:database")
include(":core:network")
include(":core:security")
include(":core:permissions")
include(":core:common")

// Feature modules
include(":feature:home")
include(":feature:sos")
include(":feature:incident")
include(":feature:trustedcircle")
include(":feature:disretmode")
include(":feature:saferoute")
include(":feature:history")
include(":feature:settings")
include(":feature:onboarding")

// Service modules (Foreground Services, Sensors, Dispatch)
include(":service:IncidentService")
include(":service:LocationService")
include(":service:SensorService")
include(":service:DeliveryService")

