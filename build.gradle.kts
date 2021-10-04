@file:Suppress("UnstableApiUsage")

buildscript {
  repositories {
    google()
  }
}

plugins {
  id("com.gradle.plugin-publish") version "0.14.0"
  `kotlin-dsl`
  kotlin("jvm") version "1.5.10"
  groovy
  id("io.gitlab.arturbosch.detekt") version "1.11.0"
  jacoco
  `java-gradle-plugin`
  `maven-publish`
  id("com.github.ben-manes.versions") version "0.33.0"
}

apply(from = "./scripts/dependency_updates.gradle")

repositories {
  gradlePluginPortal()
  google()
  mavenCentral()
}

dependencies {
  compileOnly("com.android.tools.build:gradle:7.0.0") // Compile only to not force a specific AGP version
  compileOnly("com.android.tools:common:30.0.0-beta03")

  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.0.0-RC12")

  implementation("com.github.triplet.gradle:android-publisher:3.4.0-agp7.0")
  implementation("com.github.triplet.gradle:play-publisher:3.4.0-agp7.0")
  implementation("com.google.apis:google-api-services-androidpublisher:v3-rev20200526-1.30.9")
  implementation("com.google.firebase:firebase-appdistribution-gradle:2.1.2")
  implementation("com.google.guava:guava:29.0-jre")

  testImplementation("com.android.tools.build:gradle:7.0.0")
  testImplementation( "org.mockito:mockito-inline:3.11.0")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.eclipse.jgit:org.eclipse.jgit:3.5.0.201409260305-r")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

jacoco {
  toolVersion = "0.8.2"
}

group = "com.xmartlabs"
version = "2.3.0-AGP7.0"

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
  autoCorrect = true
  input = files("$projectDir")
  config = files("detekt-config.yml")
  parallel = true
  toolVersion = "1.11.0"
}
