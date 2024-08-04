rootProject.name = "sonatype-maven-central"

pluginManagement {
    repositories { mavenCentral() }
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