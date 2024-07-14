# Sonatype Maven Central upload plugin

This plugin simplifies the process of publishing your artifacts to Sonatype Maven Central. It uses the standard
plugins `org.gradle.maven-publish` and `org.gradle.signing`.

## Usage

#### Add plugin dependency

```kotlin
buildscript {
    dependencies {
        classpath("dev.g000sha256:sonatype-maven-central:0.0.1")
    }

    repositories {
        mavenCentral()
    }
}
```

#### Add and configure necessary plugins

```kotlin
plugins {
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}

publishing {
    publications {
        register<MavenPublication>("YourVariant") {
            // configuration
        }
    }
}

signing {
    // configuration
}
```

#### Configure credentials

Store your Sonatype credentials securely in your user's Gradle properties file (`~/.gradle/gradle.properties`):

```properties
SonatypeMavenCentral.Username=<your_sonatype_username>
SonatypeMavenCentral.Password=<your_sonatype_password>
```

#### Configure plugin

Use one of the types for publishing:

- `SonatypeMavenCentralType.Automatic` - a deployment will go through validation and, if it passes, automatically proceed to
  publish to Maven Central
- `SonatypeMavenCentralType.Manual` (default) - a deployment will go through validation and require the user to publish manually
  it via the [Portal UI](https://central.sonatype.com/publishing/deployments)

```kotlin
sonatypeMavenCentralRepository {
    type = SonatypeMavenCentralType.Manual

    credentials {
        username = properties["SonatypeMavenCentral.Username"] as String?
        password = properties["SonatypeMavenCentral.Password"] as String?
    }
}
```

#### Publishing

Publish all:

```shell
./gradlew publish
```

Publish specific variant (replace `<YourVariant>`) to the Sonatype repository:

```shell
./gradlew publish<YourVariant>PublicationToSonatypeMavenCentralRepository
```