package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.utils.ReleaseNotesGenerator
import com.xmartlabs.snapshotpublisher.utils.getSnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.utils.getVersionCode
import com.xmartlabs.snapshotpublisher.utils.getVersionName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class GenerateReleaseNotesTask : DefaultTask() {
  @get:Internal
  var variant: ApplicationVariant? = null
  @get:Internal
  lateinit var generatedReleaseNotes: String

  init {
    description = "Generates release notes"
  }

  @Suppress("unused")
  @TaskAction
  fun action() {
    val releaseSetup = project.getSnapshotReleaseExtension()
    generatedReleaseNotes = ReleaseNotesGenerator.generate(
        releaseNotesConfig = releaseSetup.releaseNotes,
        versionName = project.getVersionName(variant),
        versionCode = project.getVersionCode(variant)
    )

    project.logger.info("Generated Release Notes: \n$generatedReleaseNotes")

    releaseSetup.releaseNotes.outputFile
        ?.apply {
          parentFile.mkdirs()
          writeText(generatedReleaseNotes)
        }
  }
}
