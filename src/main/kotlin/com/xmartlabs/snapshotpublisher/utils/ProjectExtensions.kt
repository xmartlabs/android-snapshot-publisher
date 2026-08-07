package com.xmartlabs.snapshotpublisher.utils

import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

@Suppress("UnsafeCast")
internal val Project.snapshotReleaseExtension: SnapshotReleaseExtension
  get() = extensions.getByName(Constants.SNAPSHOT_PUBLISHER_EXTENSION_NAME) as SnapshotReleaseExtension

internal inline fun <reified T : Task> Project.createTask(
    name: String,
    description: String? = null,
    group: String? = Constants.PLUGIN_GROUP,
    crossinline block: T.() -> Unit = {}
): TaskProvider<T> = tasks.register(name, T::class.java) {
  this.description = description
  this.group = group
  block()
}
