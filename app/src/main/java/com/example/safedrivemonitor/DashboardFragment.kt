//package com.example.safedrivemonitor
//
//import android.graphics.Color
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.fragment.app.Fragment
//import com.google.android.material.progressindicator.CircularProgressIndicator
//import kotlinx.coroutines.*
//import okhttp3.*
//import com.google.gson.Gson
//import java.io.IOException
//
///**
// * DashboardFragment:
// * Displays current risk level, circular indicator, and lets user trigger a risk alert.
// */
//class DashboardFragment : Fragment() {
//
//    private lateinit var ring: CircularProgressIndicator
//    private lateinit var tvStatus: TextView
//    private lateinit var tvRiskLine: TextView
//    private lateinit var riskBar: SeekBar
//    private lateinit var btnSpeakRisk: Button
//
//    private val client = OkHttpClient()
//    private val gson = Gson()
//    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
//
//        // Initialize UI elements
//        ring = view.findViewById(R.id.ring)
//        tvStatus = view.findViewById(R.id.tvStatus)
//        tvRiskLine = view.findViewById(R.id.tvRiskLine)
//        riskBar = view.findViewById(R.id.riskBar)
//        btnSpeakRisk = view.findViewById(R.id.btnSpeakRisk)
//
//        // Default: low risk
//        updateUI(0)
//
//        // Update ring & text as slider moves
//        riskBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                updateUI(progress)
//            }
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//
//        // Button: speak current risk
//        btnSpeakRisk.setOnClickListener {
//            sendRiskAlert(riskBar.progress)
//        }
//
//        return view
//    }
//
//    /**
//     * Update the circular indicator and labels based on the current risk level (0–4)
//     */
//    private fun updateUI(level: Int) {
//        val (statusText, color, percent, desc) = when (level) {
//            0 -> arrayOf("SAFE", Color.GREEN, 10, "Risk level is low.")
//            1 -> arrayOf("CAUTION", Color.YELLOW, 40, "Minor alert: stay focused.")
//            2 -> arrayOf("MODERATE", Color.parseColor("#FFA500"), 60, "Stay alert and slow down.")
//            3 -> arrayOf("HIGH", Color.RED, 80, "High risk: reduce speed immediately.")
//            else -> arrayOf("CRITICAL", Color.MAGENTA, 100, "Critical: stop or pull over!")
//        }
//        ring.setIndicatorColor(color)
//        ring.progress = percent as Int
//        tvStatus.text = statusText.toString()
//        tvRiskLine.text = desc.toString()
//        tvStatus.setTextColor(color)
//    }
//
//    /**
//     * Sends the selected risk level to Flask backend → gets the risk message → speaks it.
//     */
//    private fun sendRiskAlert(level: Int) {
//        val mainActivity = activity as? MainActivity ?: return
//        val url = "${mainActivity.baseUrl}/risk_alert"
//
//        val payload = gson.toJson(mapOf("risk_level" to level))
//        val req = Request.Builder()
//            .url(url)
//            .post(RequestBody.create(MediaType.get("application/json"), payload))
//            .build()
//
//        client.newCall(req).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                scope.launch {
//                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    val body = it.body?.string() ?: "{}"
//                    val msg = gson.fromJson(body, Map::class.java)["message"]?.toString() ?: "No message"
//
//                    scope.launch {
//                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
//                        mainActivity.speakText(msg) // TTS alert
//                    }
//                }
//            }
//        })
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        scope.cancel()
//    }
//}

package com.example.safedrivemonitor

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.*
import okhttp3.*
import com.google.gson.Gson
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * DashboardFragment:
 * Displays current risk level, circular indicator, and lets user trigger a risk alert.
 */
class DashboardFragment : Fragment() {

    private lateinit var ring: CircularProgressIndicator
    private lateinit var tvStatus: TextView
    private lateinit var tvRiskLine: TextView
    private lateinit var riskBar: SeekBar
    private lateinit var btnSpeakRisk: Button

    private val client = OkHttpClient()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize UI elements
        ring = view.findViewById(R.id.ring)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvRiskLine = view.findViewById(R.id.tvRiskLine)
        riskBar = view.findViewById(R.id.riskBar)
        btnSpeakRisk = view.findViewById(R.id.btnSpeakRisk)

        // Default: low risk
        updateUI(0)

        // Update ring & text as slider moves
        riskBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateUI(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Button: speak current risk
        btnSpeakRisk.setOnClickListener {
            sendRiskAlert(riskBar.progress)
        }

        return view
    }

    /**
     * Update the circular indicator and labels based on the current risk level (0–4)
     */
//    private fun updateUI(level: Int) {
//        val (statusText, colorInt, percent, desc) = when (level) {
//            0 -> arrayOf("SAFE", Color.GREEN, 10, "Risk level is low.")
//            1 -> arrayOf("CAUTION", Color.YELLOW, 40, "Minor alert: stay focused.")
//            2 -> arrayOf("MODERATE", Color.parseColor("#FFA500"), 60, "Stay alert and slow down.")
//            3 -> arrayOf("HIGH", Color.RED, 80, "High risk: reduce speed immediately.")
//            else -> arrayOf("CRITICAL", Color.MAGENTA, 100, "Critical: stop or pull over!")
//        }
//
//        val color = colorInt as Int
//        ring.setIndicatorColor(color)
//        ring.progress = percent as Int
//        tvStatus.text = statusText.toString()
//        tvRiskLine.text = desc.toString()
//        tvStatus.setTextColor(color)
//    }

    private fun updateUI(level: Int) {
        val statusText: String
        val color: Int
        val percent: Int
        val desc: String

        when (level) {
            0 -> {
                statusText = "SAFE"
                color = resources.getColor(R.color.risk_safe, null)
                percent = 10
                desc = "Risk level is low."
            }
            1 -> {
                statusText = "CAUTION"
                color = resources.getColor(R.color.risk_caution, null)
                percent = 40
                desc = "Minor alert: stay focused."
            }
            2 -> {
                statusText = "MODERATE"
                color = resources.getColor(R.color.risk_moderate, null)
                percent = 60
                desc = "Stay alert and slow down."
            }
            3 -> {
                statusText = "HIGH"
                color = resources.getColor(R.color.risk_high, null)
                percent = 80
                desc = "High risk: reduce speed now."
            }
            else -> {
                statusText = "CRITICAL"
                color = resources.getColor(R.color.risk_critical, null)
                percent = 100
                desc = "Critical: stop safely!"
            }
        }

        // ✅ Apply updates safely
        ring.setIndicatorColor(color)
        ring.progress = percent
        tvStatus.text = statusText
        tvRiskLine.text = desc
        tvStatus.setTextColor(color)
    }



    /**
     * Sends the selected risk level to Flask backend → gets the risk message → speaks it.
     */
    private fun sendRiskAlert(level: Int) {
        val mainActivity = activity as? MainActivity ?: return
        val url = "${mainActivity.baseUrl}/risk_alert"

        val payload = gson.toJson(mapOf("risk_level" to level))
        // ✅ Modern OkHttp: use toMediaType() and toRequestBody()
        val reqBody = payload.toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url(url)
            .post(reqBody)
            .build()

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                scope.launch {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string() ?: "{}"
                    val msg = gson.fromJson(body, Map::class.java)["message"]?.toString()
                        ?: "No message"

                    scope.launch {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        mainActivity.speakText(msg) // TTS alert
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
