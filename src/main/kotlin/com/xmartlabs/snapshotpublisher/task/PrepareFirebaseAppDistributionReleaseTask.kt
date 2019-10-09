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
    val serviceAccountCredentialsFilePath = firebaseConfig.serviceAccountCredentials
    check(serviceAccountCredentialsFilePath != null) {
      "Firebase serviceAccountCredentials file must be defined.\n" +
          "Make sure that `serviceAccountCredentials` file is defined in " +
          "`firebaseAppDistribution` plugin's config block."
    }

    val credentialFile = project.file(serviceAccountCredentialsFilePath)

    check(credentialFile.exists()) {
      "Firebase serviceAccountCredentials file doesn't exist.\n" +
          "Make sure that ${credentialFile.absolutePath} exists."
    }

    with(releaseTask.appDistributionProperties) {
      appId = firebaseConfig.appId
      serviceCredentialsFile = serviceAccountCredentialsFilePath
      testers = firebaseConfig.distributionEmails
      groups = firebaseConfig.distributionGroupAliases
      releaseNotesFile = generatedReleaseNotesFile.absolutePath
    }
  }
}
