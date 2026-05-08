# Maven Central publish plugin

[![Maven Central](https://img.shields.io/maven-central/v/dev.g000sha256/sonatype-maven-central?label=Maven%20Central&labelColor=171C35&color=E38E33)](https://central.sonatype.com/artifact/dev.g000sha256/sonatype-maven-central)

This is an unofficial Gradle plugin that simplifies the process of publishing your artifacts to the
[Sonatype Maven Central Repository](https://central.sonatype.com). It uses the standard
[Maven Publish plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
and [Signing plugin](https://docs.gradle.org/current/userguide/signing_plugin.html), and auto-applies
the [Gradle Signing plugin](https://github.com/g000sha256/gradle-signing) for in-memory PGP keys.

## Initialization

### Add plugin repository

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
    }
}
```

### Apply plugin

```kotlin
plugins {
    id("dev.g000sha256.sonatype-maven-central") version "<latest>"
}
```

> [!NOTE]
> The plugins `org.gradle.maven-publish`, `org.gradle.signing`, and
> [`dev.g000sha256.signing`](https://github.com/g000sha256/gradle-signing) will be applied automatically, so you don't need to
> add them manually.

## Configuration

### Choose a publishing type

The plugin supports two publishing types:

- `SonatypeMavenCentralType.Manual` (default) - a deployment will go through validation and require
  the user to manually publish it via the [Portal UI](https://central.sonatype.com/publishing/deployments)
- `SonatypeMavenCentralType.Automatic` - a deployment will go through validation and, if it passes,
  will be automatically published to Maven Central

By default, the type is set to `Manual`, but you can override it using a plugin extension:

```kotlin
import g000sha256.sonatype_maven_central.SonatypeMavenCentralType

sonatypeMavenCentralRepository {
    type = SonatypeMavenCentralType.Automatic
}
```

### Set up Sonatype credentials

Store your [Sonatype credentials](https://central.sonatype.org/publish/generate-portal-token)
securely in your private Gradle properties file (`~/.gradle/gradle.properties`):

```properties
SonatypeMavenCentral.Username=<your sonatype portal username>
SonatypeMavenCentral.Password=<your sonatype portal password>
```

For CI/CD, the plugin also reads the credentials from environment variables:

```shell
SONATYPE_USERNAME=<your sonatype portal username>
SONATYPE_PASSWORD=<your sonatype portal password>
```

You can also override the credentials using a plugin extension:

```kotlin
sonatypeMavenCentralRepository {
    credentials {
        username = "<your sonatype portal username>"
        password = "<your sonatype portal password>"
    }
}
```

> [!NOTE]
> The plugin reads credentials in the following order: extension, then environment variable, then Gradle property.

### Register Maven publication

```kotlin
publishing {
    publications {
        register<MavenPublication>("<your publication variant>") {
            // your publication configuration
        }
    }
}
```

### Configure signing

There are two steps: add signing keys and choose what to sign.

#### Add keys

The plugin auto-applies the [Gradle Signing plugin](https://github.com/g000sha256/gradle-signing) for in-memory PGP keys.

For CI/CD, set environment variables:

```shell
SIGNING_KEY=<your signing key>
SIGNING_PASSWORD=<your signing password>
# optional
SIGNING_KEY_ID=<your signing key id>
```

Or store the credentials in your private Gradle properties file (`~/.gradle/gradle.properties`):

```properties
signing.key=<your signing key>
signing.password=<your signing password>
# optional
signing.keyId=<your signing key id>
```

> [!NOTE]
> If those values aren't resolved, the standard Signing plugin's
> file-based [GPG credentials](https://central.sonatype.org/publish/requirements/gpg)
> (`signing.keyId`, `signing.password`, `signing.secretKeyRingFile`) still work.

You can also override the configured keys via your own `signing` block:

```kotlin
signing {
    useInMemoryPgpKeys("<your signing key>", "<your signing password>")
}
```

#### Sign

A specific publication:

```kotlin
signing {
    val publication = publishing.publications["<your publication variant>"]
    sign(publication)
}
```

or all publications:

```kotlin
signing {
    sign(publishing.publications)
}
```

See [signing publications](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signing_publications) in the
Gradle docs for more options.

## Publishing

```shell
./gradlew publish
```

or

```shell
./gradlew publishAllPublicationsToSonatypeMavenCentralRepository
```

or

```shell
./gradlew publish<your publication variant>PublicationToSonatypeMavenCentralRepository
```
