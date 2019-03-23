package com.xmartlabs.snapshotpublisher.utils

import com.android.annotations.VisibleForTesting
import java.io.File
import java.util.concurrent.TimeUnit

internal object GitHelper {
  private const val SECONDS_TO_WAIT_COMMAND_RESPONSE: Long = 10
  @VisibleForTesting
  internal var defaultDir = File(".")

  private fun String.execute(dir: File? = defaultDir): String {
    val parts = this.trim().split(" (?=([^\']*\'[^\']*\')*[^\']*$)".toRegex())
      .map { it.replace("'", "") }

    @Suppress("SpreadOperator")
    val proc = ProcessBuilder(*parts.toTypedArray())
      .directory(dir)
      .redirectOutput(ProcessBuilder.Redirect.PIPE)
      .redirectError(ProcessBuilder.Redirect.PIPE)
      .start()

    proc.waitFor(SECONDS_TO_WAIT_COMMAND_RESPONSE, TimeUnit.SECONDS)
    return proc.inputStream.bufferedReader().readText().trim()
  }

  fun getCommitHash() = "git rev-parse --short HEAD".execute()

  fun getLog(format: String, numberOfCommits: Int = Int.MAX_VALUE) =
    "git log --pretty=format:'$format' -n $numberOfCommits".execute()

  private fun getTotalNumberOfCommits(from: String = "HEAD", commandArg: String?) =
    "git rev-list --count $from ${commandArg ?: ""}".execute().toInt()

  fun getHistoryFromPreviousCommit(
      format: String,
      maxLinesOfChangelog: Int,
      includeMergeCommits: Boolean,
      includeLastCommitInHistory: Boolean
  ): String {
    val allowMergeCommitCommandArg = if (includeMergeCommits) "" else "--no-merges"
    val logStartCommand = "HEAD" + if (includeLastCommitInHistory) "" else "^"
    val numberOfCommits = GitHelper.getTotalNumberOfCommits(logStartCommand, allowMergeCommitCommandArg)
    val maxLinesOfCommits = maxLinesOfChangelog + 1
    val requireCommitsCommandArg = if (numberOfCommits < maxLinesOfCommits) "" else " -n ${maxLinesOfCommits - 1}"
    return "git log --pretty=format:'$format'$requireCommitsCommandArg $logStartCommand $allowMergeCommitCommandArg"
      .execute()
  }
}
