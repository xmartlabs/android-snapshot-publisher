package com.xmartlabs.snapshotpublisher

import com.xmartlabs.snapshotpublisher.model.VersionConfig
import com.xmartlabs.snapshotpublisher.plugin.AndroidPluginHelper
import com.xmartlabs.snapshotpublisher.utils.GitHelper
import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.revwalk.RevCommit
import org.gradle.internal.impldep.org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals

class VersionNameTest {
  companion object {
    private val REPO_FOLDER = File("/tmp/${UUID.randomUUID()}/")
    private val REPO_GIT_FOLDER = File("${REPO_FOLDER.absoluteFile}/.git")
    private const val BRANCH_NAME = "test-branch"
  }

  lateinit var headCommit: RevCommit

  @Before
  fun setup() {
    val newlyCreatedRepo = FileRepositoryBuilder.create(
        REPO_GIT_FOLDER
    )
    newlyCreatedRepo.create()

    headCommit = Git(newlyCreatedRepo)
        .commit()
        .setMessage("Test message")
        .setAllowEmpty(true)
        .call()

    Git(newlyCreatedRepo)
        .checkout()
        .setCreateBranch(true)
        .setName(BRANCH_NAME)
        .call()

    GitHelper.defaultDir = REPO_FOLDER
  }

  @After
  fun finish() {
    if (REPO_FOLDER.exists()) REPO_FOLDER.deleteRecursively()
  }

  @Test
  fun `Test version name with commit hash`() {
    val versionConfig = VersionConfig()
        .apply { versionNameFormat = "{commitHash}" }

    val versionName = AndroidPluginHelper.getVersionName(versionConfig, "")

    assertEquals(headCommit.shortCommitHash, versionName)
  }

  @Test
  fun `Test version name with branch name`() {
    val versionConfig = VersionConfig()
        .apply { versionNameFormat = "{branchName}" }

    val versionName = AndroidPluginHelper.getVersionName(versionConfig, "")

    assertEquals(BRANCH_NAME, versionName)
  }

  @Test
  fun `Test version name with commit hash and branch name`() {
    val versionConfig = VersionConfig()
        .apply { versionNameFormat = "{commitHash}{branchName}" }

    val versionName = AndroidPluginHelper.getVersionName(versionConfig, "")

    assertEquals(headCommit.shortCommitHash + BRANCH_NAME, versionName)
  }

  @Test
  fun `Test version name with branch name, commit hash and a custom string`() {
    val versionConfig = VersionConfig()
        .apply { versionNameFormat = "v{branchName}-{commitHash}-SNAPSHOT" }

    val versionName = AndroidPluginHelper.getVersionName(versionConfig, "")

    assertEquals("v$BRANCH_NAME-${headCommit.shortCommitHash}-SNAPSHOT", versionName)
  }

  @Test
  fun `Test version name with current version name, commit hash, branch name and a custom string`() {
    val versionConfig = VersionConfig()
        .apply { versionNameFormat = "v{currentVersionName}{commitHash}!{branchName}-SNAPSHOT" }

    val currentVersionName = "current-version"
    val versionName = AndroidPluginHelper.getVersionName(versionConfig, currentVersionName)

    val expected = "v$currentVersionName${headCommit.shortCommitHash}!$BRANCH_NAME-SNAPSHOT"
    assertEquals(expected, versionName)
  }
}

private val RevCommit.shortCommitHash: String
  get() = name.substring(0, 7)
