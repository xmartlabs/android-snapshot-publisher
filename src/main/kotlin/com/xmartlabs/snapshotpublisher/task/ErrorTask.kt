package com.xmartlabs.snapshotpublisher.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class ErrorTask : DefaultTask() {
  @Input
  lateinit var message: String

  @Suppress("unused")
  @TaskAction
  fun action() {
    throw IllegalStateException(message)
  }
}
