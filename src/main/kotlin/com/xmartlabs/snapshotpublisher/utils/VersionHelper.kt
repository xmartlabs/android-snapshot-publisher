package com.xmartlabs.snapshotpublisher.utils

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant

internal object VersionHelper {
  private fun getUpdatedVersionName(currentVersionName: String, suffix: String?) =
      currentVersionName + if (suffix.isNullOrBlank()) "" else "-$suffix"

  private fun getUpdatedVersionName(android: AppExtension, suffix: String?) =
      getUpdatedVersionName(android.defaultConfig.versionName, suffix)

  private fun getUpdatedVersionName(variant: ApplicationVariant, suffix: String?) =
      getUpdatedVersionName(variant.versionName, suffix)

  fun getUpdatedVersionName(android: AppExtension, variant: ApplicationVariant?, suffix: String?) =
      if (variant == null) {
        getUpdatedVersionName(android, suffix)
      } else {
        getUpdatedVersionName(variant, suffix)
      }
}
