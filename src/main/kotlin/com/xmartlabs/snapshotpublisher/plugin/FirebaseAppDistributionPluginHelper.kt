package com.xmartlabs.snapshotpublisher.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.google.firebase.appdistribution.gradle.AppDistributionPlugin
import com.google.firebase.appdistribution.gradle.UploadDistributionTask
import org.gradle.api.Project

internal object FirebaseAppDistributionPluginHelper {
  private const val FIREBASE_APP_DISTRIBUTION_TASK_NAME = "appDistributionUpload"

  fun initializeAppDistributionPlugin(project: Project) =
      project.pluginManager.apply(AppDistributionPlugin::class.java)

  @SuppressWarnings("UnsafeCast")
  fun getDistributionTask(project: Project, variant: ApplicationVariant): UploadDistributionTask? =
      project.tasks
          .findByName("$FIREBASE_APP_DISTRIBUTION_TASK_NAME${variant.capitalizedName}") as UploadDistributionTask
}
