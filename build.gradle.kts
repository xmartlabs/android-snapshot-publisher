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
  id("com.github.ben-manes.versions") version "0.25.0"
}

repositories {
  gradlePluginPortal()
  google()
  mavenCentral()
  jcenter()
}

dependencies {
  compileOnly("com.android.tools.build:gradle:3.5.0")

  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.0.0-RC12")

  implementation("com.github.triplet.gradle:play-publisher:2.4.1")
  implementation("com.google.apis:google-api-services-androidpublisher:v3-rev46-1.25.0")
  implementation("com.google.firebase:firebase-appdistribution-gradle:1.0.0")

  testImplementation("com.android.tools.build:gradle:3.5.0")
  testImplementation("junit:junit:4.12")
  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.eclipse.jgit:org.eclipse.jgit:3.5.0.201409260305-r")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

jacoco {
  toolVersion = "0.8.2"
}

group = "com.xmartlabs"
version = "2.0.0-SNAPSHOT"

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
  tags = listOf("android", "google-play", "firebase", "firebase-app-distribution", "snapshot-build", "publisher")
}

detekt {
  toolVersion = "1.0.0-RC12"
  input = files("$projectDir")
  filters = ".*/resources/.*,.*/build/.*"
  config = files("detekt-config.yml")
}
