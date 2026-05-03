rootProject.name = "sonatype-maven-central"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        register("catalog") {
            val file = files("catalog.toml")
            from(file)
        }
    }
}
