package com.nia.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var chat: TextView
    private lateinit var input: EditText
    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech

    private val memory by lazy {
        getSharedPreferences("nia_memory", MODE_PRIVATE)
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        tts = TextToSpeech(this, this)

        ui()

        add(
            "NIA",
            "Hi! I’m NIA. Try: “Hey NIA, open YouTube”, “set a reminder”, " +
                    "“remember Rahul is my brother”, or “who is Rahul?”"
        )
    }

    private fun ui() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 30, 22, 18)
            setBackgroundColor(Color.rgb(7, 10, 16))
        }

        val title = TextView(this).apply {
            text = "NIA"
            textSize = 34f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        status = TextView(this).apply {
            text = "● Ready"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, 14)
        }

        chat = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(18, 23, 34))
        }

        val scroll = ScrollView(this).apply {
            addView(chat)
        }

        root.addView(title)
        root.addView(status)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        input = EditText(this).apply {
            hint = "Talk to NIA..."
            hintTextColor = Color.GRAY
            setTextColor(Color.WHITE)
            setSingleLine(true)
        }

        val send = Button(this).apply {
            text = "Send"
            setOnClickListener {
                respond(input.text.toString())
            }
        }

        val mic = Button(this).apply {
            text = "🎙"
            setOnClickListener {
                listen()
            }
        }

        row.addView(
            input,
            LinearLayout.LayoutParams(0, 62, 1f)
        )

        row.addView(
            send,
            LinearLayout.LayoutParams(92, 62)
        )

        row.addView(
            mic,
            LinearLayout.LayoutParams(72, 62)
        )

        root.addView(row)

        val wakeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val startWake = Button(this).apply {
            text = "🎤 Start Hey NIA"
            setOnClickListener {
                startWakeService()
            }
        }

        val stopWake = Button(this).apply {
            text = "⏹ Stop"
            setOnClickListener {
                stopWakeService()
            }
        }

        wakeRow.addView(
            startWake,
            LinearLayout.LayoutParams(0, 58, 1f)
        )

        wakeRow.addView(
            stopWake,
            LinearLayout.LayoutParams(0, 58, 1f)
        )

        root.addView(wakeRow)

        // =========================================================
        // SMART ROUTINE BUTTON
        // =========================================================

        val routineButton = Button(this).apply {
            text = "⚙ Smart Routine"

            setOnClickListener {
                startActivity(
                    Intent(
                        this@MainActivity,
                        RoutineActivity::class.java
                    )
                )
            }
        }

        root.addView(
            routineButton,
            LinearLayout.LayoutParams(
                -1,
                58
            ).apply {
                topMargin = 10
            }
        )

        setContentView(root)
    }

    private fun add(who: String, msg: String) {
        chat.append("\n$who: $msg\n")
    }

    private fun speak(msg: String) {

        add("NIA", msg)

        if (::tts.isInitialized) {
            tts.speak(
                msg,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "nia"
            )
        }
    }

    private fun respond(text: String) {

        val q = text.trim()

        if (q.isEmpty()) return

        add("You", q)

        input.setText("")

        val s = q
            .lowercase(Locale.getDefault())
            .removePrefix("hey nia")
            .trim()

        /*
         * MEMORY
         */

        val remember = Regex(
            """(?:remember|save|note)\s+(?:that\s+)?(.+?)\s+is\s+(?:my\s+)?(.+)"""
        ).find(s)

        if (remember != null) {
            val person = remember.groupValues[1].trim()
            val relation = remember.groupValues[2].trim()

            memory.edit()
                .putString("person_$person", relation)
                .apply()

            speak(
                "Okay, I’ll remember that $person is your $relation."
            )

            return
        }

        if (s.startsWith("forget ")) {
            val person = s
                .removePrefix("forget ")
                .trim()

            memory.edit()
                .remove("person_$person")
                .apply()

            speak(
                "I removed $person from my local memory."
            )

            return
        }

        val who = Regex(
            """(?:who is|who's|what is my relation with|tell me about)\s+(.+?)[?!.]*$"""
        ).find(s)

        if (who != null) {
            val person = who.groupValues[1].trim()

            val relation = memory.getString(
                "person_$person",
                null
            )

            speak(
                if (relation != null) {
                    "$person is your $relation."
                } else {
                    "I don’t have $person saved yet."
                }
            )

            return
        }

        if (
            s.contains("what do you remember") ||
            s.contains("my memories")
        ) {
            val saved = memory
                .all
                .filterKeys {
                    it.startsWith("person_")
                }

            speak(
                if (saved.isEmpty()) {
                    "I don’t have any saved people."
                } else {
                    saved.entries.joinToString(". ") {
                        it.key.removePrefix("person_") +
                                " is your " +
                                it.value
                    }
                }
            )

            return
        }

        /*
         * EXISTING COMMANDS
         */

        when {

            s.contains("what time") ||
                    s == "time" -> {

                speak(
                    "It is " +
                            SimpleDateFormat(
                                "hh:mm a",
                                Locale.getDefault()
                            ).format(Date())
                )

                return
            }

            s.contains("today's date") ||
                    s == "date" ||
                    s.contains("what date") -> {

                speak(
                    "Today is " +
                            SimpleDateFormat(
                                "dd MMMM yyyy",
                                Locale.getDefault()
                            ).format(Date())
                )

                return
            }

            s.startsWith("search ") ||
                    s.startsWith("google ") -> {

                val term = s
                    .substringAfter(" ")
                    .trim()

                openUrl(
                    "https://www.google.com/search?q=" +
                            Uri.encode(term)
                )

                speak(
                    "Searching Google for $term."
                )

                return
            }

            s.contains("open youtube") -> {
                openUrl("https://www.youtube.com")
                speak("Opening YouTube.")
                return
            }

            s.contains("open google") -> {
                openUrl("https://www.google.com")
                speak("Opening Google.")
                return
            }

            s.contains("open whatsapp") -> {
                openUrl("https://web.whatsapp.com")
                speak("Opening WhatsApp.")
                return
            }

            s.contains("open instagram") -> {
                openUrl("https://www.instagram.com")
                speak("Opening Instagram.")
                return
            }

            s.contains("open gmail") -> {
                openUrl("https://mail.google.com")
                speak("Opening Gmail.")
                return
            }

            s.contains("open settings") -> {
                startActivity(
                    Intent(Settings.ACTION_SETTINGS)
                )
                speak("Opening Android settings.")
                return
            }

            s.contains("open bluetooth") -> {
                startActivity(
                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                )
                speak("Opening Bluetooth settings.")
                return
            }

            s.contains("open wifi") -> {
                startActivity(
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                )
                speak("Opening Wi-Fi settings.")
                return
            }

            s.contains("set alarm") ||
                    s.contains("alarm") -> {
                startActivity(
                    Intent(android.provider.AlarmClock.ACTION_SET_ALARM)
                )
                speak("Opening the alarm screen.")
                return
            }

            s.contains("set timer") ||
                    s.contains("timer") -> {
                startActivity(
                    Intent(android.provider.AlarmClock.ACTION_SET_TIMER)
                )
                speak("Opening the timer.")
                return
            }

            s.contains("open camera") -> {
                try {
                    startActivity(
                        Intent("android.media.action.IMAGE_CAPTURE")
                    )
                    speak("Opening camera.")
                } catch (e: Exception) {
                    speak(
                        "I couldn’t open the camera."
                    )
                }
                return
            }

            s.contains("open calculator") -> {
                try {
                    startActivity(
                        Intent(Intent.ACTION_MAIN)
                            .addCategory(
                                Intent.CATEGORY_APP_CALCULATOR
                            )
                    )
                    speak("Opening calculator.")
                } catch (e: Exception) {
                    speak(
                        "Calculator isn’t available."
                    )
                }
                return
            }

            s.contains("open maps") ||
                    s.contains("navigate") -> {
                openUrl(
                    "https://maps.google.com"
                )
                speak("Opening Maps.")
                return
            }

            s.contains("call ") -> {
                val number = s
                    .substringAfter("call ")
                    .trim()

                if (
                    number.matches(
                        Regex("[0-9+ ()-]{5,}")
                    )
                ) {
                    startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse(
                                "tel:" +
                                        Uri.encode(number)
                            )
                        )
                    )
                    speak("Opening the dialer.")
                } else {
                    speak(
                        "Tell me a phone number, " +
                                "for example: call 9876543210."
                    )
                }
                return
            }

            s.contains("help") ||
                    s.contains("what can you do") -> {
                speak(
                    "I can open apps and websites, " +
                            "search Google, open settings, " +
                            "camera, maps, alarm and timer, " +
                            "tell time and date, remember people, " +
                            "and connect to my AI service."
                )
                return
            }
        }

        /*
         * AI FALLBACK
         */

        status.text = "● Thinking…"

        AiClient.ask(q) { reply ->
            runOnUiThread {
                status.text = "● Ready"
                speak(reply)
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        } catch (e: Exception) {
            speak("I couldn’t open that.")
        }
    }

    private fun listen() {

        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                10
            )
            return
        }

        status.text = "● Listening…"

        val intent = Intent(
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
                RecognizerIntent.EXTRA_PROMPT,
                "Talk to NIA"
            )
        }

        startActivityForResult(
            intent,
            20
        )
    }

    private fun startWakeService() {

        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                10
            )
            return
        }

        try {
            val intent = Intent(
                this,
                NiaWakeService::class.java
            )

            startForegroundService(intent)

            status.text = "● Hey NIA active"

            add(
                "NIA",
                "Hey NIA listening mode started."
            )

        } catch (e: Exception) {
            speak(
                "I couldn't start Hey NIA listening mode."
            )
        }
    }

    private fun stopWakeService() {

        try {
            val intent = Intent(
                this,
                NiaWakeService::class.java
            )

            stopService(intent)

            status.text = "● Ready"

            add(
                "NIA",
                "Hey NIA listening mode stopped."
            )

        } catch (e: Exception) {
            speak(
                "I couldn't stop Hey NIA listening mode."
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        status.text = "● Ready"

        if (
            requestCode == 20 &&
            resultCode == RESULT_OK
        ) {
            val results =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            if (!results.isNullOrEmpty()) {
                respond(results[0])
            }
        }
    }

    override fun onInit(result: Int) {
        if (
            result ==
            TextToSpeech.SUCCESS
        ) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}
