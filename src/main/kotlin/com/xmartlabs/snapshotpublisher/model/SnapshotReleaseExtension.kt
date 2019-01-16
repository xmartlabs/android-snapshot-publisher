package com.xmartlabs.snapshotpublisher.model

import com.xmartlabs.snapshotpublisher.Constants
import org.gradle.api.Action

open class SnapshotReleaseExtension {
    var releaseNotesTextFilePath: String = ""
    var useHashCommitInVersionName: Boolean = Constants.SNAPSHOT_RELEASE_USE_HASH_COMMIT_IN_VERSION_NAME_DEFAULT_VALUE

    val fabric: FabricRelease = FabricRelease()
    val releaseNotes: ReleaseNotesConfig = ReleaseNotesConfig()

    @Suppress("unused")
    fun fabric(action: Action<in FabricRelease>) = action.execute(fabric)

    @Suppress("unused")
    fun releaseNotes(action: Action<in ReleaseNotesConfig>) = action.execute(releaseNotes)
}
