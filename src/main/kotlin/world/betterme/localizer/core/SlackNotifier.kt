package world.betterme.localizer.core

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class SlackNotifier(private val webHookUrl: String) {

    fun sendSlackMessage(message: String) {
        val url = URL(webHookUrl)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val jsonPayload = """{"text": "$message"}"""

            connection.outputStream.use { outputStream: OutputStream ->
                outputStream.write(jsonPayload.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                println("Slack message sent successfully!")
            } else {
                println("Failed to send Slack message. Response code: $responseCode")
            }
        } catch (e: Exception) {
            println("Error sending Slack message: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }
}