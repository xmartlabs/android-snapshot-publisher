package com.xmartlabs.snapshotpublisher.matchers

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class MustRunAfterMatcher extends TypeSafeMatcher<Task> {
  static mustRunAfter(String mustRunAfterTaskName) {
    return new MustRunAfterMatcher(mustRunAfterTaskName)
  }

  final mustRunAfterTaskName

  MustRunAfterMatcher(mustRunAfterTaskName) {
    this.mustRunAfterTaskName = mustRunAfterTaskName
  }

  private boolean mustRunAfterTaskTask(Object object) {
    if (object instanceof TaskProvider) {
      def task = (object as TaskProvider).get()
      println(task.name)
      return task.name == mustRunAfterTaskName || task.mustRunAfter.getDependencies().any { mustRunAfterTaskTask(it) }
    }

    if (object instanceof Task) {
      def task = object as Task
      println(task.name)
      return task.name == mustRunAfterTaskName || task.mustRunAfter.getDependencies().any { mustRunAfterTaskTask(it) }
    }
    println(object)

    return object instanceof String && object == mustRunAfterTaskName
  }

  @Override
  protected boolean matchesSafely(Task task) {
    return task.mustRunAfter.getDependencies().any { mustRunAfterTaskTask(task) }
  }

  @Override
  void describeTo(Description description) {
    description.appendText('Task to must run after to').appendValue(mustRunAfterTaskName)
  }

  @Override
  void describeMismatchSafely(Task item, Description description) {
    description.appendText('doesn\'t')
  }
}
