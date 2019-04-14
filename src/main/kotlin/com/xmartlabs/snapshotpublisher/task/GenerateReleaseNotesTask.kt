package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.plugin.AndroidPluginHelper
import com.xmartlabs.snapshotpublisher.utils.ReleaseNotesGenerator
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateReleaseNotesTask : DefaultTask() {
  @get:Internal
  var variant: ApplicationVariant? = null

  @Suppress("unused")
  @TaskAction
  fun action() {
    val releaseNotesConfig = project.snapshotReleaseExtension.releaseNotes

    if (releaseNotesConfig.releaseNotesFormat.contains(Constants.RELEASE_NOTES_COMMIT_HISTORY_KEY)) {
      project.logger.warn("${Constants.RELEASE_NOTES_COMMIT_HISTORY_KEY} in `releaseNotesFormat` is deprecated and " +
          "it will remove in the nex major update")
    }

    val generatedReleaseNotes = ReleaseNotesGenerator.generate(
        releaseNotesConfig = releaseNotesConfig,
        versionName = AndroidPluginHelper.getVersionName(project, variant),
        versionCode = AndroidPluginHelper.getVersionCode(project, variant)
    )

    project.logger.info("Generated Release Notes: \n$generatedReleaseNotes")

    val releaseNotesFile = File(project.buildDir, Constants.OUTPUT_RELEASE_NOTES_FILE_PATH)
        .apply {
          parentFile.mkdirs()
          writeText(generatedReleaseNotes)
        }

    releaseNotesConfig.outputFile
        ?.apply {
          parentFile.mkdirs()
          releaseNotesFile.copyTo(target = this, overwrite = true)
        }
  }
}
