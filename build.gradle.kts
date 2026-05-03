import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

group = "dev.g000sha256"
version = "2.0.0"

plugins {
    alias(catalog.plugins.g000sha256.sonatypeMavenCentral)
    alias(catalog.plugins.gradle.javaGradlePlugin)
    alias(catalog.plugins.gradle.mavenPublish)
    alias(catalog.plugins.gradle.signing)
    alias(catalog.plugins.jetBrains.dokka)
    alias(catalog.plugins.jetBrains.kotlin)
}

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()
    jvmToolchain(jdkVersion = 11)

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }

    compilerOptions {
        allWarningsAsErrors = true
        moduleName = "dev.g000sha256.sonatype_maven_central"
    }
}

tasks {
    named<Jar>(name = "javadocJar") {
        val taskProvider = named(name = "dokkaGeneratePublicationJavadoc")
        from(taskProvider)
    }
}

gradlePlugin {
    plugins {
        register("release") {
            id = "dev.g000sha256.sonatype-maven-central"
            implementationClass = "g000sha256.sonatype_maven_central.SonatypeMavenCentralPlugin"
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name = "Sonatype Maven Central publish plugin"
                description = "An unofficial Gradle plugin for publishing artifacts to the Sonatype Maven Central repository"

                url = "https://github.com/g000sha256/sonatype-maven-central"
                inceptionYear = "2024"

                licenses {
                    license {
                        name = "Apache License 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "g000sha256"
                        name = "Georgii Ippolitov"
                        email = "github@g000sha256.dev"
                        url = "https://github.com/g000sha256"
                    }
                }

                scm {
                    connection = "scm:git:https://github.com/g000sha256/sonatype-maven-central.git"
                    developerConnection = "scm:git:ssh://git@github.com/g000sha256/sonatype-maven-central.git"
                    url = "https://github.com/g000sha256/sonatype-maven-central"
                }

                issueManagement {
                    system = "GitHub Issues"
                    url = "https://github.com/g000sha256/sonatype-maven-central/issues"
                }
            }
        }
    }
}

signing {
    val key = getProperty("Signing.Key") ?: getEnvironment("SIGNING_KEY")
    val password = getProperty("Signing.Password") ?: getEnvironment("SIGNING_PASSWORD")
    useInMemoryPgpKeys(key, password)

    sign(publishing.publications)
}

private fun getProperty(key: String): String? {
    return properties[key] as String?
}

private fun getEnvironment(key: String): String? {
    return System.getenv(key)
}
