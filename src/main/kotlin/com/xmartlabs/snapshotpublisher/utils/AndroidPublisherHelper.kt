package com.xmartlabs.snapshotpublisher.utils

import com.android.build.gradle.api.ApplicationVariant
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
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
      variant: ApplicationVariant,
      project: Project,
      block: AndroidPublisher.Edits.(editId: String) -> Unit
  ) {
    val edits = publisher.edits()
    val id = try {
      edits.insert(variant.applicationId, null).execute().id
    } catch (e: GoogleJsonResponseException) {
      if (e has "applicationNotFound") {
        if (skipIfNotFound) {
          return
        } else {
          // Rethrow for clarity
          throw IllegalArgumentException(
              "No application found for the package name ${variant.applicationId}. " +
                  "The first version of your app must be uploaded via the " +
                  "Play Store console.", e)
        }
      } else if (e has "editAlreadyCommitted") {
        project.logger.info("Failed to retrieve saved edit.")
        return read(skipIfNotFound, publisher, variant, project, block)
      } else if (e.statusCode == UNAUTHORIZED_ERROR_CODE) {
        throw IllegalArgumentException("Service account not authenticated", e)
      } else {
        throw e
      }
    }
    edits.block(id)
  }

  // https://github.com/Triple-T/gradle-play-publisher/blob/4d3f98128c8c86bc1ea37fd34d8f4b16dbf93d1b/plugin/src/main/kotlin/com/github/triplet/gradle/play/tasks/internal/PublishingApi.kt
  @Suppress("MagicNumber")
  internal fun buildPublisher(googlePlayConfig: GooglePlayConfig): AndroidPublisher {
    val transport = buildTransport()
    val creds = googlePlayConfig.serviceAccountCredentials
    val factory = JacksonFactory.getDefaultInstance()

    val credential = GoogleCredential.fromStream(creds?.inputStream(), transport, factory)
        .createScoped(listOf(AndroidPublisherScopes.ANDROIDPUBLISHER))

    return AndroidPublisher.Builder(transport, JacksonFactory.getDefaultInstance()) { request ->
      credential.initialize(request.apply {
        readTimeout = 300_000
        connectTimeout = 300_000
      })
    }.setApplicationName(Constants.PLUGIN_NAME).build()
  }

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
