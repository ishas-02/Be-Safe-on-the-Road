//package com.example.safedrivemonitor
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.speech.tts.TextToSpeech
//import android.speech.tts.Voice
//import android.view.MotionEvent
//import android.view.View
//import android.widget.*
//import androidx.activity.ComponentActivity
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.app.ActivityCompat
//import com.google.gson.Gson
//import kotlinx.coroutines.*
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody.Companion.toRequestBody
//import java.io.IOException
//import java.util.Locale
//
//class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
//
//    private lateinit var status: TextView
//    private lateinit var serverIp: EditText
//    private lateinit var riskBar: SeekBar
//    private lateinit var btnSpeakRisk: Button
//    private lateinit var btnMic: Button
//    private lateinit var answerText: TextView
//    private lateinit var btnDownloadReport: Button
//    private lateinit var langSpinner: Spinner
//    private lateinit var voiceSpinner: Spinner
//
//    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//    private val client = OkHttpClient()
//    private val gson = Gson()
//
//    private var tts: TextToSpeech? = null
//    private var ttsReady = false
//    private var selectedLang: Locale = Locale.US
//    private var selectedVoice: Voice? = null
//    private var recorder: SimpleVoskStt? = null
//
//    private val micPerm = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { granted ->
//        if (!granted) Toast.makeText(this, "Mic permission needed", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // UI references
//        status = findViewById(R.id.status)
//        serverIp = findViewById(R.id.serverIp)
//        riskBar = findViewById(R.id.riskBar)
//        btnSpeakRisk = findViewById(R.id.btnSpeakRisk)
//        btnMic = findViewById(R.id.btnMic)
//        answerText = findViewById(R.id.answerText)
//        btnDownloadReport = findViewById(R.id.btnDownloadReport)
//        langSpinner = findViewById(R.id.langSpinner)
//        voiceSpinner = findViewById(R.id.voiceSpinner)
//
//        // Init TTS
//        tts = TextToSpeech(this, this)
//
//        // Backend default URL (update with your server IP)
//        serverIp.setText("http://192.168.1.207:8000")
//
//        // Risk alert button
//        btnSpeakRisk.setOnClickListener {
//            val level = riskBar.progress
//            speakRisk(level)
//        }
//
//        // Mic button (hold to talk)
//        btnMic.setOnTouchListener { _, ev ->
//            when (ev.action) {
//                MotionEvent.ACTION_DOWN -> startStt()
//                MotionEvent.ACTION_UP -> stopStt()
//            }
//            true
//        }
//
//        // Download report button
//        btnDownloadReport.setOnClickListener { downloadReport() }
//
//        // Init Vosk (offline STT)
//        recorder = SimpleVoskStt(this, "vosk-model-small-en-us-0.15")
//
//        // Setup spinners
//        setupLanguageSpinner()
//        setupVoiceSpinner()
//    }
//
//    // ----------------------------
//    // Text-to-Speech setup
//    // ----------------------------
//    override fun onInit(statusCode: Int) {
//        ttsReady = statusCode == TextToSpeech.SUCCESS
//        if (ttsReady) {
//            // Default language = English US
//            selectedLang = Locale.US
//            tts?.language = selectedLang
//        }
//    }
//
//    private fun setupLanguageSpinner() {
//        val langs = listOf("English", "Spanish")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, langs)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        langSpinner.adapter = adapter
//
//        langSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
//                selectedLang = if (langs[pos] == "Spanish") Locale("es", "ES") else Locale.US
//                if (ttsReady) tts?.language = selectedLang
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//    }
//
//    private fun setupVoiceSpinner() {
//        val voices = listOf("Female", "Male")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, voices)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        voiceSpinner.adapter = adapter
//
//        voiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
//                if (ttsReady) {
//                    val available = tts?.voices?.filter { it.locale.language == selectedLang.language }
//                    selectedVoice = if (voices[pos] == "Male") {
//                        available?.firstOrNull { it.name.contains("male", true) }
//                    } else {
//                        available?.firstOrNull { it.name.contains("female", true) }
//                    }
//                    if (selectedVoice != null) tts?.voice = selectedVoice
//                }
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//    }
//
//    private fun speakText(text: String) {
//        if (ttsReady) {
//            tts?.language = selectedLang
//            if (selectedVoice != null) tts?.voice = selectedVoice
//            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt")
//        }
//    }
//
//    // ----------------------------
//    // Risk Alert -> Backend
//    // ----------------------------
//    private fun speakRisk(level: Int) {
//        val base = serverIp.text.toString().trim()
//        val url = "$base/risk_alert"
//        val payload = gson.toJson(mapOf("risk_level" to level))
//        val body = payload.toRequestBody("application/json; charset=utf-8".toMediaType())
//
//        val req = Request.Builder().url(url).post(body).build()
//        client.newCall(req).enqueue(object: Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                scope.launch { status.text = "Risk alert failed: ${e.message}" }
//            }
//            override fun onResponse(call: Call, res: Response) {
//                res.use {
//                    val msg = gson.fromJson(it.body?.string() ?: "{}", Map::class.java)
//                        .get("message")?.toString().orEmpty()
//                    scope.launch {
//                        status.text = "Risk L$level → $msg"
//                        speakText(msg)
//                    }
//                }
//            }
//        })
//    }
//
//    // ----------------------------
//    // STT & Ask Assistant
//    // ----------------------------
//    private fun startStt() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//            != PackageManager.PERMISSION_GRANTED) {
//            micPerm.launch(Manifest.permission.RECORD_AUDIO); return
//        }
//        status.text = "Listening…"
//        recorder?.start { text ->
//            scope.launch {
//                status.text = "Heard: $text"
//                askQuestion(text)
//            }
//        }
//    }
//
//    private fun stopStt() {
//        recorder?.stop()
//        status.text = "Stopped listening"
//    }
//
//    private fun askQuestion(q: String) {
//        val base = serverIp.text.toString().trim()
//        val url = "$base/ask"
//        val payload = gson.toJson(mapOf("query" to q))
//        val body = payload.toRequestBody("application/json; charset=utf-8".toMediaType())
//
//        val req = Request.Builder().url(url).post(body).build()
//        client.newCall(req).enqueue(object: Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                scope.launch { status.text = "Ask failed: ${e.message}" }
//            }
//            override fun onResponse(call: Call, res: Response) {
//                res.use {
//                    val ans = gson.fromJson(it.body?.string() ?: "{}", Map::class.java)
//                        .get("answer")?.toString().orEmpty()
//                    scope.launch {
//                        answerText.text = ans
//                        speakText(ans)
//                    }
//                }
//            }
//        })
//    }
//
//    // ----------------------------
//    // Report Download
//    // ----------------------------
//    private fun downloadReport() {
//        val base = serverIp.text.toString().trim()
//        val url = "$base/report"
//        val req = Request.Builder().url(url).get().build()
//        client.newCall(req).enqueue(object: Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                scope.launch { status.text = "Report download failed: ${e.message}" }
//            }
//            override fun onResponse(call: Call, res: Response) {
//                res.use {
//                    val bytes = it.body?.bytes()
//                    if (bytes != null) {
//                        val file = getExternalFilesDir(null)?.resolve("trip_report.pdf")
//                        file?.writeBytes(bytes)
//                        scope.launch {
//                            status.text = "Saved report: ${file?.absolutePath}"
//                            Toast.makeText(this@MainActivity,
//                                "Saved: ${file?.absolutePath}", Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//            }
//        })
//    }
//
//    override fun onDestroy() {
//        recorder?.release()
//        tts?.shutdown()
//        scope.cancel()
//        super.onDestroy()
//    }
//}

