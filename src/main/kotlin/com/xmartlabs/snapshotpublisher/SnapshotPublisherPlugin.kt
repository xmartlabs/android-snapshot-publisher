package com.xmartlabs.snapshotpublisher

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
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
import com.xmartlabs.snapshotpublisher.utils.ErrorHelper
import com.xmartlabs.snapshotpublisher.utils.createTask
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class SnapshotPublisherPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      extensions.create(Constants.SNAPSHOT_PUBLISHER_EXTENSION_NAME, SnapshotReleaseExtension::class.java)
      FirebaseAppDistributionPluginHelper.initializeAppDistributionPlugin(this)
      PlayPublisherPluginHelper.initializePlayPublisherPlugin(this)

      val androidComponents = extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
          ?: throw GradleException("Android is not present")

      createGenerateReleaseNotesTask()

      val variants = mutableListOf<ApplicationVariant>()
      androidComponents.onVariants { variant ->
        updateVariantVersionName(variant)
        variants.add(variant)
      }

      afterEvaluate {
        variants.forEach { variant ->
          createTasksForVariant(variant)
        }
      }
    }
  }

  private fun Project.createTasksForVariant(variant: ApplicationVariant) {
    val capitalizedName = variant.capitalizedName
    val assembleTask = AndroidPluginHelper.getAssembleTask(this, capitalizedName)
    val bundleTask = AndroidPluginHelper.getBundleTask(this, capitalizedName)
    val generateReleaseNotesTask = createGenerateReleaseNotesTask(variant)
    val preparationTasks: List<Any> = listOf(generateReleaseNotesTask)

    createPrepareApkSnapshotTask(capitalizedName, assembleTask, preparationTasks)
    val prepareBundleTask = if (bundleTask != null) {
      createPrepareBundleSnapshotTask(capitalizedName, bundleTask, preparationTasks)
    } else {
      null
    }
    createGooglePlayDeployTask(variant, assembleTask, prepareBundleTask, preparationTasks)
    createFirebaseDeployTask(variant, assembleTask, preparationTasks)
  }

  private fun Project.createGenerateReleaseNotesTask(
      variant: ApplicationVariant? = null
  ) = createTask<GenerateReleaseNotesTask>(
      name = Constants.GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME + (variant?.capitalizedName ?: ""),
      description = "Generates release notes"
  ) {
    this.variant = variant
  }

  private fun Project.updateVariantVersionName(variant: ApplicationVariant) {
    val releaseSetup = snapshotReleaseExtension
    val currentVersionName = variant.outputs.firstOrNull()?.versionName?.orNull
        ?: extensions.getByType<ApplicationExtension>().defaultConfig.versionName
        ?: ""
    val versionName = AndroidPluginHelper.getVersionName(releaseSetup.version, currentVersionName)

    variant.outputs.forEach { output ->
      output.versionName.set(versionName)
    }
  }

  private fun Project.createPrepareApkSnapshotTask(
      capitalizedName: String,
      assembleTask: Task,
      preparationTasks: List<Any>
  ) = createTask<DefaultTask>(
      name = "${Constants.PREPARE_APK_VERSION_TASK_NAME}$capitalizedName",
      description = "Prepare, compile and create an apk file."
  ) {
    preparationTasks.forEach { dependsOn(it) }
    dependsOn(assembleTask)
  }

  private fun Project.createPrepareBundleSnapshotTask(
      capitalizedName: String,
      bundleTask: Task?,
      preparationTasks: List<Any>
  ) = createTask<DefaultTask>(
      name = "${Constants.PREPARE_BUNDLE_VERSION_TASK_NAME}$capitalizedName",
      description = "Prepare, compile and create a bundle file."
  ) {
    preparationTasks.forEach { dependsOn(it) }
    dependsOn(bundleTask)
  }

  private fun Project.createGooglePlayDeployTask(
      variant: ApplicationVariant,
      assembleApkTask: Task,
      appBundleTask: TaskProvider<DefaultTask>?,
      preparationTasks: List<Any>
  ) {
    val capitalizedName = variant.capitalizedName
    val appExtension = extensions.getByType<ApplicationExtension>()
    val buildType = appExtension.buildTypes.findByName(variant.buildType ?: "")
    if (buildType?.isDebuggable == true) {
      project.logger.info("Skipping debuggable build type '${variant.buildType}' for Google Play's tasks.")
      return
    }

    val googlePlayConfig = project.snapshotReleaseExtension.googlePlay
    if (ErrorHelper.isServiceAccountCredentialFileValid(project, googlePlayConfig.serviceAccountCredentials)) {
      val publishGooglePlayTask: Task = if (googlePlayConfig.defaultToAppBundles) {
        PlayPublisherPluginHelper.getPublishBundleTask(this, capitalizedName)
      } else {
        PlayPublisherPluginHelper.getPublishApkTask(this, capitalizedName)
      }
      val compilationTask: Any? = if (googlePlayConfig.defaultToAppBundles) appBundleTask else assembleApkTask

      val preparePublishTask = createGooglePlayReleasePreparationTask(
          variant,
          capitalizedName,
          publishGooglePlayTask,
          preparationTasks + listOfNotNull(compilationTask)
      )

      createTask<DefaultTask>(
          name = "${Constants.GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}$capitalizedName",
          group = Constants.PLUGIN_GROUP,
          description = "Release a snapshot version to Google Play"
      ) {
        dependsOn(preparePublishTask)
        dependsOn(publishGooglePlayTask)
      }
    } else {
      createGooglePlayErrorTask(googlePlayConfig, capitalizedName)
    }
  }

  private fun Project.createGooglePlayReleasePreparationTask(
      variant: ApplicationVariant,
      capitalizedName: String,
      publishGooglePlayTask: Task,
      preparationTasks: List<Any>
  ) = createTask<PrepareGooglePlayReleaseTask>(
      name = "${Constants.PREPARE_GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}$capitalizedName",
      group = null,
      description = "Prepare and deploy snapshot build to Google Play"
  ) {
    this.variant = variant
    this.publishGooglePlayTask = publishGooglePlayTask

    val generateResourcesTask = PlayPublisherPluginHelper.getGenerateResourcesTask(project, capitalizedName)
    mustRunAfter(generateResourcesTask)

    preparationTasks
        .forEach { task ->
          generateResourcesTask.mustRunAfter(task)
          dependsOn(task)
        }
  }

  private fun Project.createGooglePlayErrorTask(
      googlePlayConfig: GooglePlayConfig,
      capitalizedName: String
  ) {
    val error = ErrorHelper.getServiceAccountFileErrorMessage(
        project,
        googlePlayConfig.serviceAccountCredentials,
        "googlePlay"
    )

    createTask<ErrorTask>(
        name = "${Constants.GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME}$capitalizedName",
        group = Constants.PLUGIN_GROUP,
        description = "Release a snapshot version to Google Play"
    ) {
      message = "Google Play credentials are not valid.\n${error ?: ""}"
    }
  }

  private fun Project.createFirebaseDeployTask(
      variant: ApplicationVariant,
      assembleTask: Task,
      preparationTasks: List<Any>
  ) {
    val capitalizedName = variant.capitalizedName
    val publishTask = FirebaseAppDistributionPluginHelper.getDistributionTask(this, capitalizedName)
    if (publishTask == null) {
      logger
          .info("Skipping build type ${variant.buildType} due to Firebase App Distribution being disabled for it")
      return
    }
    val firebaseConfig = project.snapshotReleaseExtension.firebaseAppDistribution
    if (ErrorHelper.isServiceAccountCredentialFileValid(project, firebaseConfig.serviceAccountCredentials)) {
      val prepareReleaseTask = createTask<PrepareFirebaseAppDistributionReleaseTask>(
          name = "${Constants.PREPARE_FIREBASE_APP_DISTRIBUTION_SNAPSHOT_DEPLOY_TASK_NAME}$capitalizedName",
          description = "Prepare the Firebase App Distribution snapshot release",
          group = null
      ) {
        releaseTask = publishTask
        preparationTasks.forEach { task -> dependsOn(task) }
      }

      createTask<DefaultTask>(
          name = "${Constants.FIREBASE_SNAPSHOT_DEPLOY_TASK_NAME}$capitalizedName",
          description = "Prepare and deploy a snapshot build to Firebase App Distribution"
      ) {
        publishTask.mustRunAfter(assembleTask)
        publishTask.mustRunAfter(prepareReleaseTask)
        dependsOn(assembleTask)
        dependsOn(prepareReleaseTask)
        dependsOn(preparationTasks)
        dependsOn(publishTask)
      }
    } else {
      createFirebaseErrorTask(firebaseConfig, capitalizedName)
    }
  }

  private fun Project.createFirebaseErrorTask(
      firebaseConfig: FirebaseAppDistributionReleaseConfig,
      capitalizedName: String
  ) {
    val error = ErrorHelper.getServiceAccountFileErrorMessage(
        project,
        firebaseConfig.serviceAccountCredentials,
        "firebaseAppDistribution"
    )

    createTask<ErrorTask>(
        name = "${Constants.FIREBASE_SNAPSHOT_DEPLOY_TASK_NAME}$capitalizedName",
        group = Constants.PLUGIN_GROUP,
        description = "Release a snapshot version to Firebase App Distribution"
    ) {
      message = "Firebase App Distribution credentials are not valid.\n${error ?: ""}"
    }
  }
}
