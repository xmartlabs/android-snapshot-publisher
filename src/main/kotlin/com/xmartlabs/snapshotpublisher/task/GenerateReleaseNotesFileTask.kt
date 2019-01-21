package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.utils.ReleaseNotesGenerator
import com.xmartlabs.snapshotpublisher.utils.getSnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.utils.getVersionName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateReleaseNotesFileTask : DefaultTask() {
  @get:Internal
  var variant: ApplicationVariant? = null
  @get:OutputFile
  var outputFile: File? = null

  init {
    description = "Save the generated release notes in a file"
  }

  @TaskAction
  fun action() {
    val releaseSetup = project.getSnapshotReleaseExtension()
    val generatedReleaseNotes = ReleaseNotesGenerator.generate(
        releaseNotesConfig = releaseSetup.releaseNotes,
        versionName = project.getVersionName(variant)
    )

    with(releaseSetup.releaseNotesTextFilePath) {
      if (!isNullOrBlank()) {
        outputFile = File(this)
            .apply { writeText(generatedReleaseNotes) }
      }
    }
  }
}
