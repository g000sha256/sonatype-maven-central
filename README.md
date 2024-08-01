# Sonatype Maven Central publish plugin

This plugin simplifies the process of publishing your artifacts to
[Sonatype Maven Central](https://central.sonatype.org/register/central-portal). It uses the standard plugins such as
`org.gradle.maven-publish` and `org.gradle.signing`.

## Usage

### Add plugin dependency

```kotlin
buildscript {
    dependencies {
        classpath("dev.g000sha256:sonatype-maven-central:0.0.5")
    }

    repositories {
        mavenCentral()
    }
}
```

### Add and configure necessary plugins

```kotlin
plugins {
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}

publishing {
    publications {
        register<MavenPublication>("<your variant>") {
            // configuration
        }
    }
}

signing {
    val publication = publishing.publications["<your variant>"]
    sign(publication)
}
```

### Configure GPG credentials

Store your [GPG credentials](https://central.sonatype.org/publish/requirements/gpg) securely in your user's Gradle properties
file (`~/.gradle/gradle.properties`):

```properties
signing.keyId=<your keyId>
signing.password=<your password>
signing.secretKeyRingFile=<your path to secring.gpg file>
```

### Configure Sonatype credentials

Store your [Sonatype credentials](https://central.sonatype.org/publish/generate-portal-token) securely in your user's Gradle
properties file (`~/.gradle/gradle.properties`):

```properties
SonatypeMavenCentral.Username=<your sonatype username>
SonatypeMavenCentral.Password=<your sonatype password>
```

### Configure plugin

Use one of the types for publishing:

- `SonatypeMavenCentralType.Automatic` - a deployment will go through validation and, if it passes, automatically proceed to
  publish to Maven Central
- `SonatypeMavenCentralType.Manual` (default) - a deployment will go through validation and require the user to publish manually
  it via the [Portal UI](https://central.sonatype.com/publishing/deployments)

```kotlin
import g000sha256.sonatype_maven_central.SonatypeMavenCentralType
import g000sha256.sonatype_maven_central.sonatypeMavenCentralRepository

sonatypeMavenCentralRepository {
    type = SonatypeMavenCentralType.Manual

    credentials {
        username = properties["SonatypeMavenCentral.Username"] as String?
        password = properties["SonatypeMavenCentral.Password"] as String?
    }
}
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

#### Publish specific variant to the Sonatype repository

Replace `<your variant>`

```shell
./gradlew publish<your variant>PublicationToSonatypeMavenCentralRepository
```