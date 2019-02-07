package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.model.ReleaseNotesConfig
import com.xmartlabs.snapshotpublisher.plugin.AndroidPluginHelper
import com.xmartlabs.snapshotpublisher.utils.GitHelper
import com.xmartlabs.snapshotpublisher.utils.snapshotReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateReleaseNotesTask : DefaultTask() {
  @get:Internal
  var variant: ApplicationVariant? = null

  private fun generate(releaseNotesConfig: ReleaseNotesConfig, versionName: String, versionCode: Int): String =
      with(GitHelper) {
        val header = getLog(releaseNotesConfig.headerFormat, 1)
        val range = getLogRange(releaseNotesConfig.maxCommitHistoryLines)
        val changelog = getLog(releaseNotesConfig.commitHistoryFormat, range)
        val version = releaseNotesConfig.getVersion(versionName = versionName, versionCode = versionCode)
        releaseNotesConfig.getReleaseNotes(version = version, header = header, changelog = changelog)
      }

  @Suppress("unused")
  @TaskAction
  fun action() {
    val releaseNotesConfig = project.snapshotReleaseExtension.releaseNotes
    val generatedReleaseNotes = generate(
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
