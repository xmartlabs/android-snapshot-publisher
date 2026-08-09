package com.xmartlabs.snapshotpublisher.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationVariant
import com.google.common.annotations.VisibleForTesting
import com.xmartlabs.snapshotpublisher.model.VersionConfig
import com.xmartlabs.snapshotpublisher.utils.GitHelper
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByType

internal object AndroidPluginHelper {
  private const val ASSEMBLE_TASK_NAME = "assemble"
  private const val BUNDLE_TASK_NAME = "bundle"

  fun hasAndroidExtension(project: Project) =
      project.extensions.findByType(ApplicationExtension::class.java) != null

  fun getVersionName(project: Project, variant: ApplicationVariant? = null): String {
    val releaseSetup = project.snapshotReleaseExtension
    val versionName = variant?.outputs
        ?.map { it.versionName.orNull }
        ?.filterNot(String?::isNullOrBlank)
        ?.firstOrNull()

    val currentVersionName = versionName
        .orElse { project.extensions.getByType<ApplicationExtension>().defaultConfig.versionName }
        .orEmpty()

    return getVersionName(releaseSetup.version, currentVersionName)
  }

  @VisibleForTesting
  fun getVersionName(versionConfig: VersionConfig, currentVersionName: String) =
      versionConfig.getVersionName(currentVersionName, GitHelper.getCommitHash(), GitHelper.getBranchName())

  fun getVersionCode(project: Project, variant: ApplicationVariant? = null): Int? =
      variant?.outputs?.firstOrNull()?.versionCode?.orNull
          ?: project.extensions.getByType<ApplicationExtension>().defaultConfig.versionCode

  fun getBundleTask(project: Project, capitalizedName: String) =
      project.tasks.findByName("$BUNDLE_TASK_NAME$capitalizedName")

  fun getAssembleTask(project: Project, capitalizedName: String): Task =
      project.tasks.getByName("$ASSEMBLE_TASK_NAME$capitalizedName")
}

val ApplicationVariant.capitalizedName
  get() = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

private inline fun <T> T?.orElse(defaultValue: () -> T) = this ?: defaultValue.invoke()
