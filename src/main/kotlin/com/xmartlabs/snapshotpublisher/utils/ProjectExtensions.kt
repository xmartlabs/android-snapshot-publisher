package com.xmartlabs.snapshotpublisher.utils

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import kotlin.reflect.KClass

private const val ANDROID_EXTENSION_NAME = "android"

fun Project.hasAndroidExtension() = hasProperty(ANDROID_EXTENSION_NAME)

fun Project.getAndroidExtension() = extensions.getByName(ANDROID_EXTENSION_NAME) as AppExtension

internal fun Project.getSnapshotReleaseExtension() =
  extensions.getByName(Constants.SNAPSHOT_PUBLISHER_EXTENSION_NAME) as SnapshotReleaseExtension

private fun getVersionNameSuffix(releaseSetup: SnapshotReleaseExtension) =
  if (releaseSetup.useHashCommitInVersionName) GitHelper.getCommitHash() else null

fun Project.getVersionName(variant: ApplicationVariant? = null): String {
  val androidExtension = project.getAndroidExtension()
  val versionNameSuffix = getVersionNameSuffix(getSnapshotReleaseExtension())
  return VersionHelper.getUpdatedVersionName(androidExtension, variant, versionNameSuffix)
}

internal inline fun <reified T : Task> Project.createTask(
  name: String,
  taskKClass: KClass<T>,
  crossinline block: T.() -> Unit = {}
): T {
  return tasks.create(name, taskKClass.java)
    .apply(block)
}
