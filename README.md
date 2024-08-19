# Sonatype Maven Central publish plugin

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/dev.g000sha256.sonatype-maven-central?logo=gradle&label=Gradle%20Plugin%20Portal&labelColor=02303A&color=blue)](https://plugins.gradle.org/plugin/dev.g000sha256.sonatype-maven-central)

This plugin simplifies the process of publishing your artifacts to the
[Sonatype Maven Central](https://central.sonatype.org) repository. It utilizes standard plugins
such as the [Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
and the [Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html).

## Initialization

### Add plugin repository

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

### Apply plugin

```kotlin
plugins {
    id("dev.g000sha256.sonatype-maven-central") version "1.0.0"
}
```

> [!NOTE]
> The plugins `org.gradle.maven-publish` and `org.gradle.signing` will be applied automatically,
> so you don't need to add them manually.

## Configuration

### Choose a publishing type

The plugin can have two types of publishing:

- `SonatypeMavenCentralType.Manual` (default) - a deployment will go through validation and require
  the user to manually publish it via the [Portal UI](https://central.sonatype.com/publishing/deployments)
- `SonatypeMavenCentralType.Automatic` - a deployment will go through validation and, if it passes,
  will be automatically published to `Maven Central`

You can override the type using a plugin extension:

```kotlin
import g000sha256.sonatype_maven_central.SonatypeMavenCentralType

sonatypeMavenCentralRepository {
    type = SonatypeMavenCentralType.Manual
}
```

### Set up Sonatype credentials

Store your [Sonatype credentials](https://central.sonatype.org/publish/generate-portal-token)
securely in your private `Gradle` properties file (`~/.gradle/gradle.properties`):

```properties
SonatypeMavenCentral.Username=<your sonatype username>
SonatypeMavenCentral.Password=<your sonatype password>
```

You can override the credentials using a plugin extension:

```kotlin
sonatypeMavenCentralRepository {
    credentials {
        username = "<your sonatype username>"
        password = "<your sonatype password>"
    }
}
```

### Register Maven publication

```kotlin
publishing {
    publications {
        register<MavenPublication>("<your publication variant>") {
            groupId = "<your publication group id>"
            artifactId = "<your publication artifact id>"
            version = "<your publication version>"

            // pom/component/artifacts configuration
        }
    }
}
```

### Configure signing

Store your [GPG credentials](https://central.sonatype.org/publish/requirements/gpg)
securely in your private `Gradle` properties file (`~/.gradle/gradle.properties`):

```properties
signing.keyId=<your signing keyId>
signing.password=<your signing password>
signing.secretKeyRingFile=<your path to secring.gpg file>
```

Sign the publication:

```kotlin
signing {
    val publication = publishing.publications["<your publication variant>"]
    sign(publication)
}
```

## Publishing

### Publish all variants to all repositories

```shell
./gradlew publish
```

### Publish all variants to the Sonatype repository

```shell
./gradlew publishAllPublicationsToSonatypeMavenCentralRepository
```

### Publish a specific variant to the Sonatype repository

```shell
./gradlew publish<your publication variant>PublicationToSonatypeMavenCentralRepository
```