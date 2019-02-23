package com.xmartlabs.snapshotpublisher.utils

import java.io.File
import java.util.concurrent.TimeUnit

internal object GitHelper {
  private const val SECONDS_TO_WAIT_COMMAND_RESPONSE: Long = 10
  private fun String.execute(dir: File? = File(".")): String {
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

  fun getHistoryFromPreviousCommit(format: String, maxLinesOfChangelog: Int, includeMergeCommits: Boolean): String {
    val allowMergeCommitCommandArg = if (includeMergeCommits) "" else "--no-merges"
    val numberOfCommits = GitHelper.getTotalNumberOfCommits("HEAD^", allowMergeCommitCommandArg)
    val maxLinesOfCommits = maxLinesOfChangelog + 1
    println("number $numberOfCommits max $maxLinesOfChangelog")
    val requireCommitsCommandArg = if (numberOfCommits < maxLinesOfCommits) "" else " -n ${maxLinesOfCommits - 1}"
    return "git log --pretty=format:'$format'$requireCommitsCommandArg HEAD^ $allowMergeCommitCommandArg".execute()
  }
}
