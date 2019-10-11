package com.xmartlabs.snapshotpublisher

import com.android.build.gradle.api.ApplicationVariant
import com.github.triplet.gradle.play.tasks.internal.PublishArtifactTaskBase
import com.xmartlabs.snapshotpublisher.model.FirebaseAppDistributionReleaseConfig
import com.xmartlabs.snapshotpublisher.model.GooglePlayConfig
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.plugin.AndroidPluginHelper
import com.xmartlabs.snapshotpublisher.plugin.FirebaseAppDistributionPluginHelper
import com.xmartlabs.snapshotpublisher.plugin.PlayPublisherPluginHelper
import com.xmartlabs.snapshotpublisher.plugin.capitalizedName
import com.xmartlabs.snapshotpublisher.task.ErrorTask
import com.xmartlabs.snapshotpublisher.task.GenerateReleaseNotesTask
import com.xmartlabs.snapshotpublisher.task.PrepareFirebaseAppDistributionReleaseTask
import com.xmartlabs.snapshotpublisher.task.PrepareGooglePlayReleaseTask
import com.xmartlabs.snapshotpublisher.task.UpdateAndroidVersionNameTask
import com.xmartlabs.snapshotpublisher.utils.ErrorHelper
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
      FirebaseAppDistributionPluginHelper.initializeAppDistributionPlugin(this)
      PlayPublisherPluginHelper.initializePlayPublisherPlugin(this)

      if (AndroidPluginHelper.hasAndroidExtension(this)) {
        createGenerateReleaseNotesTask()

        AndroidPluginHelper.getAndroidExtension(this).applicationVariants.whenObjectAdded {
          createTasksForVariant(this)
        }
      } else {
        throw GradleException("Android is not present")
      }
    }
  }

  private fun Project.createTasksForVariant(variant: ApplicationVariant) {
    val assembleTask = AndroidPluginHelper.getAssembleTask(this, variant)
    val bundleTask = AndroidPluginHelper.getBundleTask(this, variant)
    val updateVersionNameTask = createAndroidVersionTask(variant, assembleTask, bundleTask)
    val generateReleaseNotesTask = createGenerateReleaseNotesTask(variant, updateVersionNameTask)
    val preparationTasks = listOf(generateReleaseNotesTask, updateVersionNameTask)

    createPrepareApkSnapshotTask(variant, assembleTask, preparationTasks)
    if (bundleTask != null) {
      createPrepareBundleSnapshotTask(variant, bundleTask, preparationTasks)
    }
    createGooglePlayDeployTask(variant, preparationTasks)
    createFirebaseDeployTask(variant, assembleTask, preparationTasks)
  }

  private fun Project.createGenerateReleaseNotesTask(
      variant: ApplicationVariant? = null,
      updateVersionNameTask: UpdateAndroidVersionNameTask? = null
  ) = createTask<GenerateReleaseNotesTask>(
      name = Constants.GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME + (variant?.capitalizedName ?: ""),
      description = "Generates release notes"
  ) {
    this.variant = variant
    updateVersionNameTask?.mustRunAfter(this)
  }

  private fun Project.createAndroidVersionTask(
      variant: ApplicationVariant,
      assembleTask: Task,
      bundleTask: Task?
  ) = createTask<UpdateAndroidVersionNameTask>(
      name = "${Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME}${variant.capitalizedName}",
      description = "Update Android Version name"
  ) {
    this.variant = variant
    @Suppress("UnstableApiUsage")
    assembleTask.mustRunAfter(this)
    @Suppress("UnstableApiUsage")
    bundleTask?.mustRunAfter(this)
  }

  private fun Project.createPrepareApkSnapshotTask(
      variant: ApplicationVariant,
      assembleTask: Task,
      preparationTasks: List<Task>
  ) = createTask<DefaultTask>(
      name = "${Constants.PREPARE_APK_VERSION_TASK_NAME}${variant.capitalizedName}",
      description = "Prepare, compile and create an apk file."
  ) {
    preparationTasks.forEach { dependsOn(it) }
    dependsOn(assembleTask)
  }

  private fun Project.createPrepareBundleSnapshotTask(
      variant: ApplicationVariant,
      bundleTask: Task?,
      preparationTasks: List<Task>
  ) = createTask<DefaultTask>(
      name = "${Constants.PREPARE_BUNDLE_VERSION_TASK_NAME}${variant.capitalizedName}",
      description = "Prepare, compile and create a bundle file."
  ) {
    preparationTasks.forEach { dependsOn(it) }
    dependsOn(bundleTask)
  }

  private fun Project.createGooglePlayDeployTask(
      variant: ApplicationVariant,
      preparationTasks: List<Task>
  ) {
    if (variant.buildType.isDebuggable) {
      project.logger.info("Skipping debuggable build type '${variant.buildType.name}' for Google Play's tasks.")
      return
    }

    val googlePlayConfig = project.snapshotReleaseExtension.googlePlay
    if (ErrorHelper.isServiceAccountCredentialFileValid(project, googlePlayConfig.serviceAccountCredentials)) {
      val publishGooglePlayTask: PublishArtifactTaskBase = if (googlePlayConfig.defaultToAppBundles) {
        PlayPublisherPluginHelper.getPublishBundleTask(this, variant)
      } else {
        PlayPublisherPluginHelper.getPublishApkTask(this, variant)
      }
      val preparePublishTask = createTask<PrepareGooglePlayReleaseTask>(
          name = "${Constants.PREPARE_GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
          group = null,
          description = "Prepare and deploy snapshot build to Google Play"
      ) {
        this.variant = variant
        this.publishGooglePlayTask = publishGooglePlayTask

        val generateResourcesTask = PlayPublisherPluginHelper.getGenerateResourcesTask(project, variant)
        mustRunAfter(generateResourcesTask)
        generateResourcesTask.mustRunAfter(preparationTasks)
        dependsOn(preparationTasks)
      }

      createTask<DefaultTask>(
          name = "${Constants.GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
          group = Constants.PLUGIN_GROUP,
          description = "Release a snapshot version to Google Play"
      ) {
        dependsOn(preparePublishTask)
        dependsOn(publishGooglePlayTask)
      }
    } else {
      createGooglePlayErrorTask(googlePlayConfig, variant)
    }
  }

  private fun Project.createGooglePlayErrorTask(
      googlePlayConfig: GooglePlayConfig,
      variant: ApplicationVariant
  ) {
    val error = ErrorHelper.getServiceAccountFileErrorMessage(
        project,
        googlePlayConfig.serviceAccountCredentials,
        "googlePlay"
    )

    createTask<ErrorTask>(
        name = "${Constants.GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
        group = Constants.PLUGIN_GROUP,
        description = "Release a snapshot version to Google Play"
    ) {
      message = "Google Play credentials are not valid.\n${error ?: ""}"
    }
  }

  private fun Project.createFirebaseDeployTask(
      variant: ApplicationVariant,
      assembleTask: Task,
      preparationTasks: List<Task>
  ) {
    val publishTask = FirebaseAppDistributionPluginHelper.getDistributionTask(this, variant)
    if (publishTask == null) {
      logger
          .info("Skipping build type ${variant.buildType.name} due to Firebase App Distribution being disabled for it")
      return
    }
    val firebaseConfig = project.snapshotReleaseExtension.firebaseAppDistribution
    if (ErrorHelper.isServiceAccountCredentialFileValid(project, firebaseConfig.serviceAccountCredentials)) {
      val prepareReleaseTask = createTask<PrepareFirebaseAppDistributionReleaseTask>(
          name = "${Constants.PREPARE_FIREBASE_APP_DISTRIBUTION_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
          description = "Prepare the Firebase App Distribution snapshot release",
          group = null
      ) {
        releaseTask = publishTask
        dependsOn(preparationTasks)
      }

      createTask<DefaultTask>(
          name = "${Constants.FIREBASE_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
          description = "Prepare and deploy a snapshot build to Firebase App Distribution"
      ) {
        publishTask.mustRunAfter(assembleTask, prepareReleaseTask)
        dependsOn(assembleTask, prepareReleaseTask, preparationTasks, publishTask)
      }
    } else {
      createFirebaseErrorTask(firebaseConfig, variant)
    }
  }

  private fun Project.createFirebaseErrorTask(
      firebaseConfig: FirebaseAppDistributionReleaseConfig,
      variant: ApplicationVariant
  ) {
    val error = ErrorHelper.getServiceAccountFileErrorMessage(
        project,
        firebaseConfig.serviceAccountCredentials,
        "firebaseAppDistribution"
    )

    createTask<ErrorTask>(
        name = "${Constants.FIREBASE_SNAPSHOT_DEPLOY_TASK_NAME}${variant.capitalizedName}",
        group = Constants.PLUGIN_GROUP,
        description = "Release a snapshot version to Firebase App Distribution"
    ) {
      message = "Firebase App Distribution credentials are not valid.\n${error ?: ""}"
    }
  }
}
