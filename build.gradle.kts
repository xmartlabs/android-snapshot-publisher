@file:Suppress("UnstableApiUsage")

buildscript {
  repositories {
    google()
  }
}

plugins {
  id("com.gradle.plugin-publish") version "1.3.0"
  `kotlin-dsl`
  id("io.gitlab.arturbosch.detekt") version "1.23.7"
  jacoco
  `java-gradle-plugin`
  `maven-publish`
  id("com.github.ben-manes.versions") version "0.51.0"
}

apply(from = "./scripts/dependency_updates.gradle")

repositories {
  gradlePluginPortal()
  google()
  mavenCentral()
}

dependencies {
  compileOnly("com.android.tools.build:gradle:8.1.0")
  compileOnly("com.android.tools:common:31.1.0")

  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")

  implementation("com.github.triplet.gradle:android-publisher:3.10.1")
  implementation("com.github.triplet.gradle:play-publisher:3.10.1")
  implementation("com.google.apis:google-api-services-androidpublisher:v3-rev20231115-2.0.0")
  implementation("com.google.firebase:firebase-appdistribution-gradle:5.3.0")
  implementation("com.google.auth:google-auth-library-oauth2-http:1.20.0")
  implementation("com.google.http-client:google-http-client-gson:1.44.1")

  testImplementation("com.android.tools.build:gradle:8.1.0")
  testImplementation("org.mockito:mockito-inline:3.12.4")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.eclipse.jgit:org.eclipse.jgit:3.5.0.201409260305-r")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
  testImplementation(gradleTestKit())
}

jacoco {
  toolVersion = "0.8.12"
}

group = "com.xmartlabs"
version = "3.0.0"

gradlePlugin {
  website = "https://github.com/xmartlabs/android-snapshot-publisher"
  vcsUrl = "https://github.com/xmartlabs/android-snapshot-publisher.git"

  plugins {
    create("snapshot-publisher") {
      id = "com.xmartlabs.snapshot-publisher"
      displayName = "Android Snapshot Publisher"
      description = "Android Snapshot Publisher is a Gradle plugin to prepare and distribute Android Snapshot " +
          "versions to multiple distribution sources in a common way."
      implementationClass = "com.xmartlabs.snapshotpublisher.SnapshotPublisherPlugin"
      tags = listOf("android", "google-play", "firebase", "firebase-app-distribution", "snapshot-build", "publisher")
    }
  }
}

detekt {
  autoCorrect = true
  source.setFrom(files("$projectDir"))
  config.setFrom(files("detekt-config.yml"))
  parallel = true
}
