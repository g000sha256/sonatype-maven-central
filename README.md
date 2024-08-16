# Sonatype Maven Central publish plugin

This plugin simplifies publishing your artifacts to [Sonatype Maven Central](https://central.sonatype.org).
It uses the standard plugins such as `org.gradle.maven-publish` and `org.gradle.signing`.

## Usage

### Add plugin repository

```kotlin
repositories {
    gradlePluginPortal()
}
```

### Add Sonatype plugin and necessary plugins

```kotlin
plugins {
    id("dev.g000sha256.sonatype-maven-central") version "0.0.7"
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}
```

### Configure Sonatype plugin

```kotlin
import g000sha256.sonatype_maven_central.SonatypeMavenCentralType

sonatypeMavenCentralRepository {
    type = SonatypeMavenCentralType.Manual

    credentials {
        username = properties["SonatypeMavenCentral.Username"] as String?
        password = properties["SonatypeMavenCentral.Password"] as String?
    }
}
```

You can configure the plugin by selecting one of the following types for publishing:

- `SonatypeMavenCentralType.Manual` (default) - a deployment will go through validation and require the user to publish manually
  it via the [Portal UI](https://central.sonatype.com/publishing/deployments)
- `SonatypeMavenCentralType.Automatic` - a deployment will go through validation and, if it passes, automatically proceed to
  publish to Maven Central

### Configure necessary plugins

```kotlin
publishing {
    publications {
        register<MavenPublication>("<your publication variant>") {
            // configuration
        }
    }
}

signing {
    val publication = publishing.publications["<your publication variant>"]
    sign(publication)
}
```

### Configure GPG credentials

Store your [GPG credentials](https://central.sonatype.org/publish/requirements/gpg) securely in your private Gradle properties
file (`~/.gradle/gradle.properties`):

```properties
signing.keyId=<your signing keyId>
signing.password=<your signing password>
signing.secretKeyRingFile=<your path to secring.gpg file>
```

### Configure Sonatype credentials

Store your [Sonatype credentials](https://central.sonatype.org/publish/generate-portal-token) securely in your private Gradle
properties file (`~/.gradle/gradle.properties`):

```properties
SonatypeMavenCentral.Username=<your sonatype username>
SonatypeMavenCentral.Password=<your sonatype password>
```

### Publishing

#### Publish all

```shell
./gradlew publish
```

#### Publish all variants to the Sonatype repository

```shell
./gradlew publishAllPublicationsToSonatypeMavenCentralRepository
```

#### Publish a specific variant to the Sonatype repository

```shell
./gradlew publish<your publication variant>PublicationToSonatypeMavenCentralRepository
```