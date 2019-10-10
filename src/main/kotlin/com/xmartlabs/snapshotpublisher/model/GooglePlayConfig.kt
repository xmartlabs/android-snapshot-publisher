package com.xmartlabs.snapshotpublisher.model

open class GooglePlayConfig {
  var serviceAccountCredentials: String? = null
  var track = "internal"
  var releaseStatus = "completed"
  var defaultToAppBundles = false
  var resolutionStrategy = "auto"
}
