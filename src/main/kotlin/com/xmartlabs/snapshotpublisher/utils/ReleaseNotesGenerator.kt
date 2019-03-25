package com.xmartlabs.snapshotpublisher.utils

import com.android.annotations.VisibleForTesting
import com.xmartlabs.snapshotpublisher.model.ReleaseNotesConfig

internal object ReleaseNotesGenerator {
  fun generate(releaseNotesConfig: ReleaseNotesConfig, versionName: String, versionCode: Int): String {
    val version = getVersionSection(releaseNotesConfig, versionName, versionCode)
    val header = getHeaderSection(releaseNotesConfig)
    val changelog = getHistorySection(releaseNotesConfig)
    return releaseNotesConfig.getReleaseNotes(version = version, header = header, changelog = changelog)
  }

  @VisibleForTesting
  internal fun getVersionSection(releaseNotesConfig: ReleaseNotesConfig, versionName: String, versionCode: Int) =
      releaseNotesConfig.getVersion(versionName = versionName, versionCode = versionCode)

  @VisibleForTesting
  internal fun getHeaderSection(releaseNotesConfig: ReleaseNotesConfig) =
      GitHelper.getLog(releaseNotesConfig.headerFormat, 1)

  @VisibleForTesting
  internal fun getHistorySection(releaseNotesConfig: ReleaseNotesConfig) =
      GitHelper.getHistoryFromPreviousCommit(releaseNotesConfig)
}
