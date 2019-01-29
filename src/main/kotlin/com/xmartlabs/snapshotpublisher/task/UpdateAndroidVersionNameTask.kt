package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.utils.getVersionName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class UpdateAndroidVersionNameTask : DefaultTask() {
  @get:Internal
  lateinit var variant: ApplicationVariant

  init {
    description = "Update Android Version name"
  }

  @Suppress("unused")
  @TaskAction
  fun action() {
    val versionName = project.getVersionName(variant)
    variant.outputs.all { output ->
      (output as? ApkVariantOutput)?.versionNameOverride = versionName
    }
  }
}
