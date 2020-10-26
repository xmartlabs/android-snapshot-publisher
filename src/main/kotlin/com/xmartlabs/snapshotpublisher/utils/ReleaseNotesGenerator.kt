package com.xmartlabs.snapshotpublisher.utils

import com.google.common.annotations.VisibleForTesting
import com.xmartlabs.snapshotpublisher.model.ReleaseNotesConfig

internal object ReleaseNotesGenerator {
  fun generate(releaseNotesConfig: ReleaseNotesConfig, versionName: String, versionCode: Int?): String {
    val version = getVersionSection(releaseNotesConfig, versionName, versionCode)
    val header = getHeaderSection(releaseNotesConfig)
    val history = getHistorySection(releaseNotesConfig)
    val changelog = getChangelogSection(releaseNotesConfig)
    return releaseNotesConfig.getReleaseNotes(
        version = version,
        header = header,
        history = history,
        changelog = changelog
    )
  }

  fun truncateReleaseNotesIfNeeded(releaseNotes: String, maxCharacters: Int): String {
    if (releaseNotes.length <= maxCharacters) {
      return releaseNotes
    }

    var notes = ""
    releaseNotes.lines().forEach { line ->
      if (notes.length + line.length < maxCharacters) {
        notes += (if (notes.isEmpty()) "" else "\n") + line
      } else {
        return@forEach
      }
    }
    if (notes.isEmpty()) {
      notes = releaseNotes.substring(0, Math.min(releaseNotes.length, maxCharacters))
    }

    return notes
  }

  @VisibleForTesting
  internal fun getVersionSection(releaseNotesConfig: ReleaseNotesConfig, versionName: String, versionCode: Int?) =
      releaseNotesConfig.getVersion(versionName = versionName, versionCode = versionCode)

  @VisibleForTesting
  internal fun getHeaderSection(releaseNotesConfig: ReleaseNotesConfig) =
      GitHelper.getLog(releaseNotesConfig.headerFormat, 1)

  @VisibleForTesting
  internal fun getHistorySection(releaseNotesConfig: ReleaseNotesConfig) =
      getChangelogSection(releaseNotesConfig).let { changelog ->
        if (changelog.isBlank()) "" else releaseNotesConfig.getHistory(changelog)
      }

  private fun getChangelogSection(releaseNotesConfig: ReleaseNotesConfig) =
      GitHelper.getHistoryFromPreviousCommit(releaseNotesConfig)
}
