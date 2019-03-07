package com.xmartlabs.snapshotpublisher

import java.io.File

internal object Constants {
  const val FABRIC_BETA_SNAPSHOT_DEPLOY_TASK_NAME = "publishSnapshotFabric"
  const val GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME = "publishSnapshotGooglePlay"
  const val PREPARE_FABRIC_BETA_SNAPSHOT_DEPLOY_TASK_NAME = "preparePublishSnapshotFabric"
  const val PREPARE_GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME = "preparePublishSnapshotGooglePlay"

  const val PLUGIN_NAME = "Android Snapshot Publisher"
  const val PLUGIN_GROUP = "Snapshot Publishing"

  const val SNAPSHOT_PUBLISHER_EXTENSION_NAME = "snapshotPublisher"
  const val UPDATE_ANDROID_VERSION_NAME_TASK_NAME = "updateAndroidVersionName"
  const val GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME = "generateSnapshotReleaseNotes"

  private const val OUTPUT_PATH = "snapshot-publisher"
  const val RESOURCES_OUTPUT_PATH = "generated/$OUTPUT_PATH"
  const val OUTPUT_RELEASE_NOTES_FILE_NAME = "releaseNotes.txt"
  const val OUTPUT_RELEASE_NOTES_FILE_PATH = "$RESOURCES_OUTPUT_PATH/$OUTPUT_RELEASE_NOTES_FILE_NAME"

  const val VERSION_FORMAT_CURRENT_VERSION_NAME_KEY = "{currentVersionName}"
  const val VERSION_FORMAT_COMMIT_HASH_KEY = "{commitHash}"
  const val VERSION_FORMAT = "$VERSION_FORMAT_CURRENT_VERSION_NAME_KEY-$VERSION_FORMAT_COMMIT_HASH_KEY"

  const val RELEASE_NOTES_COMMIT_HISTORY_KEY = "{commitHistory}"
  const val RELEASE_NOTES_HEADER_KEY = "{header}"
  const val RELEASE_NOTES_VERSION_CODE_KEY = "{versionCode}"
  const val RELEASE_NOTES_VERSION_KEY = "{version}"
  const val RELEASE_NOTES_VERSION_NAME_KEY = "{versionName}"

  const val RELEASE_NOTES_VERSION_FORMAT_DEFAULT_VALUE = RELEASE_NOTES_VERSION_NAME_KEY
  const val RELEASE_NOTES_CONFIG_COMMIT_HISTORY_FORMAT_DEFAULT_VALUE = "• %s (%an - %ci)"
  const val RELEASE_NOTES_CONFIG_HEADER_FORMAT_DEFAULT_VALUE = "%s%n%nAuthor: %an <%ae>"
  const val RELEASE_NOTES_CONFIG_MAX_COMMIT_HISTORY_LINES_DEFAULT_VALUE = 10
  const val RELEASE_NOTES_INCLUDE_MERGE_COMMITS_DEFAULT_VALUE: Boolean = true
  val RELEASE_NOTES_OUTPUT_FILE_DEFAULT_VALUE: File? = null
  const val RELEASE_NOTES_CONFIG_FORMAT_DEFAULT_VALUE =
      """$RELEASE_NOTES_VERSION_KEY: $RELEASE_NOTES_HEADER_KEY

Last Changes:
$RELEASE_NOTES_COMMIT_HISTORY_KEY
"""

  val FABRIC_DEPLOY_DISTRIBUTION_EMAILS_DEFAULT_VALUE = listOf<String>()
  val FABRIC_DEPLOY_DISTRIBUTION_GROUP_ALIASES_DEFAULT_VALUE = listOf<String>()
  const val FABRIC_DEPLOY_DISTRIBUTION_NOTIFICATIONS_DEFAULT_VALUE = true
}
