package com.xmartlabs.snapshotpublisher.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.crashlytics.tools.gradle.CrashlyticsPlugin
import org.gradle.api.Project
import org.gradle.api.Task

internal object FabricBetaPluginHelper {
  private const val BETA_DISTRIBUTION_TASK_NAME = "crashlyticsUploadDistribution"

  fun initializeFabricBetaPublisherPlugin(project: Project) =
      project.pluginManager.apply(CrashlyticsPlugin::class.java)

  fun getBetaDistributionTask(project: Project, variant: ApplicationVariant): Task =
      project.tasks.getByName("$BETA_DISTRIBUTION_TASK_NAME${variant.capitalizedName}")
}
