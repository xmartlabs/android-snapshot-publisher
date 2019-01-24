package com.xmartlabs.snapshotpublisher.model

import org.gradle.api.Action

open class SnapshotReleaseExtension {
  var version: VersionConfig = VersionConfig()
  val fabric: FabricReleaseConfig = FabricReleaseConfig()
  val releaseNotes: ReleaseNotesConfig = ReleaseNotesConfig()

  @Suppress("unused")
  fun fabric(action: Action<in FabricReleaseConfig>) = action.execute(fabric)

  @Suppress("unused")
  fun releaseNotes(action: Action<in ReleaseNotesConfig>) = action.execute(releaseNotes)

  @Suppress("unused")
  fun version(action: Action<in VersionConfig>) = action.execute(version)
}
