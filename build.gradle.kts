import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "dev.g000sha256"
version = "0.0.6"

plugins {
    alias(catalog.plugins.gradle.pluginPublish)
    alias(catalog.plugins.jetbrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetbrains.kotlinJvm)
    id("org.gradle.java-gradle-plugin")
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        moduleName = "g000sha256.sonatype_maven_central"
    }

    sourceSets {
        main {
            dependencies {
                implementation(catalog.library.jetbrains.annotations)
                implementation(catalog.library.jetbrains.kotlin)

                implementation(catalog.library.ktor.core)
                implementation(catalog.library.ktor.java)
            }
        }
    }
}

val pomName = "Sonatype Maven Central publish plugin"
val pomDescription = "A plugin to publish artifacts to the Sonatype Maven Central repository"
val pomUrl = "https://github.com/g000sha256/sonatype-maven-central"

publishing {
    publications {
        withType<MavenPublication> {
            if (name == "pluginMaven") {
                pom {
                    name = pomName
                    description = pomDescription
                }
            }

            pom {
                url = pomUrl
                inceptionYear = "2024"

                licenses {
                    license {
                        name = "Apache License 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "g000sha256"
                        name = "Georgii Ippolitov"
                        email = "detmmpmznb@g000sha256.dev"
                        url = "https://github.com/g000sha256"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/g000sha256/sonatype-maven-central.git"
                    developerConnection = "scm:git:ssh://github.com:g000sha256/sonatype-maven-central.git"
                    url = "https://github.com/g000sha256/sonatype-maven-central/tree/master"
                }

                issueManagement {
                    system = "GitHub Issues"
                    url = "https://github.com/g000sha256/sonatype-maven-central/issues"
                }
            }
        }
    }
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    vcsUrl = pomUrl
    website = pomUrl

    plugins {
        register("release") {
            id = "dev.g000sha256.sonatype-maven-central"
            implementationClass = "g000sha256.sonatype_maven_central.SonatypeMavenCentralPlugin"

            displayName = pomName
            description = pomDescription
            tags = setOf("artifact", "central", "kotlin", "maven", "publish", "repository", "sonatype", "upload")
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