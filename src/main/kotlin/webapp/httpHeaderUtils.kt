package webapp

/**
 * Utility file for HTTP headers creation.
 */

import org.springframework.http.HttpHeaders
import java.io.UnsupportedEncodingException
import java.net.URLEncoder.encode
import kotlin.text.Charsets.UTF_8

/**
 *
 * createAlert.
 *
 * @param applicationName a [java.lang.String] object.
 * @param message a [java.lang.String] object.
 * @param param a [java.lang.String] object.
 * @return a [org.springframework.http.HttpHeaders] object.
 */
private fun HttpHeaders.createAlert(
    applicationName: String,
    message: String?,
    param: String?
): HttpHeaders = apply {
    add("X-$applicationName-alert", message)
    try {
        add(
            "X-$applicationName-params",
            encode(param, UTF_8)
        )
    } catch (_: UnsupportedEncodingException) {
        // StandardCharsets are supported by every Java implementation
        // so this exceptions will never happen
    }
}

/**
 *
 * createEntityCreationAlert.
 *
 * @param applicationName a [java.lang.String] object.
 * @param enableTranslation a boolean.
 * @param entityName a [java.lang.String] object.
 * @param param a [java.lang.String] object.
 * @return a [org.springframework.http.HttpHeaders] object.
 */
fun HttpHeaders.createEntityCreationAlert(
    applicationName: String,
    enableTranslation: Boolean,
    entityName: String,
    param: String
): HttpHeaders = createAlert(
    applicationName,
    when {
        enableTranslation -> "$applicationName.$entityName.created"
        else -> "A new $entityName is created with identifier $param"
    },
    param
)

/**
 *
 * createEntityUpdateAlert.
 *
 * @param applicationName a [java.lang.String] object.
 * @param enableTranslation a boolean.
 * @param entityName a [java.lang.String] object.
 * @param param a [java.lang.String] object.
 * @return a [org.springframework.http.HttpHeaders] object.
 */
fun HttpHeaders.createEntityUpdateAlert(
    applicationName: String,
    enableTranslation: Boolean,
    entityName: String,
    param: String
): HttpHeaders = createAlert(
    applicationName,
    when {
        enableTranslation -> "$applicationName.$entityName.updated"
        else -> "A $entityName is updated with identifier $param"
    },
    param
)

/**
 *
 * createEntityDeletionAlert.
 *
 * @param applicationName a [java.lang.String] object.
 * @param enableTranslation a boolean.
 * @param entityName a [java.lang.String] object.
 * @param param a [java.lang.String] object.
 * @return a [org.springframework.http.HttpHeaders] object.
 */
fun HttpHeaders.createEntityDeletionAlert(
    applicationName: String,
    enableTranslation: Boolean,
    entityName: String,
    param: String
): HttpHeaders = createAlert(
    applicationName,
    when {
        enableTranslation -> "$applicationName.$entityName.deleted"
        else -> "A $entityName is deleted with identifier $param"
    },
    param
)

/**
 *
 * createFailureAlert.
 *
 * @param applicationName a [java.lang.String] object.
 * @param enableTranslation a boolean.
 * @param entityName a [java.lang.String] object.
 * @param errorKey a [java.lang.String] object.
 * @param defaultMessage a [java.lang.String] object.
 * @return a [org.springframework.http.HttpHeaders] object.
 */
fun HttpHeaders.createFailureAlert(
    applicationName: String,
    enableTranslation: Boolean,
    entityName: String?,
    errorKey: String,
    defaultMessage: String?
): HttpHeaders = apply {
    e("Entity processing failed, {}", defaultMessage)
    add(
        "X-$applicationName-error",
        when {
            enableTranslation -> "error.$errorKey"
            else -> defaultMessage!!
        }
    )
    add("X-$applicationName-params", entityName)
}