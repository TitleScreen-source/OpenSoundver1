package com.opensound.app.data

import java.util.Base64

object StorageTextCodec {
    fun encode(value: String): String {
        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.toByteArray(Charsets.UTF_8))
    }

    fun decode(value: String): String? {
        return runCatching {
            String(
                Base64.getUrlDecoder().decode(padded(value)),
                Charsets.UTF_8
            )
        }.getOrNull()
    }

    private fun padded(value: String): String {
        val paddingLength = (4 - value.length % 4) % 4
        return value + "=".repeat(paddingLength)
    }
}
