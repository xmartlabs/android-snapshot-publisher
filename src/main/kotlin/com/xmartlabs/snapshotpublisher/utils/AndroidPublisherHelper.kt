package com.xmartlabs.snapshotpublisher.utils

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.xmartlabs.snapshotpublisher.Constants
import com.xmartlabs.snapshotpublisher.model.GooglePlayConfig
import org.gradle.api.Project
import java.io.FileInputStream
import java.security.KeyStore

private const val UNAUTHORIZED_ERROR_CODE = 401

private infix fun GoogleJsonResponseException.has(error: String) =
    details?.errors.orEmpty().any { it.reason == error }

internal object AndroidPublisherHelper {
  @Suppress("ComplexMethod", "ThrowsCount")
  fun read(
      skipIfNotFound: Boolean = false,
      publisher: AndroidPublisher,
      applicationId: String,
      project: Project,
      block: AndroidPublisher.Edits.(editId: String) -> Unit
  ) {
    val edits = publisher.edits()
    val id = try {
      edits.insert(applicationId, null).execute().id
    } catch (e: GoogleJsonResponseException) {
      if (e has "applicationNotFound") {
        if (skipIfNotFound) {
          return
        } else {
          throw IllegalArgumentException(
              "No application found for the package name $applicationId. " +
                  "The first version of your app must be uploaded via the " +
                  "Play Store console.", e)
        }
      } else if (e has "editAlreadyCommitted") {
        project.logger.info("Failed to retrieve saved edit.")
        return read(skipIfNotFound, publisher, applicationId, project, block)
      } else if (e.statusCode == UNAUTHORIZED_ERROR_CODE) {
        throw IllegalArgumentException("Service account not authenticated", e)
      } else {
        throw e
      }
    }
    edits.block(id)
  }

  @Suppress("MagicNumber")
  internal fun buildPublisher(project: Project, googlePlayConfig: GooglePlayConfig): AndroidPublisher {
    val transport = buildTransport()
    val creds = getCreds(googlePlayConfig, project)

    val credentials = GoogleCredentials.fromStream(creds?.inputStream())
        .createScoped(listOf(AndroidPublisherScopes.ANDROIDPUBLISHER))
    val requestInitializer = HttpCredentialsAdapter(credentials)

    return AndroidPublisher.Builder(transport, GsonFactory.getDefaultInstance()) { request ->
      requestInitializer.initialize(request)
      request.readTimeout = 300_000
      request.connectTimeout = 300_000
    }.setApplicationName(Constants.PLUGIN_NAME).build()
  }

  private fun getCreds(googlePlayConfig: GooglePlayConfig, project: Project) =
      googlePlayConfig.serviceAccountCredentials?.let { project.file(it) }

  private fun buildTransport(): NetHttpTransport {
    val trustStore: String? = System.getProperty("javax.net.ssl.trustStore", null)
    val trustStorePassword: String? =
        System.getProperty("javax.net.ssl.trustStorePassword", null)

    return if (trustStore == null) {
      GoogleNetHttpTransport.newTrustedTransport()
    } else {
      val ks = KeyStore.getInstance(KeyStore.getDefaultType())
      FileInputStream(trustStore).use { fis ->
        ks.load(fis, trustStorePassword?.toCharArray())
      }
      NetHttpTransport.Builder().trustCertificates(ks).build()
    }
  }
}
