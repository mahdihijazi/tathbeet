package com.quran.tathbeet.sync

import java.net.URI
import java.net.URLDecoder

fun resolveAuthLink(rawLink: String?): String? {
    val normalizedLink = rawLink?.trim().orEmpty()
    if (normalizedLink.isBlank()) {
        return null
    }

    val uri = runCatching { URI(normalizedLink) }.getOrNull() ?: return normalizedLink
    return when (uri.scheme?.lowercase()) {
        "http",
        "https",
        -> uri.queryParameter("link")?.let { decoded ->
            resolveAuthLink(decoded) ?: decoded
        } ?: normalizedLink

        "tathbeet" -> uri.queryParameter("emailLink")

        else -> null
    }
}

private fun URI.queryParameter(name: String): String? {
    val rawQuery = rawQuery ?: return null
    return rawQuery
        .split("&")
        .asSequence()
        .mapNotNull { pair ->
            val index = pair.indexOf('=')
            when {
                index < 0 && URLDecoder.decode(pair, Charsets.UTF_8.name()) == name ->
                    ""

                index < 0 -> null

                URLDecoder.decode(pair.substring(0, index), Charsets.UTF_8.name()) == name ->
                    URLDecoder.decode(pair.substring(index + 1), Charsets.UTF_8.name())

                else -> null
            }
        }
        .firstOrNull()
        ?.takeIf { it.isNotEmpty() }
}
