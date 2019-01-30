package com.xmartlabs.snapshotpublisher.model

import org.gradle.api.Action

open class SnapshotReleaseExtension {
  val version: VersionConfig = VersionConfig()
  val fabric: FabricReleaseConfig = FabricReleaseConfig()
  val releaseNotes: ReleaseNotesConfig = ReleaseNotesConfig()
  val googlePlay: GooglePlayConfig = GooglePlayConfig()

  @Suppress("unused")
  fun fabric(action: Action<in FabricReleaseConfig>) = action.execute(fabric)

  @Suppress("unused")
  fun googlePlay(action: Action<in GooglePlayConfig>) = action.execute(googlePlay)

  @Suppress("unused")
  fun releaseNotes(action: Action<in ReleaseNotesConfig>) = action.execute(releaseNotes)

  @Suppress("unused")
  fun version(action: Action<in VersionConfig>) = action.execute(version)
}
