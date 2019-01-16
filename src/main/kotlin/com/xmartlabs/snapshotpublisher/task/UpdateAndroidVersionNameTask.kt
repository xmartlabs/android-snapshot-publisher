package com.xmartlabs.snapshotpublisher.task

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.xmartlabs.snapshotpublisher.utils.getSnapshotReleaseExtension
import com.xmartlabs.snapshotpublisher.utils.getVersionName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class UpdateAndroidVersionNameTask : DefaultTask() {
    lateinit var variant: ApplicationVariant

    init {
        description = "Update version name"
    }

    @TaskAction
    fun action() {
        if (project.getSnapshotReleaseExtension().useHashCommitInVersionName) {
            variant.outputs.all { output ->
                (output as ApkVariantOutput).versionNameOverride = project.getVersionName(variant)
            }
        }
    }
}
