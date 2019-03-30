package com.xmartlabs.snapshotpublisher.model

import com.xmartlabs.snapshotpublisher.Constants

open class VersionConfig {
  @Suppress("MemberVisibilityCanBePrivate")
  var versionNameFormat = Constants.VERSION_FORMAT

  internal fun getVersionName(versionName: String, hashCommit: String, branchName: String): String = versionNameFormat
      .replace(Constants.VERSION_FORMAT_CURRENT_VERSION_NAME_KEY, versionName, true)
      .replace(Constants.VERSION_FORMAT_COMMIT_HASH_KEY, hashCommit, true)
      .replace(Constants.VERSION_FORMAT_BRANCH_NAME_KEY, branchName, true)
}
