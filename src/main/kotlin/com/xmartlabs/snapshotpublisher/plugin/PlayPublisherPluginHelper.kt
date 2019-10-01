package com.xmartlabs.snapshotpublisher.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.github.triplet.gradle.play.PlayPublisherPlugin
import com.github.triplet.gradle.play.tasks.PublishApk
import com.github.triplet.gradle.play.tasks.PublishBundle
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.util.GradleVersion
import java.io.File

internal object PlayPublisherPluginHelper {
  private const val PLAY_EXTENSION_NAME = "play"
  private val MIN_GRADLE_VERSION = GradleVersion.version("5.6.1")

  private const val GENERATE_RESOURCES_TASK_NAME = "generate%sPlayResources"
  private const val PUBLISH_APK_TASK_NAME = "publish%sApk"
  private const val PUBLISH_BUNDLE_TASK_NAME = "publish%sBundle"

  // https://github.com/Triple-T/gradle-play-publisher/blob/3e86503431794792dc63dd6b5bb51e03493f1ed7/plugin/src/main/kotlin/com/github/triplet/gradle/play/internal/Constants.kt
  private const val RESOURCE_PATH = "res"
  private const val OUTPUT_PATH = "gpp"
  private const val RESOURCES_OUTPUT_PATH = "generated/$OUTPUT_PATH"
  private const val RELEASE_NOTES_PATH = "release-notes"

  // https://github.com/Triple-T/gradle-play-publisher/blob/4d3f98128c8c86bc1ea37fd34d8f4b16dbf93d1b/plugin/src/main/kotlin/com/github/triplet/gradle/play/internal/Validation.kt#L28
  private fun PlayPublisherExtension.areCredsValid(): Boolean {
    val creds = serviceAccountCredentials ?: return false
    return if (creds.extension.equals("json", true)) {
      serviceAccountEmail == null
    } else {
      serviceAccountEmail != null
    }
  }

  private val BaseVariant.playPath get() = "$RESOURCES_OUTPUT_PATH/$name"

  private val BaseVariant.playResourcePath get() = "$playPath/$RESOURCE_PATH"

  fun releaseNotesFile(project: Project, variant: BaseVariant, defaultLanguage: String, track: String) =
      File(project.buildDir, "${variant.playResourcePath}/$RELEASE_NOTES_PATH/$defaultLanguage/$track.txt")

  @Suppress("UnsafeCast")
  private val Project.playPublisherExtension
    get() = project.extensions.findByName(PLAY_EXTENSION_NAME) as PlayPublisherExtension

  @Suppress("UnsafeCast")
  fun getPublishBundleTask(project: Project, variant: ApplicationVariant): PublishBundle =
      project.tasks.getByName(PUBLISH_BUNDLE_TASK_NAME.format(variant.capitalizedName)) as PublishBundle

  @Suppress("UnsafeCast")
  fun getPublishApkTask(project: Project, variant: ApplicationVariant): PublishApk =
      project.tasks.getByName(PUBLISH_APK_TASK_NAME.format(variant.capitalizedName)) as PublishApk

  fun getGenerateResourcesTask(project: Project, variant: ApplicationVariant): Task =
      project.tasks.getByName(GENERATE_RESOURCES_TASK_NAME.format(variant.capitalizedName))

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

  fun initializePlayPublisherPlugin(project: Project) {
    checkGradleVersion()
    val releaseSetup = project.snapshotReleaseExtension
    project.beforeEvaluate {
      val playPublisherExtension = project.playPublisherExtension
      if (!playPublisherExtension.areCredsValid()) {
        playPublisherExtension.serviceAccountCredentials = if (releaseSetup.googlePlay.areCredsValid()) {
          releaseSetup.googlePlay.serviceAccountCredentials
        } else {
          File("mock.json") // To skip Google Play Publisher validation
        }
      }
    }
    @Suppress("UnstableApiUsage")
    project.pluginManager.apply(PlayPublisherPlugin::class.java)
  }
}
