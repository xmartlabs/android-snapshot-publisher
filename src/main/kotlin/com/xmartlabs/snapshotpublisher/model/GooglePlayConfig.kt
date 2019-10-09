package com.xmartlabs.snapshotpublisher.model

import org.gradle.api.Project

open class GooglePlayConfig {
  var serviceAccountCredentials: String? = null
  var track = "internal"
  var releaseStatus = "completed"
  var defaultToAppBundles = false
  var resolutionStrategy = "auto"

  fun areCredsValid(project: Project): Boolean {
    val serviceAccountCredentials = serviceAccountCredentials ?: return false
    val serviceAccountCredentialsFile = project.file(serviceAccountCredentials)
    return serviceAccountCredentialsFile.exists() &&
        serviceAccountCredentialsFile.extension.equals("json", true)
  }
}
