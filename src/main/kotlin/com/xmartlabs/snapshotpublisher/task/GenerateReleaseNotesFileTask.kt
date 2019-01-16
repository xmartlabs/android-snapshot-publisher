package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.model.SnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.utils.ReleaseNotesGenerator
import com.xmartlabs.snapshotpublisher.utils.getSnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.utils.getVersionName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateReleaseNotesFileTask : DefaultTask() {
    var variant: ApplicationVariant? = null
    lateinit var generatedReleaseNotes: String

    init {
        description = "Save the generated release notes in a file"
    }

    @TaskAction
    fun action() {
        val releaseSetup = project.getSnapshotReleaseExtension()
        generatedReleaseNotes = ReleaseNotesGenerator.generate(
            releaseNotesConfig = releaseSetup.releaseNotes,
            versionName = project.getVersionName(variant)
        )

        with(releaseSetup.releaseNotesTextFilePath) {
            if (!isNullOrBlank()) {
                File(this).writeText(generatedReleaseNotes)
            }
        }
    }
}
