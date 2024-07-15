pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories { mavenCentral() }

    versionCatalogs {
        register("catalog") {
            val file = files("catalog.toml")
            from(file)
        }
    }
}