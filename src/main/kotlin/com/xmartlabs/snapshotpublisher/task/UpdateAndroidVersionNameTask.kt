package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.plugin.AndroidPluginHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class UpdateAndroidVersionNameTask : DefaultTask() {
  @get:Internal
  lateinit var variant: ApplicationVariant

  @Suppress("unused")
  @TaskAction
  fun action() {
    val versionName = AndroidPluginHelper.getVersionName(project, variant)
    variant.outputs.all {
      (this as? ApkVariantOutput)?.versionNameOverride = versionName
    }
  }
}
