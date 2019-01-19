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
  lateinit var releaseNotes: () -> String
  @get:Internal
  lateinit var releaseFabricTask: Task

  init {
    description = "Prepare Fabric deploy"
  }

  @TaskAction
  fun action() {
    val fabricRelease = project.getSnapshotReleaseExtension().fabric
    with(releaseFabricTask.extensions) {
      add(BETA_DISTRIBUTION_EMAILS_EXTENSION_NAME, fabricRelease.distributionEmails)
      add(BETA_DISTRIBUTION_GROUP_ALIASES_EXTENSION_NAME, fabricRelease.distributionGroupAliases)
      add(BETA_DISTRIBUTION_NOTIFICATIONS_EXTENSION_NAME, fabricRelease.distributionNotifications)
      add(BETA_DISTRIBUTION_RELEASE_NOTES_EXTENSION_NAME, releaseNotes.invoke())
    }
  }
}
