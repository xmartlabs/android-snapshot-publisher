package com.xmartlabs.snapshotpublisher.task

import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.utils.ReleaseNotesGenerator
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PrepareFabricReleaseTask : DefaultTask() {
  companion object {
    private const val BETA_DISTRIBUTION_EMAILS_EXTENSION_NAME = "betaDistributionEmails"
    private const val BETA_DISTRIBUTION_GROUP_ALIASES_EXTENSION_NAME = "betaDistributionGroupAliases"
    private const val BETA_DISTRIBUTION_NOTIFICATIONS_EXTENSION_NAME = "betaDistributionNotifications"
    private const val BETA_DISTRIBUTION_RELEASE_NOTES_EXTENSION_NAME = "betaDistributionReleaseNotes"
    private const val MAX_RELEASE_NOTES_LENGTH = 16384
  }

  @get:Internal
  lateinit var releaseFabricTask: Task

  private val generatedReleaseNotesFile by lazy { File(project.buildDir, Constants.OUTPUT_RELEASE_NOTES_FILE_PATH) }

  @Suppress("unused")
  @TaskAction
  fun action() {
    val fabric = project.snapshotReleaseExtension.fabric
    with(releaseFabricTask.extensions) {
      add(BETA_DISTRIBUTION_EMAILS_EXTENSION_NAME, fabric.distributionEmails.toFabricStringList())
      add(BETA_DISTRIBUTION_GROUP_ALIASES_EXTENSION_NAME, fabric.distributionGroupAliases.toFabricStringList())
      add(BETA_DISTRIBUTION_NOTIFICATIONS_EXTENSION_NAME, fabric.distributionNotifications)
      add(BETA_DISTRIBUTION_RELEASE_NOTES_EXTENSION_NAME, getReleaseNotes())
    }
  }

  private fun getReleaseNotes() =
      ReleaseNotesGenerator.truncateReleaseNotesIfNeeded(generatedReleaseNotesFile.readText(), MAX_RELEASE_NOTES_LENGTH)

  private fun List<String>?.toFabricStringList() = this?.joinToString(",") ?: ""
}
