# android-snapshot-publisher
[![CircleCI](https://circleci.com/gh/xmartlabs/snapshot-publisher.svg?style=svg)](https://circleci.com/gh/xmartlabs/snapshot-publisher)

Android Snapshot Publisher is a Gradle plugin to **prepare and distribute Android Snapshot versions** to multiple distribution sources in a common way.

The main features of the preparation process are:
- Update the Android Version Name to keep track of the distributed versions.
The default behaviour adds the commit identifier to the Android Version name.
It's very helpful to track possible issues.
- Create release notes based on the git history.
It prepares rich release notes which are flexible and customizable.

The second purpose of this plugin is to give the ability of deploying the snapshot builds in a distribution source easily.

Currently the available sources are:
- Google Play
- Fabric Beta

An interesting feature of this plugin is that it enables you to deploy the build in multiple sources with a low effort setup.

## Installation

The plugin is hosted in the Gradle Plugin Portal.
```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
     classpath "com.xmartlabs:snapshot-publisher:0.0.1"
  }
}
```
Apply the plugin to each individual `com.android.application` module where you want to use it.

```groovy
apply plugin: 'com.xmartlabs.snapshot-publisher'
```

## Setup
The plugin defines a `snapshotPublisher` block where you can add the different setup alongside the android modules.

```groovy
snapshotPublisher {
    version {
        // Version customization
    }
    releaseNotes {
        // Release notes customization
    }
    fabric {
        // Fabric Beta setup
    }
    googlePlay {
        // Google Play setup
    }
}
```

### Version customizations
The version name block allows you to perform version customizations.
The field in that block is optional and its default values is:

```groovy
snapshotPublisher {
    version {
        versionNameFormat = '{currentVersionName}-{commitHash}'
    }
    // ...
}
```

- `versionNameFormat` defines the Android Version Name for the delivered build.
    The default value is the current version name and the short-hash commit joined by a hyphen.
    
    The possible variables for this value are:
    - `{currentVersionName}`: The current version name.
    - `{commitHash}`: The hash id of the current git commit in the short format.

    This value can be updated using these optional variables. 
    For example, if you want to keep the current version name and the hash commit and add a custom suffix such as `-SNAPSHOT`, you must assign `'{currentVersionName}-{commitHash}-SNAPSHOT'` to the `versionNameFormat`.

### Release notes
The release notes block allows you to perform build release notes customizations.
All fields in that block are optional and their default values are:

```groovy
snapshotPublisher {
    releaseNotes {
        releaseNotesFormat = """{version}: - {header}
    
Last Changes:
{changelog}
"""
        versionFormat: '{versionName}'
        headerFormat = '%s%n%nAuthor: %an <%ae>%n%B'
        changelogFormat = '• %s (%an - %ci)'
        maxChangelogLines = 10
        outputFile = null
    }
    // ...
}
```

- `releaseNotesFormat`: Defines the format of the release notes:
    The possible variables to play with in this case are:
    - `{version}` given by `versionFormat`.
    - `{header}` given by `headerFormat`.
    - `{changelog}` given by `changelogFormat`.

- `versionFormat`: Specifies the version's variable format.
    
    `{versionName}` (android version name) and `{versionCode}` (android version code) can be used to create it

- `headerFormat`: Specifies the header's variable format.
The plugin uses [Git's pretty format] to be able to retrieve the information about the current commit.
If you want to modify it, you may use the same tool to do it.

- `changelogFormat`: Specifies the changelog's variable format.
As `headerFormat` does, it uses [Git's pretty format] to create the changelog for the previous commits.
This changelog includes all commits from the last -not current- commit to `maxChangelogLines` commits before that.

- `maxChangelogLines`: Indicates the number of commits included in `{changelog}`.

- `outputFile`: The file where the release notes will be saved.
By default this value is `null` and that means that the release notes will be generated and delivered with the snapshot build but it will not saved in the local storage.
If you want to save the release notes in the local storage, you can set `outputFile = file("release-notes.txt")`.


### Fabric Beta
This block defines the configuration needed to deploy the artifacts in Fabric's Beta.
This plugin uses [Fabric's beta plugin](https://docs.fabric.io/android/beta/gradle.html), so to be able to release you must have added the Fabric `ApiKey` in the application manifest and the `apiSecret` in the `fabric.properties` file.
For more information about that you can read [Fabric's setup guide](https://docs.fabric.io/android/fabric/settings/api-keys.html#).

Here too, all of the block's fields are optional:

```groovy
snapshotPublisher {
    fabric {
        distributionEmails = ''
        distributionGroupAliases = ''
        distributionNotifications = true
    }
    // ...
}
```

- `distributionEmails`: The email addresses of those who'll get the release.
- `distributionGroupAliases`: The names (aliases) of the groups defined inside Fabric's Beta that will get the release.
- `distributionNotifications`: If set to `true`, all of the build's recipients will get an email notification about its release.


### Google Play

This block defines the configuration needed to deploy the artifacts in Google Play.
This plugin uses [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher).

```groovy
snapshotPublisher {
    googlePlay {
       serviceAccountCredentials = file("your-key.json")
       track = "internal"
       releaseStatus = "completed"
       defaultToAppBundles = false
       resolutionStrategy = "auto"
    }
    // ...
}
```

The only required field is `serviceAccountCredentials`
To be able to release to Google Play you must create a service account with access to the Play Developer API.
To do that, you can [follow this guide](https://guides.codepath.com/android/automating-publishing-to-the-play-store).

- `serviceAccountCredentials`: contains the service account JSON file with your private key.
- `track`: refers to the Google play tracks. The possible tracks are `internal`, `alpha`, `beta` and `production`.
- `releaseStatus`: the type of the release. The possible values are `completed`, `draft`, `inProgress`, `halted`.
- `defaultToAppBundles`: if set to `true`, the plugin will generate an [App Bundle](https://developer.android.com/platform/technology/app-bundle/) instead of an APK.  
- `resolutionStrategy`: defines the strategy that will take place if a build with the same version code already exists in Google Play (this will throw an error). 
The possible values are `ignore` (it will ignore the error and continue) and `auto` (it will automatically increase the version code for you).

## How to use it?


The plugin defines some tasks to can be ran.
The naming convention is as follows: [action][Variant][Thing]. For example, `publishSnapshotGooglePlayStagingRelease` will be generated if the app has a `staging` flavor and `release` build type.

To find available tasks, run ./gradlew tasks and look under the publishing section.

The available tasks are:
- `publishSnapshotFabric`: it'll publish a snapshot version in Fabric's Beta.   
- `publishSnapshotGooglePlay`: it'll publish a snapshot version in Google play.
- `publishSnapshot`: it'll publish a snapshot version in all defined distribution sources.

## Getting involved

* If you **want to contribute** please feel free to **submit pull requests**.
* If you **have a feature request** please **open an issue**.
* If you **found a bug** check older issues before submitting a new one.

**Before contributing, please check the [CONTRIBUTING](.github/CONTRIBUTING.md) file.**

## About
Made with ❤️ by [XMARTLABS](http://xmartlabs.com)

[Git's pretty format]: https://git-scm.com/docs/pretty-formats
 