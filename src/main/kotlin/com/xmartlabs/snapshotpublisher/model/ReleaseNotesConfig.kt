package com.xmartlabs.snapshotpublisher.model

import com.xmartlabs.snapshotpublisher.Constants

open class ReleaseNotesConfig {
  var headerFormat: String = Constants.RELEASE_NOTES_CONFIG_HEADER_FORMAT_DEFAULT_VALUE
  var maxLinesOfChangelog: Int = Constants.RELEASE_NOTES_CONFIG_MAX_LINES_OF_CHANGELOG_DEFAULT_VALUE
  var changelogFormat: String = Constants.RELEASE_NOTES_CONFIG_CHANGELOG_FORMAT_DEFAULT_VALUE
  @Suppress("MemberVisibilityCanBePrivate")
  var template: String = Constants.RELEASE_NOTES_CONFIG_TEMPLATE_DEFAULT_VALUE

  internal fun getReleaseNotes(versionName: String, header: String, changelog: String): String = template
    .replace(Constants.FILE_TEMPLATE_VERSION_KEY, versionName, true)
    .replace(Constants.FILE_TEMPLATE_HEADER_KEY, header, true)
    .replace(Constants.FILE_TEMPLATE_CHANGELOG_KEY, changelog, true)
}
