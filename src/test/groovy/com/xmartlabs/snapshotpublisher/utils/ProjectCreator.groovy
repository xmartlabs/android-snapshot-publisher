package com.xmartlabs.snapshotpublisher.utils


import com.xmartlabs.snapshotpublisher.SnapshotPublisherPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class ProjectCreator {
  public static final String BUILD_TYPE = "Release"
  public static final String FLAVOUR = "Dev"
  public static final String FLAVOUR_WITH_BUILD_TYPE = "$FLAVOUR$BUILD_TYPE"
  private static final File SERVICE_KEY_FILE = new File("/tmp/${UUID.randomUUID()}/service_key.json")

  static Project mockProject(boolean useBundle = false) {
    SERVICE_KEY_FILE.mkdirs()
    def project = ProjectBuilder.builder().build()
    project.apply plugin: 'com.android.application'
    project.pluginManager.apply(SnapshotPublisherPlugin.class)
    project.android {
      compileSdkVersion 28

      defaultConfig {
        applicationId "com.xmartlabs.snapshotpublisher.test"
        versionCode 1
        versionName '1.0'
        minSdkVersion 28
        targetSdkVersion 28
      }

      buildTypes {
        release {
          signingConfig signingConfigs.debug
        }
      }
      flavorDimensions "env"
      productFlavors {
        dev {
          dimension "env"
        }
      }
    }
    project.snapshotPublisher {
      googlePlay {
        serviceAccountCredentials = SERVICE_KEY_FILE.path
        track = "internal"
        releaseStatus = "completed"
        defaultToAppBundles = useBundle
        resolutionStrategy = "auto"
      }
    }

    return project
  }
}
