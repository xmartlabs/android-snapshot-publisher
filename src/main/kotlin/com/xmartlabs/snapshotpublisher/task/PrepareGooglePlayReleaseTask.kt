package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApplicationVariant
import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import com.github.triplet.gradle.androidpublisher.ResolutionStrategy
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.model.GooglePlayConfig
import com.xmartlabs.snapshotpublisher.plugin.PlayPublisherPluginHelper
import com.xmartlabs.snapshotpublisher.utils.AndroidPublisherHelper
import com.xmartlabs.snapshotpublisher.utils.ReleaseNotesGenerator
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import java.io.File

open class PrepareGooglePlayReleaseTask : DefaultTask() {
  companion object {
    private const val MAX_RELEASE_NOTES_LENGTH = 500
  }

  @get:Internal
  internal lateinit var publishGooglePlayTask: Task

  @get:Internal
  internal lateinit var variant: ApplicationVariant

  private val googlePlayConfig by lazy { project.snapshotReleaseExtension.googlePlay }
  private val publisher by lazy { AndroidPublisherHelper.buildPublisher(project, googlePlayConfig) }
  private val generatedReleaseNotesFile by lazy { File(project.buildDir, Constants.OUTPUT_RELEASE_NOTES_FILE_PATH) }

  @Suppress("unused")
  @TaskAction
  fun action() {
    createReleaseNotesFile()
    with(project.extensions.getByType<PlayPublisherExtension>()) {
      defaultToAppBundles.set(googlePlayConfig.defaultToAppBundles)
      releaseStatus.set(googlePlayConfig.toPublisherReleaseStatus())
      resolutionStrategy.set(googlePlayConfig.toPublisherResolutionStrategy())
      track.set(googlePlayConfig.track)
      setupCredentialFile(this)
    }
  }

  private fun setupCredentialFile(extension: PlayPublisherExtension) {
    val credentialsFile: File? = googlePlayConfig.serviceAccountCredentials
        ?.let(project::file)
    extension.serviceAccountCredentials.set(credentialsFile)
  }

  private fun createReleaseNotesFile() {
    AndroidPublisherHelper.read(
        skipIfNotFound = false,
        publisher = publisher,
        variant = variant,
        project = project
    ) { editId ->
      val details = details().get(variant.applicationId, editId).execute()
      PlayPublisherPluginHelper.releaseNotesFile(project, variant, details.defaultLanguage, googlePlayConfig.track)
          .apply {
            parentFile.mkdirs()
            writeText(getReleaseNotes())
          }
    }
  }

  private fun getReleaseNotes() =
      ReleaseNotesGenerator.truncateReleaseNotesIfNeeded(generatedReleaseNotesFile.readText(), MAX_RELEASE_NOTES_LENGTH)

  private fun GooglePlayConfig.toPublisherReleaseStatus(): ReleaseStatus {
    val releaseStatus = ReleaseStatus.values()
        .firstOrNull { it.publishedName == releaseStatus }
    return requireNotNull(releaseStatus) {
      "Invalid Release status: $releaseStatus"
    }
  }

  private fun GooglePlayConfig.toPublisherResolutionStrategy(): ResolutionStrategy {
    val releaseStatus = ResolutionStrategy.values()
        .firstOrNull { it.publishedName == resolutionStrategy }
    return requireNotNull(releaseStatus) {
      "Invalid Resolution status: $releaseStatus"
    }
  }
}
