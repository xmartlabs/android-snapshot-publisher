package com.xmartlabs.snapshotpublisher.model

import java.io.File

open class FirebaseAppDistributionReleaseConfig {
  var appId: String? = null
  var distributionEmails: String? = null
  var distributionGroupAliases: String? = null
  var serviceAccountCredentials: File? = null
}
