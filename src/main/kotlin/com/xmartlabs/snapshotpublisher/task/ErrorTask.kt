package com.xmartlabs.snapshotpublisher.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ErrorTask : DefaultTask() {
  lateinit var message: String

  @Suppress("unused")
  @TaskAction
  fun action() {
    throw IllegalStateException(message)
  }
}
