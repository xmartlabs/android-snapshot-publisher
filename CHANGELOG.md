Change Log
==========
All notable changes to this project will be documented in this file.

[Version 2.4.0] _(2021-10-06)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v2.4.0)
- Change min Android Gradle plugin gradle version to `7.0.0`.
- Upgrade Google Play Publisher dependency to [v3.6.0](https://github.com/Triple-T/gradle-play-publisher/releases/tag/3.6.0).
- Update Core libraries

[Version 2.3.0-AGP7.0 _(2021-05-18)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v2.3.0-AGP7.0)
- Upgrade Google Play Publisher dependency to [v3.4.0-agp7.0](https://github.com/Triple-T/gradle-play-publisher/releases/tag/3.4.0-agp7.0).
- Change min Android Gradle plugin gradle version to `7.0.0-beta01`.

[Version 2.3.0 _(2021-05-18)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v2.3.0)
- Upgrade Google Play Publisher dependency to [v3.4.0-agp4.2](https://github.com/Triple-T/gradle-play-publisher/releases/tag/3.4.0-agp4.2).
- Change min Android Gradle plugin gradle version to `4.2.0`.

[Version 2.2.0 _(2020-10-26)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v2.2.0)
- Upgrade Google Play Publisher dependency to [v3.0.0](https://github.com/Triple-T/gradle-play-publisher/releases/tag/3.0.0).
- Change min required gradle version to `6.5.0`.
- Change min Android Gradle plugin gradle version to `4.1.0`.

### Breaking changes
- After AGP 4.1 the version name cannot be applied only to specific plugin tasks. In turn, it's applied in the configuration step, causing all builds to contain `versionNameFormat`. AGP 4.2 fixes this issue but is still in alpha. If you're using a lower version of AGP, you can use the version of [2.1.0](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v2.1.0).

---

[Version 2.1.0 _(2020-01-31)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v2.1.0)
---

### Changes
- Upgrade Google Play Publisher dependency to [v2.6.2](https://github.com/Triple-T/gradle-play-publisher/releases/tag/2.6.2).
- Change min required gradle version to 6.0.


[Version 2.0.0 _(2019-10-15)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v2.0.0)
---

### Changes
#### Firebase app distribution migration
Google deprecated and migrated [Fabric Craslytics Beta](https://get.fabric.io/roadmap) to [Firebase app distribution](https://firebase.google.com/docs/app-distribution).  
The [documentation](README.md#firebase-app-distribution) specifies the required changes to use the new tool. 

Migration:

Change plugin dependency setup:
- Version 1x:
```groovy
buildscript {
  repositories {
    maven { url "https://plugins.gradle.org/m2/" }
    maven { url 'https://maven.fabric.io/public' } // Remove it
  }
  dependencies {
     classpath "com.xmartlabs:snapshot-publisher:1.0.4" // Replace version
  }
}
```
- Version 2x:
```groovy
buildscript {
  repositories {
    gradlePluginPortal()
    google() // Add it
  }
  dependencies {
     classpath "com.xmartlabs:snapshot-publisher:2.0.0" // Replace version
  }
}
```

Change the previous `fabric` block to the new `firebaseAppDistribution` block:

- Version 1x:
```groovy
snapshotPublisher {
    fabric {
        distributionEmails = "mail@xmartlabs.com"
        distributionGroupAliases = "tester-group"
        distributionNotifications = true // Must be deleted
    }
    // ...
}
```

- Version 2x:
```groovy
snapshotPublisher {
    firebaseAppDistribution {
        distributionEmails =  "mail@xmartlabs.com"
        distributionGroupAliases = "tester-group"
        appId = null // New parameter
        serviceAccountCredentials = "/path/to/your-service-account-key.json" // new parameter
    }
    // ...
}
```
- `appId`: Your app's Firebase App ID.
Required only if you don't have the google services gradle plugin installed.
You can find the App ID in the google-services.json file or in the Firebase console on the General Settings page.
The value in your build.gradle file overrides the value output from the google-services gradle plugin.
- `serviceAccountCredentials`: The path to your service account private key JSON file.
To release to Firebase you must create a Google service account with Firebase Quality Admin role.
If you don't have a service account, you can create one following [this guide](https://firebase.google.com/docs/app-distribution/android/distribute-gradle#authenticate_using_a_service_account).

### Breaking changes
- Fabric Craslytics Beta integration was removed
- Google Play's service credential account file path was changed to a credential file path.

Migration:
- Version 1x:
```groovy
snapshotPublisher {
    googlePlay {
       serviceAccountCredentials = file("/path/to/your-service-account-key.json")
    }
    // ...
}
```
- version 2x:
```groovy
snapshotPublisher {
    googlePlay {
       serviceAccountCredentials = "/path/to/your-service-account-key.json"
    }
    // ...
}
```

[Version 1.0.4 _(2019-09-20)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.4)
---

### Changes
- Upgrade Google Play publisher plugin version to 2.4.1 (#36)
- Upgrade Fabric plugin version to 1.31.1 (#37)

[Version 1.0.3 _(2019-05-20)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.3)
---

### Fixes
- Fix release notes issue when previous commit was a merge commit (#31)

[Version 1.0.2 _(2019-04-30)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.2)
---

### New Features
- Allow to use the `debuggable` build types in Fabric's Beta. (#30)

[Version 1.0.1 _(2019-04-17)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.1)
---

### New Features
- Added new `history` variable to use in `releaseNotesFormat` given by `historyFormat`.
It's shown only if the `{commitHistory}` is not empty. (#25)
- Added `includeHistorySinceLastTag` release notes setup variable.
It enables to generate the history only for the commits after the latest git's tag.  
It's useful to show only the changes that changed since the last build. (#25)

### Fixes
- Fix version name issue in generated release notes. (#24)

[Version 1.0.0 _(2019-04-12)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.0)
---

### New Features
- Add the ability to include the last commit in the release notes history. (#16)
- Create preparation tasks.
It enables you to create snapshot builds and test them locally without the necessity of deploy it. (#17)
- Include git branch name in the app custom version name options (#18)

### Fixes
- Truncate Fabric Beta's release notes if its length is greater than `16384` characters. (#21)

### Breaking changes
- `distributionEmails` and `distributionGroupAliases` in Fabric's Beta configuration block are changed to strings instead of a list of strings.
These values are built by joining all `emails` or `aliases` with commas.
This change was made in order to make the setup process easier. (#19)

[Version 0.0.1 _(2019-03-08)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v0.0.1)
---

This is the initial version.
