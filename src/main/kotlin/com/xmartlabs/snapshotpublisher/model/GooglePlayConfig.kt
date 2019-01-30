package com.xmartlabs.snapshotpublisher.model

import java.io.File

open class GooglePlayConfig {
  var serviceAccountCredentials: File? = null
  var track = "internal"
  var releaseStatus = "completed"
  var defaultToAppBundles = false
  var resolutionStrategy = "auto"

  fun areCredsValid(): Boolean {
    val creds = serviceAccountCredentials ?: return false
    return creds.extension.equals("json", true)
  }
}
