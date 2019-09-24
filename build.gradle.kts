@file:Suppress("UnstableApiUsage")

buildscript {
  repositories {
    google()
    jcenter()
  }
}

plugins {
  id("com.gradle.plugin-publish") version "0.10.1"
  `kotlin-dsl`
  groovy
  id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
  jacoco
  `java-gradle-plugin`
  `maven-publish`
  id("org.jetbrains.kotlin.jvm") version "1.3.50"
  id("com.github.ben-manes.versions") version "0.25.0"
}

repositories {
  gradlePluginPortal()
  google()
  mavenCentral()
  maven("https://maven.fabric.io/public")
  jcenter()
}

dependencies {
  compileOnly("com.android.tools.build:gradle:3.5.0")

  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.0.0-RC12")

  implementation("com.github.triplet.gradle:play-publisher:2.4.1")
  implementation("com.google.apis:google-api-services-androidpublisher:v3-rev46-1.25.0")
  implementation("io.fabric.tools:gradle:1.31.1")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50")

  testImplementation("com.android.tools.build:gradle:3.5.0")
  testImplementation("junit:junit:4.12")
  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.eclipse.jgit:org.eclipse.jgit:3.5.0.201409260305-r")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
  testImplementation("org.mockito:mockito-core:3.3.0")
}

jacoco {
  toolVersion = "0.8.2"
}

group = "com.xmartlabs"
version = "1.0.3"

gradlePlugin {
  plugins {
    create("snapshot-publisher") {
      id = "com.xmartlabs.snapshot-publisher"
      displayName = "Android Snapshot Publisher"
      description = "Android Snapshot Publisher is a Gradle plugin to prepare and distribute Android Snapshot " +
          "versions to multiple distribution sources in a common way."
      implementationClass = "com.xmartlabs.snapshotpublisher.SnapshotPublisherPlugin"
    }
  }
}

pluginBundle {
  website = "https://github.com/xmartlabs/android-snapshot-publisher"
  vcsUrl = "https://github.com/xmartlabs/android-snapshot-publisher.git"
  @Suppress("UnstableApiUsage")
  tags = listOf("android", "google-play", "beta-fabric", "snapshot-build", "publisher")
}

detekt {
  toolVersion = "1.0.0-RC12"
  input = files("$projectDir")
  filters = ".*/resources/.*,.*/build/.*"
  config = files("detekt-config.yml")
}
