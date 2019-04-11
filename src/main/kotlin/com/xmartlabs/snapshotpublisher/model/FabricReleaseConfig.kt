package com.xmartlabs.snapshotpublisher.model

import com.xmartlabs.snapshotpublisher.Constants

open class FabricReleaseConfig {
  var distributionEmails: String? = Constants.FABRIC_DEPLOY_DISTRIBUTION_EMAILS_DEFAULT_VALUE
  var distributionGroupAliases: String? = Constants.FABRIC_DEPLOY_DISTRIBUTION_GROUP_ALIASES_DEFAULT_VALUE
  var distributionNotifications: Boolean = Constants.FABRIC_DEPLOY_DISTRIBUTION_NOTIFICATIONS_DEFAULT_VALUE
}
