package com.xmartlabs.snapshotpublisher

import com.xmartlabs.snapshotpublisher.utils.ProjectCreator
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.Test

import static com.xmartlabs.snapshotpublisher.matchers.DependsOnMatcher.dependsOn
import static com.xmartlabs.snapshotpublisher.matchers.MustRunAfterMatcher.mustRunAfter
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertThat

class TaskDependenciesTest {
  @Test
  void testCreateDefaultTask() {
    def project = ProjectCreator.mockProject()
    project.evaluate()

    assertNotNull(getGooglePlaySnapshotTask(project))
  }

  @Test
  void testGenerateReleaseNotesTaskMustBeRunBeforeUpdateVersionName() {
    def project = ProjectCreator.mockProject()
    project.evaluate()

    assertThat(getUpdateVersionNameTask(project), mustRunAfter(Constants.GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME+ ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
  }

  @Test
  void testPublishApkTaskDependsOnAssembleTask() {
    def project = ProjectCreator.mockProject()
    project.evaluate()


    assertThat(getGooglePlaySnapshotTask(project), dependsOn("assemble$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE"))
  }

  @Test
  void testPublishBundleTaskDependsOnBundleTask() {
    def project = ProjectCreator.mockProject(true)
    project.evaluate()


    assertThat(getGooglePlaySnapshotTask(project), dependsOn("bundle$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE"))
    def assembleTask = project.tasks.getByName("assemble$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
    assertThat(assembleTask, mustRunAfter(Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
  }

  @Test
  void testGooglePlayPublishDependencies() {
    def project = ProjectCreator.mockProject(false)
    project.evaluate()

    def googlePlayPublishTask = getGooglePlaySnapshotTask(project)
    assertThat(googlePlayPublishTask, dependsOn(Constants.GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
    assertThat(googlePlayPublishTask, dependsOn(Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
    assertThat(googlePlayPublishTask, dependsOn(Constants.PREPARE_GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
    assertThat(googlePlayPublishTask, dependsOn("assemble$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE"))
  }

  @Test
  void testPrepareBundleTaskDependencies() {
    def project = ProjectCreator.mockProject(false)
    project.evaluate()

    def preparationTask = getPrepareBundleSnapshotTask(project)
    assertThat(preparationTask, dependsOn(Constants.GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
    assertThat(preparationTask, dependsOn(Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
    assertThat(preparationTask, dependsOn("bundle$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE"))
  }

  @Test
  void testPrepareApkTaskDependencies() {
    def project = ProjectCreator.mockProject(false)
    project.evaluate()

    def preparationTask = getPrepareApkSnapshotTask(project)
    assertThat(preparationTask, dependsOn(Constants.GENERATE_SNAPSHOT_RELEASE_NOTES_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
    assertThat(preparationTask, dependsOn(Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
    assertThat(preparationTask, dependsOn("assemble$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE"))
  }

  @Test
  void testUpdateAndroidNameTaskMustBeRunBeforeCompilationTasks() {
    def project = ProjectCreator.mockProject(false)
    project.evaluate()

    def assembleTask = project.tasks.getByName("assemble$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
    assertThat(assembleTask, mustRunAfter(Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))

    project = ProjectCreator.mockProject(true)
    project.evaluate()
    def bundleTask = project.tasks.getByName("bundle$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
    assertThat(bundleTask, mustRunAfter(Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME + ProjectCreator.FLAVOUR_WITH_BUILD_TYPE))
  }

  private static Task getUpdateVersionNameTask(Project project) {
    project.tasks.getByName("$Constants.UPDATE_ANDROID_VERSION_NAME_TASK_NAME$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
  }

  private static Task getGooglePlaySnapshotTask(Project project) {
    project.tasks.getByName("$Constants.GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
  }

  private static Task getPrepareApkSnapshotTask(Project project) {
    project.tasks.getByName("$Constants.PREPARE_APK_VERSION_TASK_NAME$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
  }

  private static Task getPrepareBundleSnapshotTask(Project project) {
    project.tasks.getByName("$Constants.PREPARE_BUNDLE_VERSION_TASK_NAME$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
  }
}
