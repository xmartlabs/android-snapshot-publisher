package com.xmartlabs.snapshotpublisher.utils

import org.gradle.api.Project

object ErrorHelper {
  private fun getServiceAccountFileError(project: Project, filePath: String?) = when {
    filePath.isNullOrBlank() -> ServiceAccountError.FILE_NOT_DEFINED
    !project.file(filePath).exists() -> ServiceAccountError.FILE_DOES_NOT_EXIST
    !project.file(filePath).extension.equals("json", true) -> ServiceAccountError.FILE_MUST_HAVE_JSON_EXTENSION
    else -> null
  }

  fun getServiceAccountFileErrorMessage(project: Project, filePath: String?, configBlockName: String) =
      when (getServiceAccountFileError(project, filePath)) {
        ServiceAccountError.FILE_NOT_DEFINED ->
          "Make sure that `serviceAccountCredentials` file is defined in " +
              "`$configBlockName` plugin's config block."
        ServiceAccountError.FILE_DOES_NOT_EXIST -> "Make sure that $filePath exists."
        ServiceAccountError.FILE_MUST_HAVE_JSON_EXTENSION -> "Service account file must have json extension"
        else -> null
      }

  fun isServiceAccountCredentialFileValid(project: Project, filePath: String?) =
      getServiceAccountFileError(project, filePath) == null

  private enum class ServiceAccountError {
    FILE_NOT_DEFINED,
    FILE_DOES_NOT_EXIST,
    FILE_MUST_HAVE_JSON_EXTENSION,
  }
}