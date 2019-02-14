package com.xmartlabs.snapshotpublisher

import com.xmartlabs.snapshotpublisher.utils.ProjectCreator
import org.gradle.api.Project
import org.gradle.api.Task
import static org.junit.Assert.assertNotNull
import static com.xmartlabs.snapshotpublisher.matchers.DependsOnMatcher.dependsOn
import static com.xmartlabs.snapshotpublisher.matchers.MustRunAfterMatcher.mustRunAfter
import org.junit.Test

import static org.junit.Assert.assertThat

class TaskDependenciesTest {
  @Test
  void testCreateDefaultTask() {
    def project = ProjectCreator.mockProject()
    project.evaluate()

    assertNotNull(getFabricSnapshotTask(project))
    assertNotNull(getGooglePlaySnapshotTask(project))
  }

  @Test
  void testPublishApkTaskDependsOnAssembleTask() {
    def project = ProjectCreator.mockProject()
    project.evaluate()


    assertThat(getFabricSnapshotTask(project), dependsOn("assemble$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE"))
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

  private static Task getFabricSnapshotTask(Project project) {
    project.tasks.getByName("$Constants.FABRIC_BETA_SNAPSHOT_DEPLOY_TASK_NAME$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
  }

  private static Task getGooglePlaySnapshotTask(Project project) {
    project.tasks.getByName("$Constants.GOOGLE_PLAY_SNAPSHOT_DEPLOY_TASK_NAME$ProjectCreator.FLAVOUR_WITH_BUILD_TYPE")
  }
}
