package com.nia.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class NiaWakeService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null

    companion object {
        private const val CHANNEL_ID = "nia_wake_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notification = createNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                notification
            )
        }

        startListening()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "NIA Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description =
                    "Keeps NIA ready for Hey NIA voice commands."
            }

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("NIA is listening")
                .setContentText(
                    "Say “Hey NIA” to talk to NIA."
                )
                .setSmallIcon(
                    android.R.drawable.ic_btn_speak_now
                )
                .setOngoing(true)
                .build()

        } else {

            Notification.Builder(this)
                .setContentTitle("NIA is listening")
                .setContentText(
                    "Say “Hey NIA” to talk to NIA."
                )
                .setSmallIcon(
                    android.R.drawable.ic_btn_speak_now
                )
                .setOngoing(true)
                .build()
        }
    }

    private fun startListening() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            stopSelf()
            return
        }

        speechRecognizer?.destroy()

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: android.os.Bundle?
                ) {
                }

                override fun onBeginningOfSpeech() {
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }

                override fun onEndOfSpeech() {
                }

                override fun onError(
                    error: Int
                ) {
                    restartListening()
                }

                override fun onResults(
                    results: android.os.Bundle?
                ) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    if (!matches.isNullOrEmpty()) {

                        val spoken =
                            matches[0]

                        handleSpeech(spoken)
                    }

                    restartListening()
                }

                override fun onPartialResults(
                    partialResults: android.os.Bundle?
                ) {
                }

                override fun onEvent(
                    eventType: Int,
                    params: android.os.Bundle?
                ) {
                }
            }
        )

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "en-IN"
                )

                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )

                putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    3
                )
            }

        speechRecognizer?.startListening(intent)
    }

    private fun handleSpeech(text: String) {

        val spoken =
            text.trim()

        if (spoken.isEmpty()) {
            return
        }

        val lower =
            spoken.lowercase()

        if (
            lower.contains("hey nia") ||
            lower.startsWith("nia ")
        ) {

            val command =
                lower
                    .replaceFirst("hey nia", "")
                    .trim()

            if (command.isNotEmpty()) {

                val intent =
                    Intent(
                        this,
                        MainActivity::class.java
                    ).apply {

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                        )

                        putExtra(
                            "nia_command",
                            command
                        )
                    }

                startActivity(intent)
            }
        }
    }

    private fun restartListening() {

        android.os.Handler(
            android.os.Looper.getMainLooper()
        ).postDelayed(
            {
                if (!isStopped) {
                    startListening()
                }
            },
            1000
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        return START_STICKY
    }

    override fun onDestroy() {

        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
