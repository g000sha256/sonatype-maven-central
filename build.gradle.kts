import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val libraryGroup = "dev.g000sha256"
val libraryModule = "sonatype-maven-central"
val libraryVersion = "0.0.1"

group = libraryGroup
version = libraryVersion

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

val dokkaJavaDocTaskProvider = tasks.dokkaJavadoc

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
                description = "Sonatype Maven Central upload plugin"
                url = "https://github.com/g000sha256/sonatype-maven-central"
                inceptionYear = "2024"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "g000sha256"
                        name = "Georgii Ippolitov"
                        email = "bsuakseygr@g000sha256.dev"
                        url = "https://github.com/g000sha256"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/g000sha256/sonatype-maven-central.git"
                    developerConnection = "scm:git:ssh://github.com:g000sha256/sonatype-maven-central.git"
                    url = "https://github.com/g000sha256/sonatype-maven-central/tree/master"
                }

                issueManagement {
                    name = "GitHub Issues"
                    url = "https://github.com/g000sha256/sonatype-maven-central/issues"
                }
            }

            val component = components["kotlin"]
            from(component)

            artifact(sourcesJarTaskProvider) { builtBy(sourcesJarTaskProvider) }

            artifact(dokkaJavaDocJarTaskProvider) { builtBy(dokkaJavaDocJarTaskProvider) }
        }
    }
}

signing { sign(publishing.publications) }