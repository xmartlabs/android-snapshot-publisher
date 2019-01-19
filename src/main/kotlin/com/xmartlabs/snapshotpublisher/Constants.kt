package com.xmartlabs.snapshotpublisher

object Constants {
  const val FILE_TEMPLATE_VERSION_KEY = "{version}"
  const val FILE_TEMPLATE_HEADER_KEY = "{header}"
  const val FILE_TEMPLATE_CHANGELOG_KEY = "{changelog}"

  const val ASSEMBLE_TASK_NAME = "assemble"
  const val BETA_DISTRIBUTION_TASK_NAME = "crashlyticsUploadDistribution"
  const val FABRIC_BETA_SNAPSHOT_DEPLOY_TASK_NAME = "fabricBetaSnapshotDeploy"
  const val GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME = "generateSnapshotReleaseNotes"
  const val SNAPSHOT_PUBLISHER_EXTENSION_NAME = "snapshotPublisher"
  const val UPDATE_ANDROID_VERSION_NAME_TASK_NAME = "updateAndroidVersionName"

  const val RELEASE_NOTES_CONFIG_CHANGELOG_FORMAT_DEFAULT_VALUE = "• %s (%an - %ci)"
  const val RELEASE_NOTES_CONFIG_HEADER_FORMAT_DEFAULT_VALUE = "%s%n%nAuthor: %an <%ae>%n%B"
  const val RELEASE_NOTES_CONFIG_MAX_LINES_OF_CHANGELOG_DEFAULT_VALUE = 10
  const val RELEASE_NOTES_CONFIG_TEMPLATE_DEFAULT_VALUE =
      """${Constants.FILE_TEMPLATE_VERSION_KEY}: - ${Constants.FILE_TEMPLATE_HEADER_KEY}

Last Changes:
${Constants.FILE_TEMPLATE_CHANGELOG_KEY}
"""

  const val SNAPSHOT_RELEASE_USE_HASH_COMMIT_IN_VERSION_NAME_DEFAULT_VALUE = true

  const val FABRIC_DEPLOY_DISTRIBUTION_EMAILS_DEFAULT_VALUE = ""
  const val FABRIC_DEPLOY_DISTRIBUTION_GROUP_ALIASES_DEFAULT_VALUE = ""
  const val FABRIC_DEPLOY_DISTRIBUTION_NOTIFICATIONS_DEFAULT_VALUE = true
}
