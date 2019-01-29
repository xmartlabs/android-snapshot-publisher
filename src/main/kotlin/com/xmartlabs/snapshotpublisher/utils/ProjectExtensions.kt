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

@Suppress("UnsafeCast")
fun Project.getAndroidExtension() = extensions.getByName(ANDROID_EXTENSION_NAME) as AppExtension

@Suppress("UnsafeCast")
internal fun Project.getSnapshotReleaseExtension() =
    extensions.getByName(Constants.SNAPSHOT_PUBLISHER_EXTENSION_NAME) as SnapshotReleaseExtension

fun Project.getVersionName(variant: ApplicationVariant? = null): String {
  val releaseSetup = getSnapshotReleaseExtension()
  val versionName = variant?.versionName ?: project.getAndroidExtension().defaultConfig.versionName
  return releaseSetup.version.getVersionName(versionName, GitHelper.getCommitHash())
}

fun Project.getVersionCode(variant: ApplicationVariant? = null): Int =
    variant?.versionCode ?: project.getAndroidExtension().defaultConfig.versionCode

internal inline fun <reified T : Task> Project.createTask(
    name: String,
    taskKClass: KClass<T>,
    crossinline block: T.() -> Unit = {}
): T = tasks.create(name, taskKClass.java)
    .apply { group = Constants.PLUGIN_GROUP }
    .apply(block)
