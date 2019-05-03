package com.xmartlabs.snapshotpublisher.utils

import com.android.annotations.VisibleForTesting
import com.xmartlabs.snapshotpublisher.model.ReleaseNotesConfig
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

  private fun getPreviousTag() = "git describe --tags --abbrev=0 HEAD^".execute()

  private fun getPreviousCommitHash() = "git log  --format=\"%H\" --skip 1 -1".execute()

  fun getCommitHash() = "git rev-parse --short HEAD".execute()

  fun getBranchName() = "git rev-parse --abbrev-ref HEAD".execute()

  fun getLog(format: String, numberOfCommits: Int = Int.MAX_VALUE) =
      "git log --pretty=format:'$format' -n $numberOfCommits".execute()

  private fun getTotalNumberOfCommits(logRange: String, commandArg: String?) =
      "git rev-list --count $logRange ${commandArg ?: ""}".execute().toInt()

  private fun getHistoryRange(releaseNotesConfig: ReleaseNotesConfig): String {
    val startRange = if (releaseNotesConfig.includeHistorySinceLastTag) getPreviousTag() else null
    val endRange = if (releaseNotesConfig.includeLastCommitInHistory) "HEAD" else getPreviousCommitHash()
    return if (startRange.isNullOrBlank()) endRange else "$startRange..$endRange"
  }

  fun getHistoryFromPreviousCommit(releaseNotesConfig: ReleaseNotesConfig): String {
    with(releaseNotesConfig) {
      val allowMergeCommitCommandArg = if (includeMergeCommitsInHistory) "" else "--no-merges"

      val logRange = getHistoryRange(this)
      val numberOfCommits = GitHelper.getTotalNumberOfCommits(logRange, allowMergeCommitCommandArg)
      val requireCommitsCommandArg = if (numberOfCommits <= maxCommitHistoryLines) "" else " -n $maxCommitHistoryLines"
      val gitLogCommand = "git log --pretty=format:'$commitHistoryFormat'$requireCommitsCommandArg $logRange"

      return "$gitLogCommand $allowMergeCommitCommandArg".execute()
    }
  }
}
