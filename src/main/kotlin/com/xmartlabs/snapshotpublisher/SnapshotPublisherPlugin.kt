package com.xmartlabs.snapshotpublisher

import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.task.GenerateReleaseNotesFileTask
import com.xmartlabs.snapshotpublisher.task.PrepareFabricReleaseTask
import com.xmartlabs.snapshotpublisher.task.UpdateAndroidVersionNameTask
import com.xmartlabs.snapshotpublisher.utils.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

@Suppress("unused")
class SnapshotPublisherPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            val releaseSetup =
                extensions.create(Constants.SNAPSHOT_PUBLISHER_EXTENSION_NAME, SnapshotReleaseExtension::class.java)

            if (hasAndroidExtension()) {
                createGenerateReleaseNotesFileTask()

                getAndroidExtension().applicationVariants.all { variant ->
                    val variantName = variant.getCapitalizedName()

                    createGenerateReleaseNotesFileTask(variant)
                    val assembleTask = getAssembleTask(variantName)

                    val updateVersionNameTask = createAndroidVersionTask(variantName, variant, assembleTask)

                    createDeployTask(variant, releaseSetup, assembleTask, updateVersionNameTask)
                }
            } else {
                throw GradleException("Android is not present")
            }
        }
    }

    private fun ApplicationVariant.getCapitalizedName() = name.capitalize()

    private fun Project.getBetaDistributionTask(variantName: String) =
        tasks.getByName("${Constants.BETA_DISTRIBUTION_TASK_NAME}$variantName")

    private fun Project.getAssembleTask(variantName: String) =
        tasks.getByName("${Constants.ASSEMBLE_TASK_NAME}$variantName")

    private fun Project.createGenerateReleaseNotesFileTask(variant: ApplicationVariant? = null) =
        createTask(
            Constants.GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME + (variant?.getCapitalizedName() ?: ""),
            GenerateReleaseNotesFileTask::class
        ) {
            this.variant = variant
        }

    private fun Project.createAndroidVersionTask(variantName: String, variant: ApplicationVariant, assembleTask: Task) =
        createTask(
            "${Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME}$variantName",
            UpdateAndroidVersionNameTask::class
        ) {
            this.variant = variant
            assembleTask.mustRunAfter(this)
        }

    private fun Project.createDeployTask(
        variant: ApplicationVariant,
        releaseSetup: SnapshotReleaseExtension,
        assembleTask: Task,
        updateVersionNameTask: UpdateAndroidVersionNameTask
    ) = createTask(
        "${Constants.FABRIC_BETA_SNAPSHOT_DEPLOY_TASK_NAME}${variant.getCapitalizedName()}",
        PrepareFabricReleaseTask::class
    ) {
        releaseFabricTask = getBetaDistributionTask(variant.getCapitalizedName())
        releaseNotes = {
            ReleaseNotesGenerator.generate(
                releaseNotesConfig = releaseSetup.releaseNotes,
                versionName = getVersionName(variant)
            )
        }
        dependsOn(updateVersionNameTask)
        dependsOn(assembleTask)
        finalizedBy(releaseFabricTask)
    }
}
