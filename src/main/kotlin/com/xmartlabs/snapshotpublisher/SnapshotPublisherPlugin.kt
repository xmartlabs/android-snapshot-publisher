package com.xmartlabs.snapshotpublisher

import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.plugin.AndroidPluginHelper
import com.xmartlabs.snapshotpublisher.plugin.FabricBetaPluginHelper
import com.xmartlabs.snapshotpublisher.plugin.PlayPublisherPluginHelper
import com.xmartlabs.snapshotpublisher.plugin.capitalizedName
import com.xmartlabs.snapshotpublisher.task.ErrorTask
import com.xmartlabs.snapshotpublisher.task.GenerateReleaseNotesTask
import com.xmartlabs.snapshotpublisher.task.PrepareFabricReleaseTask
import com.xmartlabs.snapshotpublisher.task.PrepareGooglePlayReleaseTask
import com.xmartlabs.snapshotpublisher.task.UpdateAndroidVersionNameTask
import com.xmartlabs.snapshotpublisher.utils.createTask
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

@Suppress("unused")
class SnapshotPublisherPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      extensions.create(Constants.SNAPSHOT_PUBLISHER_EXTENSION_NAME, SnapshotReleaseExtension::class.java)
      FabricBetaPluginHelper.initializeFabricBetaPublisherPlugin(this)
      PlayPublisherPluginHelper.initializePlayPublisherPlugin(this)

      if (AndroidPluginHelper.hasAndroidExtension(this)) {
        createGenerateReleaseNotesTask()

        AndroidPluginHelper.getAndroidExtension(this).applicationVariants.all { variant ->
          if (variant.buildType.isDebuggable) {
            project.logger.info("Skipping debuggable build type ${variant.buildType.name}.")
            return@all
          }

          val generateReleaseNotesTask = createGenerateReleaseNotesTask(variant)
          val assembleTask = AndroidPluginHelper.getAssembleTask(this, variant)
          val bundleTask = AndroidPluginHelper.getBundleTask(this, variant)
          val updateVersionNameTask = createAndroidVersionTask(variant, assembleTask, bundleTask)

          createFabricDeployTask(variant, generateReleaseNotesTask, assembleTask, updateVersionNameTask)
          createGooglePlayDeployTask(variant, generateReleaseNotesTask, updateVersionNameTask)
        }
      } else {
        throw GradleException("Android is not present")
      }
    }
  }

  private fun Project.createGenerateReleaseNotesTask(variant: ApplicationVariant? = null) =
    createTask<GenerateReleaseNotesTask>(
      name = Constants.GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME + (variant?.capitalizedName ?: ""),
      description = "Generates release notes"
    ) {
      this.variant = variant
    }

  private fun Project.createAndroidVersionTask(
      variant: ApplicationVariant,
      assembleTask: Task,
      bundleTask: Task?
  ) =
    createTask<UpdateAndroidVersionNameTask>(
      name = "${Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME}${variant.capitalizedName}",
      description = "Update Android Version name"
    ) {
      this.variant = variant
      @Suppress("UnstableApiUsage")
      assembleTask.mustRunAfter(this)
      @Suppress("UnstableApiUsage")
      bundleTask?.mustRunAfter(this)
    }

  private fun Project.createFabricDeployTask(
      variant: ApplicationVariant,
      generateReleaseNotesTask: GenerateReleaseNotesTask,
      assembleTask: Task,
      updateVersionNameTask: UpdateAndroidVersionNameTask
  ): DefaultTask {
    val releaseFabricTask = FabricBetaPluginHelper.getBetaDistributionTask(project, variant)
    val prepareFabricReleaseTask = createTask<PrepareFabricReleaseTask>(
      name = "${Constants.PREPARE_FABRIC_BETA_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
      description = "Prepare the Fabric snapshot release",
      group = null
    ) {
      this.releaseFabricTask = releaseFabricTask

      dependsOn(updateVersionNameTask)
      dependsOn(generateReleaseNotesTask)
      @Suppress("UnstableApiUsage")
      releaseFabricTask.mustRunAfter(this)
    }

    return createTask(
      name = "${Constants.FABRIC_BETA_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
      description = "Release a snapshot version to Fabric"
    ) {
      releaseFabricTask.mustRunAfter(assembleTask)
      dependsOn(assembleTask)
      dependsOn(prepareFabricReleaseTask)
      dependsOn(releaseFabricTask)
    }
  }

  private fun Project.createGooglePlayDeployTask(
      variant: ApplicationVariant,
      generateReleaseNotesTask: GenerateReleaseNotesTask,
      updateVersionNameTask: UpdateAndroidVersionNameTask
  ): DefaultTask {
    val googlePlayConfig = project.snapshotReleaseExtension.googlePlay
    return if (googlePlayConfig.areCredsValid()) {
      val publishGooglePlayTask = if (googlePlayConfig.defaultToAppBundles) {
        PlayPublisherPluginHelper.getPublishBundleTask(this, variant)
      } else {
        PlayPublisherPluginHelper.getPublishApkTask(this, variant)
      }
      val preparePublishTask = createTask<PrepareGooglePlayReleaseTask>(
        name = "${Constants.PREPARE_GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
        group = null,
        description = "Prepare the Google Play snapshot release"
      ) {
        this.variant = variant
        this.publishGooglePlayTask = publishGooglePlayTask

        val generateResourcesTask = PlayPublisherPluginHelper.getGenerateResourcesTask(project, variant)

        mustRunAfter(generateResourcesTask)
        @Suppress("UnstableApiUsage")
        generateResourcesTask.mustRunAfter(updateVersionNameTask)
        dependsOn(updateVersionNameTask)
        dependsOn(generateReleaseNotesTask)
      }

      createTask(
        name = "${Constants.GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
        group = Constants.PLUGIN_GROUP,
        description = "Release a snapshot version to Google Play"
      ) {
        dependsOn(preparePublishTask)
        dependsOn(publishGooglePlayTask)
      }
    } else {
      createTask<ErrorTask>(
        name = "${Constants.GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
        group = Constants.PLUGIN_GROUP,
        description = "Release a snapshot version to Google Play"
      ) {
        message = "Google Play credentials are not valid."
      }
    }
  }
}
