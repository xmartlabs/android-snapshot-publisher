package com.xmartlabs.snapshotpublisher.task

import com.xmartlabs.snapshotpublisher.utils.getSnapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class PrepareFabricReleaseTask : DefaultTask() {
  companion object {
    const val BETA_DISTRIBUTION_EMAILS_EXTENSION_NAME = "betaDistributionEmails"
    const val BETA_DISTRIBUTION_GROUP_ALIASES_EXTENSION_NAME = "betaDistributionGroupAliases"
    const val BETA_DISTRIBUTION_NOTIFICATIONS_EXTENSION_NAME = "betaDistributionNotifications"
    const val BETA_DISTRIBUTION_RELEASE_NOTES_EXTENSION_NAME = "betaDistributionReleaseNotes"
  }

  @get:Internal
  lateinit var generateReleaseNotesTask: GenerateReleaseNotesTask
  @get:Internal
  lateinit var releaseFabricTask: Task

  init {
    description = "Fabric Beta deploy"
  }

  @Suppress("unused")
  @TaskAction
  fun action() {
    val fabric = project.getSnapshotReleaseExtension().fabric
    with(releaseFabricTask.extensions) {
      add(BETA_DISTRIBUTION_EMAILS_EXTENSION_NAME, fabric.distributionEmails)
      add(BETA_DISTRIBUTION_GROUP_ALIASES_EXTENSION_NAME, fabric.distributionGroupAliases)
      add(BETA_DISTRIBUTION_NOTIFICATIONS_EXTENSION_NAME, fabric.distributionNotifications)
      add(BETA_DISTRIBUTION_RELEASE_NOTES_EXTENSION_NAME, generateReleaseNotesTask.generatedReleaseNotes)
    }
  }
}
