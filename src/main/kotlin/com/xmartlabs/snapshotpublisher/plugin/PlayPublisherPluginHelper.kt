package com.xmartlabs.snapshotpublisher.plugin

import com.android.Version
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.utils.ErrorHelper
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.the
import org.gradle.util.GradleVersion
import org.gradle.util.VersionNumber
import java.io.File

internal object PlayPublisherPluginHelper {
  private const val PLAY_EXTENSION_NAME = "play"
  private val MIN_GRADLE_VERSION = GradleVersion.version("6.5")
  private val MIN_AGP_VERSION = VersionNumber.parse("4.1.0-beta01")

  private const val GENERATE_RESOURCES_TASK_NAME = "generate%sPlayResources"
  private const val PUBLISH_APK_TASK_NAME = "publish%sApk"
  private const val PUBLISH_BUNDLE_TASK_NAME = "publish%sBundle"
  private const val PLAY_PUBLISHER_PLUGIN_CLASS = "com.github.triplet.gradle.play.PlayPublisherPlugin"

  // https://github.com/Triple-T/gradle-play-publisher/blob/3e86503431794792dc63dd6b5bb51e03493f1ed7/plugin/src/main/kotlin/com/github/triplet/gradle/play/internal/Constants.kt
  private const val RESOURCE_PATH = "res"
  private const val OUTPUT_PATH = "gpp"
  private const val RESOURCES_OUTPUT_PATH = "generated/$OUTPUT_PATH"
  private const val RELEASE_NOTES_PATH = "release-notes"

  private fun PlayPublisherExtension.areCredsValid(): Boolean {
    val creds = serviceAccountCredentials.orNull?.asFile  ?: return false
    return creds.extension.equals("json", true)
  }

  private val BaseVariant.playPath get() = "$RESOURCES_OUTPUT_PATH/$name"

  private val BaseVariant.playResourcePath get() = "$playPath/$RESOURCE_PATH"

  fun releaseNotesFile(project: Project, variant: BaseVariant, defaultLanguage: String, track: String) =
      File(project.buildDir, "${variant.playResourcePath}/$RELEASE_NOTES_PATH/$defaultLanguage/$track.txt")

  @Suppress("UnsafeCast")
  private val Project.playPublisherExtension
    get() = project.extensions.findByName(PLAY_EXTENSION_NAME) as PlayPublisherExtension

  private fun Project.getTask(variant: ApplicationVariant, taskName: String): Task =
      project.tasks.getByName(taskName.format(variant.capitalizedName))

  fun getPublishBundleTask(project: Project, variant: ApplicationVariant): Task =
      project.getTask(variant, PUBLISH_BUNDLE_TASK_NAME)

  fun getPublishApkTask(project: Project, variant: ApplicationVariant): Task =
      project.getTask(variant, PUBLISH_APK_TASK_NAME)

  fun getGenerateResourcesTask(project: Project, variant: ApplicationVariant): Task =
      project.getTask(variant, GENERATE_RESOURCES_TASK_NAME)

  // Required in https://github.com/Triple-T/gradle-play-publisher/blob/40092f24d68034395c4c3399dbef0c5eb2f2c484/common/validation/src/main/kotlin/com/github/triplet/gradle/common/validation/Validation.kt#L9
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
    val agpVersion = VersionNumber.parse(
        try {
          Version.ANDROID_GRADLE_PLUGIN_VERSION
        } catch (e: NoClassDefFoundError) {
          @Suppress("DEPRECATION") // TODO(#708): remove when 3.6 is the minimum
          com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
        }
    )
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

    val android = project.the<BaseAppModuleExtension>()
    android.onVariants v@{
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
            File("mock.json") // To skip Google Play Publisher validation
          }
      )
    }
  }
}
