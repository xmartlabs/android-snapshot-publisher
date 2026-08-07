package com.xmartlabs.snapshotpublisher.task

import com.google.firebase.appdistribution.gradle.tasks.UploadDistributionTask
import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class PrepareFirebaseAppDistributionReleaseTask : DefaultTask() {
  @get:Internal
  lateinit var releaseTask: Task

  private val generatedReleaseNotesFile by lazy {
    project.layout.buildDirectory.file(Constants.OUTPUT_RELEASE_NOTES_FILE_PATH).get().asFile
  }

  @Suppress("unused")
  @TaskAction
  fun action() {
    val firebaseConfig = project.snapshotReleaseExtension.firebaseAppDistribution
    val uploadTask = releaseTask as UploadDistributionTask
    firebaseConfig.appId?.let { uploadTask.appId.set(it) }
    uploadTask.serviceCredentialsFile.set(
        project.file(requireNotNull(firebaseConfig.serviceAccountCredentials)).absolutePath
    )
    firebaseConfig.distributionEmails?.let { uploadTask.testers.set(it) }
    firebaseConfig.distributionGroupAliases?.let { uploadTask.groups.set(it) }
    uploadTask.releaseNotesFile.set(generatedReleaseNotesFile.absolutePath)
  }
}