package com.example.safedrivemonitor

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.appbar.MaterialToolbar
import androidx.viewpager2.widget.ViewPager2
import android.view.Menu
import android.view.MenuItem
import java.util.Locale

/**
 * MainActivity:
 * Hosts the tabbed UI (Dashboard / Assistant / Report)
 * and manages Text-to-Speech, voice, and backend settings.
 */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    var selectedLang: Locale = Locale.US
    var selectedGender: String = "Female"
    private var selectedVoice: Voice? = null

    // persisted server URL (editable from settings)
    var baseUrl: String
        get() = getSharedPreferences("app", MODE_PRIVATE)
            .getString("baseUrl", "http://192.168.1.207:8000")!!
        set(v) {
            getSharedPreferences("app", MODE_PRIVATE)
                .edit().putString("baseUrl", v).apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar) // ✅ fixed

        val pager = findViewById<ViewPager2>(R.id.pager)
        pager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) { // ✅ fixed
            override fun getItemCount() = 3
            override fun createFragment(pos: Int) = when (pos) {
                0 -> DashboardFragment()
                1 -> AssistantFragment()
                else -> ReportsFragment()
            }
        }

        val tabs = findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabs)
        TabLayoutMediator(tabs, pager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Dashboard"
                1 -> "Assistant"
                else -> "Report"
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            SettingsBottomSheet().show(supportFragmentManager, "settings") // ✅ now valid
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) tts.language = selectedLang
    }

    fun applySettings(newUrl: String, langCode: String, gender: String) {
        baseUrl = newUrl
        selectedLang = if (langCode == "es") Locale("es", "ES") else Locale.US
        selectedGender = gender

        if (ttsReady) tts.language = selectedLang

        val voices = tts.voices?.filter {
            it.locale.language == selectedLang.language
        } ?: emptyList()

        selectedVoice =
            if (gender == "Male") voices.firstOrNull { it.name.contains("male", true) }
            else voices.firstOrNull { it.name.contains("female", true) }

        selectedVoice?.let { tts.voice = it }
    }

    fun speakText(text: String) {
        if (!ttsReady) return
        tts.language = selectedLang
        selectedVoice?.let { tts.voice = it }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt")
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
