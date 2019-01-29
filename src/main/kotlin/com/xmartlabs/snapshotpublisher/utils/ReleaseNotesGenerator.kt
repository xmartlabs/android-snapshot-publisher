package com.xmartlabs.snapshotpublisher.utils

import com.xmartlabs.snapshotpublisher.model.ReleaseNotesConfig

internal object ReleaseNotesGenerator {
  fun generate(releaseNotesConfig: ReleaseNotesConfig, versionName: String, versionCode: Int): String =
      with(GitHelper) {
        val header = getLog(releaseNotesConfig.headerFormat, 1)
        val range = getLogRange(releaseNotesConfig.maxCommitHistoryLines)
        val changelog = getLog(releaseNotesConfig.commitHistoryFormat, range)
        val version = releaseNotesConfig.getVersion(versionName = versionName, versionCode = versionCode)
        releaseNotesConfig.getReleaseNotes(version = version, header = header, changelog = changelog)
      }
}
