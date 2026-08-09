package com.xmartlabs.snapshotpublisher.plugin

import org.gradle.api.Project
import org.gradle.api.Task

internal object FirebaseAppDistributionPluginHelper {
  private const val FIREBASE_APP_DISTRIBUTION_PLUGIN_ID = "com.google.firebase.appdistribution"
  private const val FIREBASE_APP_DISTRIBUTION_TASK_NAME = "appDistributionUpload"

  fun initializeAppDistributionPlugin(project: Project) =
      project.pluginManager.apply(FIREBASE_APP_DISTRIBUTION_PLUGIN_ID)

  fun getDistributionTask(project: Project, capitalizedName: String): Task? =
      project.tasks
          .findByName("$FIREBASE_APP_DISTRIBUTION_TASK_NAME$capitalizedName")
}
