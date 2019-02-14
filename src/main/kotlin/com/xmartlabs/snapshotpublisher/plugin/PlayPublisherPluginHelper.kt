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
import java.io.File

internal object PlayPublisherPluginHelper {
  private const val PLAY_EXTENSION_NAME = "play"

  private const val GENERATE_RESOURCES_TASK_NAME = "generate%sPlayResources"
  private const val PUBLISH_APK_TASK_NAME = "publish%sApk"
  private const val PUBLISH_BUNDLE_TASK_NAME = "publish%sBundle"

  // https://github.com/Triple-T/gradle-play-publisher/blob/3e86503431794792dc63dd6b5bb51e03493f1ed7/plugin/src/main/kotlin/com/github/triplet/gradle/play/internal/Constants.kt
  private const val RESOURCE_PATH = "res"
  private const val OUTPUT_PATH = "gpp"
  private const val RESOURCES_OUTPUT_PATH = "generated/$OUTPUT_PATH"
  private const val RELEASE_NOTES_PATH = "release-notes"

  // https://github.com/Triple-T/gradle-play-publisher/blob/4d3f98128c8c86bc1ea37fd34d8f4b16dbf93d1b/plugin/src/main/kotlin/com/github/triplet/gradle/play/internal/Validation.kt#L28
  fun PlayPublisherExtension.areCredsValid(): Boolean {
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

  fun initializePlayPublisherPlugin(project: Project) {
    val releaseSetup = project.snapshotReleaseExtension
    project.afterEvaluate {
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
