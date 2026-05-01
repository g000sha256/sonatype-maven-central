group = "dev.g000sha256"
version = "1.1.1"

plugins {
    alias(catalog.plugins.g000sha256.sonatypeMavenCentral)
    alias(catalog.plugins.gradle.javaGradlePlugin)
    alias(catalog.plugins.gradle.mavenPublish)
    alias(catalog.plugins.gradle.signing)
    alias(catalog.plugins.jetBrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetBrains.kotlin)
}

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()
    jvmToolchain(jdkVersion = 11)

    compilerOptions {
        allWarningsAsErrors = true
        moduleName = "g000sha256.sonatype_maven_central"
    }
}

dependencies {
    implementation(catalog.libs.jetBrains.coroutines)
    implementation(catalog.libs.ktor.client.core)
    implementation(catalog.libs.ktor.client.java)
    implementation(catalog.libs.ktor.http)
    implementation(catalog.libs.ktor.io)
    implementation(catalog.libs.ktor.utils)
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
                    connection = "scm:git:git://github.com/g000sha256/sonatype-maven-central.git"
                    developerConnection = "scm:git:git@github.com:g000sha256/sonatype-maven-central.git"
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

gradlePlugin {
    plugins {
        register("release") {
            id = "dev.g000sha256.sonatype-maven-central"
            implementationClass = "g000sha256.sonatype_maven_central.SonatypeMavenCentralPlugin"
        }
    }
}

signing {
    val key = getProperty("Signing.Key") ?: getEnvironment("SIGNING_KEY")
    val password = getProperty("Signing.Password") ?: getEnvironment("SIGNING_PASSWORD")
    useInMemoryPgpKeys(key, password)

    sign(publishing.publications)
}

sonatypeMavenCentralRepository {
    credentials {
        username = getProperty("SonatypeMavenCentral.Username") ?: getEnvironment("SONATYPE_USERNAME")
        password = getProperty("SonatypeMavenCentral.Password") ?: getEnvironment("SONATYPE_PASSWORD")
    }
}

private fun getProperty(key: String): String? {
    return properties[key] as String?
}

private fun getEnvironment(key: String): String? {
    return System.getenv(key)
}
