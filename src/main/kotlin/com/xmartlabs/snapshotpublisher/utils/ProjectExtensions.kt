package com.xmartlabs.snapshotpublisher.utils

import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task

@Suppress("UnsafeCast")
val Project.snapshotReleaseExtension: SnapshotReleaseExtension
  get() = extensions.getByName(Constants.SNAPSHOT_PUBLISHER_EXTENSION_NAME) as SnapshotReleaseExtension

internal inline fun <reified T : Task> Project.createTask(
    name: String,
    description: String? = null,
    group: String? = Constants.PLUGIN_GROUP,
    crossinline block: T.() -> Unit = {}
): T {
  val config: T.() -> Unit = {
    this.description = description
    this.group = group
    block()
  }

  return tasks.create(name, T::class.java, config)
}
