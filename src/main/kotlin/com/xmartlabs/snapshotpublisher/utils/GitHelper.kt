package com.xmartlabs.snapshotpublisher.utils

import java.io.File
import java.util.concurrent.TimeUnit

internal object GitHelper {
  private fun String.execute(dir: File? = File(".")): String {
    val parts = this.trim().split(" (?=([^\']*\'[^\']*\')*[^\']*$)".toRegex())
        .map { it.replace("'", "") }

    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(dir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(5, TimeUnit.SECONDS)
    return proc.inputStream.bufferedReader().readText().trim()
  }

  fun getCommitHash() = "git rev-parse --short HEAD".execute()

  fun getLog(format: String, range: String) = "git log --pretty=format:'$format' $range".execute()

  fun getLog(format: String, numberOfCommits: Int = Int.MAX_VALUE) =
      "git log --pretty=format:'$format' -n $numberOfCommits".execute()

  private fun getNumberOfCommits() = "git rev-list --count HEAD".execute().toInt()

  fun getLogRange(maxLinesOfChangelog: Int): String {
    val numberOfCommits = GitHelper.getNumberOfCommits()
    val maxLinesOfCommits = maxLinesOfChangelog + 1
    return (if (numberOfCommits <= maxLinesOfCommits) "" else "HEAD~$maxLinesOfCommits..") + "HEAD^"
  }
}
