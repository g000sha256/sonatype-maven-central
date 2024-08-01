import g000sha256.sonatype_maven_central.SonatypeMavenCentralType
import g000sha256.sonatype_maven_central.sonatypeMavenCentralRepository
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val libraryGroup = "dev.g000sha256"
val libraryModule = "sonatype-maven-central"
val libraryVersion = "0.0.5"

buildscript {
    dependencies { classpath(catalog.plugin.sonatype) }
}

plugins {
    alias(catalog.plugins.jetbrains.dokka)
    alias(catalog.plugins.jetbrains.kotlin.jvm)
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
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

                val gradleKotlinDsl = gradleKotlinDsl()
                implementation(gradleKotlinDsl)
            }
        }
    }
}

val sourcesJarTaskProvider = tasks.kotlinSourcesJar

val dokkaJavaDocTaskProvider = tasks.dokkaHtml

val dokkaJavaDocJarTaskProvider = tasks.register<Jar>("dokkaJavaDocJar") {
    archiveClassifier = "javadoc"
    group = "documentation"
    dependsOn(dokkaJavaDocTaskProvider)

    val dokkaJavaDocTask = dokkaJavaDocTaskProvider.get()
    from(dokkaJavaDocTask.outputDirectory)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = libraryGroup
            artifactId = libraryModule
            version = libraryVersion

            pom {
                name = "Sonatype Maven Central"
                description = "Sonatype Maven Central publish plugin"
                url = "https://github.com/g000sha256/sonatype-maven-central"
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

            val component = components["kotlin"]
            from(component)

            artifact(sourcesJarTaskProvider)
            artifact(dokkaJavaDocJarTaskProvider)
        }
    }
}

signing {
    val key = getProperty("Signing.Key") ?: getEnvironment("SIGNING_KEY")
    val password = getProperty("Signing.Password") ?: getEnvironment("SIGNING_PASSWORD")
    useInMemoryPgpKeys(key, password)

    val publication = publishing.publications["release"]
    sign(publication)
}

sonatypeMavenCentralRepository {
    type = SonatypeMavenCentralType.Manual

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