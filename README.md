# Maven Central publish plugin

[![Maven Central](https://img.shields.io/maven-central/v/dev.g000sha256/sonatype-maven-central?label=Maven%20Central&labelColor=171C35&color=E38E33)](https://central.sonatype.com/artifact/dev.g000sha256/sonatype-maven-central)

This `Gradle` plugin simplifies the process of publishing your artifacts to the
[Sonatype Maven Central Repository](https://central.sonatype.com). It uses standard plugins
such as the [Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
and the [Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html).

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
    id("dev.g000sha256.sonatype-maven-central") version "2.0.0"
}
```

> [!NOTE]
> The plugins `org.gradle.maven-publish` and `org.gradle.signing` will be applied automatically,
> so you don't need to add them manually.

## Configuration

### Choose a publishing type

The plugin supports two publishing types:

- `SonatypeMavenCentralType.Manual` (default) - a deployment will go through validation and require
  the user to manually publish it via the [Portal UI](https://central.sonatype.com/publishing/deployments)
- `SonatypeMavenCentralType.Automatic` - a deployment will go through validation and, if it passes,
  will be automatically published to `Maven Central`

By default, the type is set to `Manual`, but you can override it using a plugin extension:

```kotlin
import g000sha256.sonatype_maven_central.SonatypeMavenCentralType

sonatypeMavenCentralRepository {
    type = SonatypeMavenCentralType.Automatic
}
```

### Set up Sonatype credentials

Store your [Sonatype credentials](https://central.sonatype.org/publish/generate-portal-token)
securely in your private `Gradle` properties file (`~/.gradle/gradle.properties`):

```properties
SonatypeMavenCentral.Username=<your sonatype portal username>
SonatypeMavenCentral.Password=<your sonatype portal password>
```

For `CI/CD`, the plugin also reads the credentials from environment variables:

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
> The plugin reads credentials in the following order: extension, then environment variable, then `Gradle` property.

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

Store your [GPG credentials](https://central.sonatype.org/publish/requirements/gpg)
securely in your private `Gradle` properties file (`~/.gradle/gradle.properties`):

```properties
signing.keyId=<your signing keyId>
signing.password=<your signing password>
signing.secretKeyRingFile=<your path to secring.gpg file>
```

Also, you can set up [in-memory PGP keys](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys):

```kotlin
signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}
```

or

```kotlin
signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
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
`Gradle` docs for more options.

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
