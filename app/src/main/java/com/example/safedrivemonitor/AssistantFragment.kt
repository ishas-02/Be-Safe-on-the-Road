////package com.example.safedrivemonitor
////
////import android.Manifest
////import android.content.pm.PackageManager
////import android.os.Bundle
////import android.view.LayoutInflater
////import android.view.MotionEvent
////import android.view.View
////import android.view.ViewGroup
////import android.widget.Button
////import android.widget.Toast
////import androidx.core.app.ActivityCompat
////import androidx.fragment.app.Fragment
////import androidx.recyclerview.widget.LinearLayoutManager
////import androidx.recyclerview.widget.RecyclerView
////import kotlinx.coroutines.*
////import okhttp3.*
////import com.google.gson.Gson
////import java.io.IOException
////
/////**
//// * AssistantFragment:
//// * Voice-based Q&A system for driver queries.
//// * Uses Vosk STT → Flask backend → TTS reply.
//// */
////class AssistantFragment : Fragment() {
////
////    private lateinit var rvChat: RecyclerView
////    private lateinit var btnMic: Button
////    private lateinit var adapter: ChatAdapter
////    private val messages = mutableListOf<ChatMessage>()
////
////    private val client = OkHttpClient()
////    private val gson = Gson()
////    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
////
////    private var vosk: SimpleVoskStt? = null
////    private var isListening = false
////
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View {
////        val view = inflater.inflate(R.layout.fragment_assistant, container, false)
////
////        rvChat = view.findViewById(R.id.rvChat)
////        btnMic = view.findViewById(R.id.btnMic)
////
////        adapter = ChatAdapter(messages)
////        rvChat.adapter = adapter
////        rvChat.layoutManager = LinearLayoutManager(requireContext())
////
////        // Initialize Vosk STT (offline model)
////        vosk = SimpleVoskStt(requireContext(), "vosk-model-small-en-us-0.15")
////
////        // Mic button: Hold to talk
////        btnMic.setOnTouchListener { _, event ->
////            when (event.action) {
////                MotionEvent.ACTION_DOWN -> startListening()
////                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopListening()
////            }
////            true
////        }
////
////        return view
////    }
////
////    /**
////     * Starts speech recognition using Vosk.
////     */
////    private fun startListening() {
////        if (ActivityCompat.checkSelfPermission(
////                requireContext(), Manifest.permission.RECORD_AUDIO
////            ) != PackageManager.PERMISSION_GRANTED
////        ) {
////            Toast.makeText(requireContext(), "Microphone permission needed", Toast.LENGTH_SHORT).show()
////            return
////        }
////
////        if (isListening) return
////        isListening = true
////        btnMic.text = "Listening… 🎧"
////        vosk?.start { recognizedText ->
////            if (recognizedText.isNotBlank()) {
////                handleQuery(recognizedText)
////            }
////        }
////    }
////
////    /**
////     * Stops listening (when mic released).
////     */
////    private fun stopListening() {
////        if (!isListening) return
////        isListening = false
////        vosk?.stop()
////        btnMic.text = "🎤 HOLD TO TALK"
////    }
////
////    /**
////     * Send recognized query → Flask backend `/ask` → get answer → speak + display.
////     */
////    private fun handleQuery(query: String) {
////        val mainActivity = activity as? MainActivity ?: return
////        adapter.addMessage(ChatMessage(query, true)) // add driver query
////
////        val url = "${mainActivity.baseUrl}/ask"
////        val payload = gson.toJson(mapOf("query" to query))
////
////        val req = Request.Builder()
////            .url(url)
////            .post(RequestBody.create(MediaType.get("application/json"), payload))
////            .build()
////
////        client.newCall(req).enqueue(object : Callback {
////            override fun onFailure(call: Call, e: IOException) {
////                scope.launch {
////                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
////                }
////            }
////
////            override fun onResponse(call: Call, response: Response) {
////                response.use {
////                    val body = it.body?.string() ?: "{}"
////                    val answer = gson.fromJson(body, Map::class.java)["answer"]?.toString()
////                        ?: "Sorry, I couldn't understand that."
////
////                    scope.launch {
////                        adapter.addMessage(ChatMessage(answer, false)) // add assistant reply
////                        rvChat.smoothScrollToPosition(messages.size - 1)
////                        mainActivity.speakText(answer) // speak it out
////                    }
////                }
////            }
////        })
////    }
////
////    override fun onDestroyView() {
////        super.onDestroyView()
////        vosk?.release()
////        scope.cancel()
////    }
////}
//
////package com.example.safedrivemonitor
////
////import android.Manifest
////import android.content.pm.PackageManager
////import android.os.Bundle
////import android.view.LayoutInflater
////import android.view.MotionEvent
////import android.view.View
////import android.view.ViewGroup
////import android.widget.Button
////import android.widget.Toast
////import androidx.core.app.ActivityCompat
////import androidx.fragment.app.Fragment
////import androidx.recyclerview.widget.LinearLayoutManager
////import androidx.recyclerview.widget.RecyclerView
////import kotlinx.coroutines.*
////import okhttp3.*
////import com.google.gson.Gson
////import java.io.IOException
////import okhttp3.MediaType.Companion.toMediaType
////import okhttp3.RequestBody.Companion.toRequestBody
////
/////**
//// * AssistantFragment:
//// * Voice-based Q&A system for driver queries.
//// * Uses Vosk STT → Flask backend → TTS reply.
//// */
////class AssistantFragment : Fragment() {
////
////    private lateinit var rvChat: RecyclerView
////    private lateinit var btnMic: Button
////    private lateinit var adapter: ChatAdapter
////    private val messages = mutableListOf<ChatMessage>()
////
////    private val client = OkHttpClient()
////    private val gson = Gson()
////    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
////
////    private var vosk: SimpleVoskStt? = null
////    private var isListening = false
////
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View {
////        val view = inflater.inflate(R.layout.fragment_assistant, container, false)
////
////        rvChat = view.findViewById(R.id.rvChat)
////        btnMic = view.findViewById(R.id.btnMic)
////
////        adapter = ChatAdapter(messages)
////        rvChat.adapter = adapter
////        rvChat.layoutManager = LinearLayoutManager(requireContext())
////
////        // Initialize Vosk STT (offline model)
////        vosk = SimpleVoskStt(requireContext(), "vosk-model-small-en-us-0.15")
////
////        // Mic button: Hold to talk
////        btnMic.setOnTouchListener { _, event ->
////            when (event.action) {
////                MotionEvent.ACTION_DOWN -> startListening()
////                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopListening()
////            }
////            true
////        }
////
////        return view
////    }
////
////    /**
////     * Starts speech recognition using Vosk.
////     */
////    private fun startListening() {
////        if (ActivityCompat.checkSelfPermission(
////                requireContext(), Manifest.permission.RECORD_AUDIO
////            ) != PackageManager.PERMISSION_GRANTED
////        ) {
////            Toast.makeText(requireContext(), "Microphone permission needed", Toast.LENGTH_SHORT).show()
////            return
////        }
////
////        if (isListening) return
////        isListening = true
////        btnMic.text = "Listening… 🎧"
////        vosk?.start { recognizedText ->
////            if (recognizedText.isNotBlank()) {
////                handleQuery(recognizedText)
////            }
////        }
////    }
////
////    /**
////     * Stops listening (when mic released).
////     */
////    private fun stopListening() {
////        if (!isListening) return
////        isListening = false
////        vosk?.stop()
////        btnMic.text = "🎤 HOLD TO TALK"
////    }
////
////    /**
////     * Send recognized query → Flask backend `/ask` → get answer → speak + display.
////     */
////    private fun handleQuery(query: String) {
////        val mainActivity = activity as? MainActivity ?: return
////        adapter.addMessage(ChatMessage(query, true)) // add driver query
////
////        val url = "${mainActivity.baseUrl}/ask"
////        val payload = gson.toJson(mapOf("query" to query))
////
////        // ✅ Modern OkHttp syntax (no deprecated MediaType.get)
////        val reqBody = payload.toRequestBody("application/json".toMediaType())
////
////        val req = Request.Builder()
////            .url(url)
////            .post(reqBody)
////            .build()
////
////        client.newCall(req).enqueue(object : Callback {
////            override fun onFailure(call: Call, e: IOException) {
////                scope.launch {
////                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
////                }
////            }
////
////            override fun onResponse(call: Call, response: Response) {
////                response.use {
////                    val body = it.body?.string() ?: "{}"
////                    val answer = gson.fromJson(body, Map::class.java)["answer"]?.toString()
////                        ?: "Sorry, I couldn't understand that."
////
////                    scope.launch {
////                        adapter.addMessage(ChatMessage(answer, false)) // add assistant reply
////                        rvChat.smoothScrollToPosition(messages.size - 1)
////                        mainActivity.speakText(answer) // speak it out
////                    }
////                }
////            }
////        })
////    }
////
////    override fun onDestroyView() {
////        super.onDestroyView()
////        vosk?.release()
////        scope.cancel()
////    }
////}
////
//
//package com.example.safedrivemonitor
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.MotionEvent
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import kotlinx.coroutines.*
//import okhttp3.*
//import com.google.gson.Gson
//import java.io.IOException
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody.Companion.toRequestBody
//
///**
// * AssistantFragment:
// * Voice-based Q&A system for driver queries.
// * Uses Vosk STT → Flask backend → TTS reply.
// * Now logs metrics for ERA, RL, and ISR.
// */
//class AssistantFragment : Fragment() {
//
//    private lateinit var rvChat: RecyclerView
//    private lateinit var btnMic: Button
//    private lateinit var adapter: ChatAdapter
//    private val messages = mutableListOf<ChatMessage>()
//
//    private val client = OkHttpClient()
//    private val gson = Gson()
//    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//
//    private var vosk: SimpleVoskStt? = null
//    private var isListening = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_assistant, container, false)
//
//        rvChat = view.findViewById(R.id.rvChat)
//        btnMic = view.findViewById(R.id.btnMic)
//
//        adapter = ChatAdapter(messages)
//        rvChat.adapter = adapter
//        rvChat.layoutManager = LinearLayoutManager(requireContext())
//
//        // Initialize Vosk STT (offline model)
//        vosk = SimpleVoskStt(requireContext(), "vosk-model-small-en-us-0.15")
//
//        // Mic button: Hold to talk
//        btnMic.setOnTouchListener { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> startListening()
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopListening()
//            }
//            true
//        }
//
//        return view
//    }
//
//    /**
//     * Starts speech recognition using Vosk.
//     */
//    private fun startListening() {
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(), Manifest.permission.RECORD_AUDIO
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Toast.makeText(requireContext(), "Microphone permission needed", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        if (isListening) return
//        isListening = true
//        btnMic.text = "Listening… 🎧"
//        vosk?.start { recognizedText ->
//            if (recognizedText.isNotBlank()) {
//                handleQuery(recognizedText)
//            }
//        }
//    }
//
//    /**
//     * Stops listening (when mic released).
//     */
//    private fun stopListening() {
//        if (!isListening) return
//        isListening = false
//        vosk?.stop()
//        btnMic.text = "🎤 HOLD TO TALK"
//    }
//
//    /**
//     * Send recognized query → Flask backend `/ask` → get answer → speak + display.
//     * Logs latency and success for evaluation metrics.
//     */
//    private fun handleQuery(query: String) {
//        val mainActivity = activity as? MainActivity ?: return
//        adapter.addMessage(ChatMessage(query, true)) // add driver query
//
//        val url = "${mainActivity.baseUrl}/ask"
//        val payload = gson.toJson(mapOf("query" to query))
//        val reqBody = payload.toRequestBody("application/json".toMediaType())
//
//        val startTime = System.currentTimeMillis() // for latency calculation
//
//        val req = Request.Builder()
//            .url(url)
//            .post(reqBody)
//            .build()
//
//        client.newCall(req).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                val latency = System.currentTimeMillis() - startTime
//                Log.e("AssistantMetrics", "Query failed in ${latency}ms | Error: ${e.message}")
//                scope.launch {
//                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val endTime = System.currentTimeMillis()
//                val latency = endTime - startTime
//
//                response.use {
//                    val body = it.body?.string() ?: "{}"
//                    val json = gson.fromJson(body, Map::class.java)
//                    val answer = json["answer"]?.toString()
//                        ?: "Sorry, I couldn't understand that."
//                    val success = json["success"] ?: true
//
//                    Log.i(
//                        "AssistantMetrics",
//                        "Query: \"$query\" | Latency: ${latency}ms | Success: $success | Answer: $answer"
//                    )
//
//                    scope.launch {
//                        adapter.addMessage(ChatMessage(answer, false))
//                        rvChat.smoothScrollToPosition(messages.size - 1)
//                        mainActivity.speakText(answer)
//                    }
//                }
//            }
//        })
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        vosk?.release()
//        scope.cancel()
//    }
//}
//

package com.example.safedrivemonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import okhttp3.*
import com.google.gson.Gson
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * AssistantFragment:
 * Voice-based Q&A system for driver queries.
 * Uses Vosk STT → Flask backend → TTS reply.
 * Includes latency logging, timeout fix, and modern OkHttp syntax.
 */
class AssistantFragment : Fragment() {

    private lateinit var rvChat: RecyclerView
    private lateinit var btnMic: Button
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    // ✅ Extended timeout client
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var vosk: SimpleVoskStt? = null
    private var isListening = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_assistant, container, false)

        rvChat = view.findViewById(R.id.rvChat)
        btnMic = view.findViewById(R.id.btnMic)

        adapter = ChatAdapter(messages)
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(requireContext())

        // Initialize Vosk STT (offline model)
        vosk = SimpleVoskStt(requireContext(), "vosk-model-small-en-us-0.15")

        // Mic button: Hold to talk
        btnMic.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startListening()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopListening()
            }
            true
        }

        return view
    }

    /**
     * Starts speech recognition using Vosk.
     */
    private fun startListening() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Microphone permission needed", Toast.LENGTH_SHORT).show()
            return
        }

        if (isListening) return
        isListening = true
        btnMic.text = "Listening… 🎧"
        vosk?.start { recognizedText ->
            if (recognizedText.isNotBlank()) {
                handleQuery(recognizedText)
            }
        }
    }

    /**
     * Stops listening (when mic released).
     */
    private fun stopListening() {
        if (!isListening) return
        isListening = false
        vosk?.stop()
        btnMic.text = "🎤 HOLD TO TALK"
    }

    /**
     * Send recognized query → Flask backend `/ask` → get answer → speak + display.
     * Logs latency and success for evaluation metrics.
     */
    private fun handleQuery(query: String) {
        val mainActivity = activity as? MainActivity ?: return
        adapter.addMessage(ChatMessage(query, true)) // add driver query

        val url = "${mainActivity.baseUrl}/ask"
        val payload = gson.toJson(mapOf("query" to query))
        val reqBody = payload.toRequestBody("application/json".toMediaType())

        val startTime = System.currentTimeMillis() // for latency measurement

        val req = Request.Builder()
            .url(url)
            .post(reqBody)
            .build()

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val latency = System.currentTimeMillis() - startTime
                Log.e("AssistantFragment", "❌ Query failed in ${latency}ms | Error: ${e.message}")
                scope.launch {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val endTime = System.currentTimeMillis()
                val latency = endTime - startTime

                response.use {
                    val body = it.body?.string() ?: "{}"
                    val json = gson.fromJson(body, Map::class.java)
                    val answer = json["answer"]?.toString()
                        ?: "Sorry, I couldn't understand that."
                    val success = json["success"] ?: true

                    Log.i(
                        "AssistantFragment",
                        "✅ Query: \"$query\" | Latency: ${latency}ms | Success: $success | Answer: $answer"
                    )

                    scope.launch {
                        adapter.addMessage(ChatMessage(answer, false))
                        rvChat.smoothScrollToPosition(messages.size - 1)
                        mainActivity.speakText(answer)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vosk?.release()
        scope.cancel()
    }
}
