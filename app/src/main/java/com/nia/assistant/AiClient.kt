package com.nia.assistant

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object AiClient {

    private const val BACKEND_URL = BuildConfig.NIA_BACKEND_URL

    fun ask(
        message: String,
        callback: (String) -> Unit
    ) {
        if (BACKEND_URL.isBlank()) {
            callback(
                "AI backend abhi configure nahi hai. " +
                "Pehle NIA ka secure AI backend connect karna hoga."
            )
            return
        }

        Thread {
            var connection: HttpURLConnection? = null

            try {
                connection =
                    URL(BACKEND_URL).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                val requestBody = JSONObject()
                    .put("message", message)
                    .toString()

                connection.outputStream.use { output ->
                    output.write(requestBody.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode

                val stream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val responseText =
                    stream?.bufferedReader()?.use { it.readText() }
                        ?: ""

                if (responseCode !in 200..299) {
                    callback(
                        "AI service se response nahi mila. " +
                        "Server error: $responseCode"
                    )
                    return@Thread
                }

                val json = JSONObject(responseText)

                val reply = json.optString(
                    "reply",
                    "AI ne koi response nahi diya."
                )

                callback(reply)

            } catch (e: Exception) {

                callback(
                    "AI service se connect nahi ho paaya. " +
                    "Internet ya backend connection check karo."
                )

            } finally {
                connection?.disconnect()
            }
        }.start()
    }
}
