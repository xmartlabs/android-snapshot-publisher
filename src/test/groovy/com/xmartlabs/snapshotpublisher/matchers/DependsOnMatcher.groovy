package com.xmartlabs.snapshotpublisher.matchers

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class DependsOnMatcher extends TypeSafeMatcher<Task> {
  static dependsOn(String dependsOnTaskName) {
    return new DependsOnMatcher(dependsOnTaskName)
  }

  final dependsOnTaskName

  DependsOnMatcher(dependsOnTaskName) {
    this.dependsOnTaskName = dependsOnTaskName
  }

  private boolean dependsOnTask(Object object) {
    if (object instanceof TaskProvider) {
      def task = (object as TaskProvider).get()
      return task.name == dependsOnTaskName || task.dependsOn.any { dependsOnTask(it) }
    }

    if (object instanceof Task) {
      def task = object as Task
      return task.name == dependsOnTaskName || task.dependsOn.any { dependsOnTask(it) }
    }

    return object instanceof String && object == dependsOnTaskName
  }

  @Override
  protected boolean matchesSafely(Task task) {
    return task.dependsOn.any { dependsOnTask(task) }
  }

  @Override
  void describeTo(Description description) {
    description.appendText('Task to depend on ').appendValue(dependsOnTaskName)
  }

  @Override
  void describeMismatchSafely(Task item, Description description) {
    description.appendText('doesn\'t')
  }
}
