package com.xmartlabs.snapshotpublisher

import com.xmartlabs.snapshotpublisher.model.ReleaseNotesConfig
import com.xmartlabs.snapshotpublisher.utils.GitHelper
import com.xmartlabs.snapshotpublisher.utils.ReleaseNotesGenerator
import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.lib.PersonIdent
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Repository
import org.gradle.internal.impldep.org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import kotlin.test.assertEquals

class ReleaseNotesTest {
  companion object {
    private val AUTHOR_1 = Author("Author 1", "author1@mail.com")
    private val AUTHOR_2 = Author("Author 2", "author2@mail.com")
    private const val NUMBER_OF_COMMITS = 15
    private val COMMITS = getCommits()
    private val REPO_FOLDER = File("/tmp/${UUID.randomUUID()}/")
    private val REPO_GIT_FOLDER = File("${REPO_FOLDER.absoluteFile}/.git")
    private val COMMIT_TIMEZONE = TimeZone.getTimeZone("GMT-3:00")
    private val COMMIT_DATE = Calendar.getInstance()
      .apply {
        timeZone = COMMIT_TIMEZONE
        set(2019, Calendar.MARCH, 6, 12, 0, 0)
      }
      .time

    private fun getCommits() =
      (0 until NUMBER_OF_COMMITS).map { commitNumber ->
        Commit(
          message = "Commit number $commitNumber",
          author = if (commitNumber % 3 == 0) AUTHOR_1 else AUTHOR_2
        )
      }
  }

  data class Commit(val message: String, val author: Author)

  data class Author(val name: String, val mail: String)

  private fun Repository.addCommit(commit: Commit) {
    Git(this)
      .commit()
      .setMessage(commit.message)
      .setAuthor(commit.author.name, commit.author.mail)
      .setCommitter(PersonIdent(commit.author.name, commit.author.mail, COMMIT_DATE, COMMIT_TIMEZONE))
      .setAllowEmpty(true)
      .call()
  }

  @Before
  fun setup() {
    val newlyCreatedRepo = FileRepositoryBuilder.create(
      REPO_GIT_FOLDER
    )
    newlyCreatedRepo.create()
    COMMITS.forEach { newlyCreatedRepo.addCommit(it) }
    GitHelper.defaultDir = REPO_FOLDER
  }

  @After
  fun finish() {
    if (REPO_FOLDER.exists()) REPO_FOLDER.deleteRecursively()
  }

  @Test
  fun `Test header section`() {
    val config = ReleaseNotesConfig()
      .apply { headerFormat = "%B%n%nAuthor: %an <%ae>" }
    val generatedHeader = ReleaseNotesGenerator.getHeaderSection(config)
    val lastCommit = COMMITS.last()
    val expectedHeader = "${lastCommit.message}\n\nAuthor: ${lastCommit.author.name} <${lastCommit.author.mail}>"
    assertEquals(expectedHeader, generatedHeader)
  }

  @Test
  fun `Test version section`() {
    val config = ReleaseNotesConfig()
      .apply { versionFormat = "{versionName}({versionCode})-SNAPSHOT" }

    val generatedVersion = ReleaseNotesGenerator.getVersionSection(config, "1.1.1-ef79601", 44)

    assertEquals("1.1.1-ef79601(44)-SNAPSHOT", generatedVersion)
  }

  @Test
  fun `Test history section`() {
    val config = ReleaseNotesConfig()
      .apply {
        commitHistoryFormat = "- %s"
        maxCommitHistoryLines = 5
      }

    checkChangelogIsRight(config)
  }

  @Test
  fun `Test history section with less commits that are required`() {
    val config = ReleaseNotesConfig()
      .apply {
        commitHistoryFormat = "- %s"
        maxCommitHistoryLines = NUMBER_OF_COMMITS + 5
      }

    checkChangelogIsRight(config)
  }

  @Test
  fun `Test history section where all commits are required`() {
    val config = ReleaseNotesConfig()
      .apply {
        commitHistoryFormat = "- %s"
        maxCommitHistoryLines = NUMBER_OF_COMMITS - 1
      }

    checkChangelogIsRight(config)
  }

  @Test
  fun `Test release notes`() {
    val config = ReleaseNotesConfig()
      .apply {
        commitHistoryFormat = "%s"
        maxCommitHistoryLines = 1
        versionFormat = "{versionCode}"
        releaseNotesFormat = "{version}{header}{commitHistory}"
        headerFormat = "%an"
      }
    val versionCode = 23

    val expectedReleaseNotes = "$versionCode${COMMITS.last().author.name}${COMMITS[COMMITS.size - 2].message}"
    val generatedReleaseNotes = ReleaseNotesGenerator.generate(config, "", versionCode)
    assertEquals(expectedReleaseNotes, generatedReleaseNotes)
  }

  @Test
  fun `Test default values`() {
    val generatedReleaseNotes = ReleaseNotesGenerator.generate(ReleaseNotesConfig(), "1.1.1", 12)
    val expectedReleaseNotes = """1.1.1: Commit number 14

Author: Author 2 <author2@mail.com>

Last Changes:
• Commit number 13 (Author 2 - 2019-03-06 12:00:00 -0300)
• Commit number 12 (Author 1 - 2019-03-06 12:00:00 -0300)
• Commit number 11 (Author 2 - 2019-03-06 12:00:00 -0300)
• Commit number 10 (Author 2 - 2019-03-06 12:00:00 -0300)
• Commit number 9 (Author 1 - 2019-03-06 12:00:00 -0300)
• Commit number 8 (Author 2 - 2019-03-06 12:00:00 -0300)
• Commit number 7 (Author 2 - 2019-03-06 12:00:00 -0300)
• Commit number 6 (Author 1 - 2019-03-06 12:00:00 -0300)
• Commit number 5 (Author 2 - 2019-03-06 12:00:00 -0300)
• Commit number 4 (Author 2 - 2019-03-06 12:00:00 -0300)
"""
    println(generatedReleaseNotes)
    assertEquals(expectedReleaseNotes, generatedReleaseNotes)
  }

  private fun checkChangelogIsRight(config: ReleaseNotesConfig) {
    val libraryHistory = ReleaseNotesGenerator.getHistorySection(config)

    val realHistory = COMMITS.subList(Math.max(COMMITS.size - config.maxCommitHistoryLines - 1, 0), COMMITS.size - 1)
      .reversed()
      .joinToString("\n") {
        config.commitHistoryFormat.format(it.message)
      }
    assertEquals(libraryHistory, realHistory)
  }
}
