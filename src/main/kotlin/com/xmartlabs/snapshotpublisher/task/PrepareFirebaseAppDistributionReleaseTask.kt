package com.xmartlabs.snapshotpublisher.task

import com.google.firebase.appdistribution.gradle.UploadDistributionTask
import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PrepareFirebaseAppDistributionReleaseTask : DefaultTask() {
  @get:Internal
  lateinit var releaseTask: UploadDistributionTask

  private val generatedReleaseNotesFile by lazy { File(project.buildDir, Constants.OUTPUT_RELEASE_NOTES_FILE_PATH) }

  @Suppress("unused")
  @TaskAction
  fun action() {
    val firebaseConfig = project.snapshotReleaseExtension.firebaseAppDistribution
    with(releaseTask.appDistributionProperties.get()) {
      appId = firebaseConfig.appId
      serviceCredentialsFile = project.file(requireNotNull(firebaseConfig.serviceAccountCredentials)).absolutePath
      testers = firebaseConfig.distributionEmails
      groups = firebaseConfig.distributionGroupAliases
      releaseNotesFile = generatedReleaseNotesFile.absolutePath
    }
  }
}
