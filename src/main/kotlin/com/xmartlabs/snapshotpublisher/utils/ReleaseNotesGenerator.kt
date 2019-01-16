package com.xmartlabs.snapshotpublisher.utils

import com.xmartlabs.snapshotpublisher.model.ReleaseNotesConfig

internal object ReleaseNotesGenerator {
    fun generate(releaseNotesConfig: ReleaseNotesConfig, versionName: String): String =
        with(GitHelper) {
            val header = getLog(releaseNotesConfig.headerFormat, 1)
            val range = getLogRange(releaseNotesConfig.maxLinesOfChangelog)
            val changelog = getLog(releaseNotesConfig.changelogFormat, range)

            releaseNotesConfig.getReleaseNotes(versionName = versionName, header = header, changelog = changelog)
        }
}
