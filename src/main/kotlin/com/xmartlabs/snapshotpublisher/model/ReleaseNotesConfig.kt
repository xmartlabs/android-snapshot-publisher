package com.xmartlabs.snapshotpublisher.model

import com.xmartlabs.snapshotpublisher.Constants
import java.io.File

open class ReleaseNotesConfig {
  var versionFormat: String = Constants.RELEASE_NOTES_VERSION_FORMAT_DEFAULT_VALUE
  var headerFormat: String = Constants.RELEASE_NOTES_CONFIG_HEADER_FORMAT_DEFAULT_VALUE
  var commitHistoryFormat: String = Constants.RELEASE_NOTES_CONFIG_COMMIT_HISTORY_FORMAT_DEFAULT_VALUE
  var maxCommitHistoryLines: Int = Constants.RELEASE_NOTES_CONFIG_MAX_COMMIT_HISTORY_LINES_DEFAULT_VALUE
  @Suppress("MemberVisibilityCanBePrivate")
  var releaseNotesFormat: String = Constants.RELEASE_NOTES_CONFIG_FORMAT_DEFAULT_VALUE
  var outputFile: File? = Constants.RELEASE_NOTES_OUTPUT_FILE_DEFAULT_VALUE
  var includeMergeCommitsInHistory: Boolean = Constants.RELEASE_NOTES_INCLUDE_MERGE_COMMITS_DEFAULT_VALUE
  var includeLastCommitInHistory: Boolean = Constants.RELEASE_NOTES_INCLUDE_LAST_COMMIT_DEFAULT_VALUE

  internal fun getVersion(versionName: String, versionCode: Int) = versionFormat
      .replace(Constants.RELEASE_NOTES_VERSION_NAME_KEY, versionName, true)
      .replace(Constants.RELEASE_NOTES_VERSION_CODE_KEY, versionCode.toString(), true)

  internal fun getReleaseNotes(version: String, header: String, changelog: String): String = releaseNotesFormat
      .replace(Constants.RELEASE_NOTES_VERSION_KEY, version, true)
      .replace(Constants.RELEASE_NOTES_HEADER_KEY, header, true)
      .replace(Constants.RELEASE_NOTES_COMMIT_HISTORY_KEY, changelog, true)
}
