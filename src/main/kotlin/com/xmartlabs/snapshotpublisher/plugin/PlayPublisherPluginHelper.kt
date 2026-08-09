package com.xmartlabs.snapshotpublisher.plugin

import com.android.Version
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.utils.ErrorHelper
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByType
import org.gradle.util.GradleVersion
import java.io.File

internal object PlayPublisherPluginHelper {
  private const val PLAY_EXTENSION_NAME = "play"
  private val MIN_GRADLE_VERSION = GradleVersion.version("8.1")
  private val MIN_AGP_VERSION = GradleVersion.version("8.1.0")

  private const val GENERATE_RESOURCES_TASK_NAME = "generate%sPlayResources"
  private const val PUBLISH_APK_TASK_NAME = "publish%sApk"
  private const val PUBLISH_BUNDLE_TASK_NAME = "publish%sBundle"
  private const val PLAY_PUBLISHER_PLUGIN_CLASS = "com.github.triplet.gradle.play.PlayPublisherPlugin"

  private const val RESOURCE_PATH = "res"
  private const val OUTPUT_PATH = "gpp"
  private const val RESOURCES_OUTPUT_PATH = "generated/$OUTPUT_PATH"
  private const val RELEASE_NOTES_PATH = "release-notes"

  private fun PlayPublisherExtension.areCredsValid(): Boolean {
    val creds = serviceAccountCredentials.orNull?.asFile ?: return false
    return creds.extension.equals("json", true)
  }

  private fun playPath(variantName: String) = "$RESOURCES_OUTPUT_PATH/$variantName"

  private fun playResourcePath(variantName: String) = "${playPath(variantName)}/$RESOURCE_PATH"

  fun releaseNotesFile(project: Project, variantName: String, defaultLanguage: String, track: String) =
      project.layout.buildDirectory
          .file("${playResourcePath(variantName)}/$RELEASE_NOTES_PATH/$defaultLanguage/$track.txt")
          .get().asFile

  @Suppress("UnsafeCast")
  private val Project.playPublisherExtension
    get() = project.extensions.findByName(PLAY_EXTENSION_NAME) as PlayPublisherExtension

  private fun Project.getTask(capitalizedName: String, taskName: String): Task =
      project.tasks.getByName(taskName.format(capitalizedName))

  fun getPublishBundleTask(project: Project, capitalizedName: String): Task =
      project.getTask(capitalizedName, PUBLISH_BUNDLE_TASK_NAME)

  fun getPublishApkTask(project: Project, capitalizedName: String): Task =
      project.getTask(capitalizedName, PUBLISH_APK_TASK_NAME)

  fun getGenerateResourcesTask(project: Project, capitalizedName: String): Task =
      project.getTask(capitalizedName, GENERATE_RESOURCES_TASK_NAME)

  private fun checkGradleVersion() {
    val gradleVersion = GradleVersion.current()
    check(gradleVersion >= MIN_GRADLE_VERSION) {
      "Android Snapshot Publisher's minimum Gradle version is at least $MIN_GRADLE_VERSION and " +
          "yours is $gradleVersion. Find the latest version at " +
          "https://github.com/gradle/gradle/releases, then run " +
          "'./gradlew wrapper --gradle-version=\$LATEST --distribution-type=ALL'."
    }
  }

  private fun checkAgp() {
    val agpVersion = GradleVersion.version(Version.ANDROID_GRADLE_PLUGIN_VERSION)
    check(agpVersion >= MIN_AGP_VERSION) {
      "Android Snapshot Publisher's minimum Android Gradle Plugin version is at least " +
          "$MIN_AGP_VERSION and yours is $agpVersion. Find the latest version " +
          "and upgrade instructions at " +
          "https://developer.android.com/studio/releases/gradle-plugin."
    }
  }

  fun initializePlayPublisherPlugin(project: Project) {
    checkGradleVersion()
    checkAgp()

    var credentialsInitialized = false

    val android = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
    android.onVariants {
      val releaseSetup = project.snapshotReleaseExtension
      if (!credentialsInitialized) {
        credentialsInitialized = true
        setupPluginCredentials(project, releaseSetup)
      }
    }
    @Suppress("UnstableApiUsage")
    project.pluginManager.apply(Class.forName(PLAY_PUBLISHER_PLUGIN_CLASS))
  }

  private fun setupPluginCredentials(project: Project, releaseSetup: SnapshotReleaseExtension) {
    val playPublisherExtension = project.playPublisherExtension
    if (!playPublisherExtension.areCredsValid()) {
      val credentialFile = releaseSetup.googlePlay.serviceAccountCredentials
      playPublisherExtension.serviceAccountCredentials.set(
          if (ErrorHelper.isServiceAccountCredentialFileValid(project, credentialFile)) {
            project.file(credentialFile ?: "mock.json")
          } else {
            File("mock.json")
          }
      )
    }
  }
}
