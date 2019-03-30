package com.xmartlabs.snapshotpublisher.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.utils.GitHelper
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task

internal object AndroidPluginHelper {
  private const val ANDROID_EXTENSION_NAME = "android"

  private const val ASSEMBLE_TASK_NAME = "assemble"
  private const val BUNDLE_TASK_NAME = "bundle"

  fun hasAndroidExtension(project: Project) = project.hasProperty(ANDROID_EXTENSION_NAME)

  @Suppress("UnsafeCast")
  fun getAndroidExtension(project: Project) = project.extensions.getByName(ANDROID_EXTENSION_NAME) as AppExtension

  fun getVersionName(project: Project, variant: ApplicationVariant? = null): String {
    val releaseSetup = project.snapshotReleaseExtension
    val versionNameOverride = variant?.outputs
        ?.map { (it as? ApkVariantOutput)?.versionNameOverride }
        ?.filterNot { it.isNullOrBlank() }
        ?.first()

    val versionName = versionNameOverride
        .orElse { variant?.versionName }
        .orElse { getAndroidExtension(project).defaultConfig.versionName }
        .orEmpty()

    return releaseSetup.version.getVersionName(versionName, GitHelper.getCommitHash(), GitHelper.getBranchName())
  }

  fun getVersionCode(project: Project, variant: ApplicationVariant? = null): Int =
      variant?.versionCode ?: getAndroidExtension(project).defaultConfig.versionCode

  fun getBundleTask(project: Project, variant: ApplicationVariant) =
      project.tasks.findByName("$BUNDLE_TASK_NAME${variant.capitalizedName}")

  fun getAssembleTask(project: Project, variant: ApplicationVariant): Task =
      project.tasks.getByName("$ASSEMBLE_TASK_NAME${variant.capitalizedName}")
}

val ApplicationVariant.capitalizedName get() = name.capitalize()

private inline fun <T> T?.orElse(defaultValue: () -> T) = this ?: defaultValue.invoke()
