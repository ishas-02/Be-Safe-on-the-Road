package com.example.safedrivemonitor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

/**
 * ReportsFragment:
 * Shows trip events fetched from Flask backend and allows report download.
 */
class ReportsFragment : Fragment() {

    private lateinit var tvStatus: TextView
    private lateinit var btnDownload: Button
    private lateinit var btnOpen: Button
    private lateinit var rvEvents: RecyclerView
    private lateinit var eventAdapter: EventAdapter

    private val client = OkHttpClient()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var lastFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)

        tvStatus = view.findViewById(R.id.tvStatus)
        btnDownload = view.findViewById(R.id.btnDownload)
        btnOpen = view.findViewById(R.id.btnOpen)
        rvEvents = view.findViewById(R.id.rvEvents)

        eventAdapter = EventAdapter(mutableListOf())
        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        rvEvents.adapter = eventAdapter

        btnDownload.setOnClickListener { downloadReport() }
        btnOpen.setOnClickListener { openReport() }

        // 🚀 Fetch events live from backend when tab opens
        fetchEvents()

        return view
    }

    /**
     * Fetch latest driving events from Flask /events
     */
    private fun fetchEvents() {
        val mainActivity = activity as? MainActivity ?: return
        val url = "${mainActivity.baseUrl}/events"

        tvStatus.text = "Fetching recent events..."

        val req = Request.Builder().url(url).get().build()

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                scope.launch {
                    tvStatus.text = "Failed to fetch events: ${e.message}"
                    Toast.makeText(requireContext(), "Unable to load events", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        scope.launch {
                            tvStatus.text = "Failed: ${it.message}"
                            Toast.makeText(requireContext(), "Failed to fetch events", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val body = it.body?.string() ?: "[]"
                    val events: Array<String> = gson.fromJson(body, Array<String>::class.java)

                    scope.launch {
                        tvStatus.text = "Loaded ${events.size} recent events"
                        eventAdapter.clearAll()
                        events.reversed().forEach { e -> eventAdapter.addEvent(e) }
                    }
                }
            }
        })
    }

    /**
     * Downloads the latest PDF trip report
     */
    private fun downloadReport() {
        val mainActivity = activity as? MainActivity ?: return
        val url = "${mainActivity.baseUrl}/report"

        val req = Request.Builder().url(url).get().build()
        tvStatus.text = "Downloading report..."

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                scope.launch {
                    tvStatus.text = "Failed: ${e.message}"
                    Toast.makeText(requireContext(), "Download failed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        scope.launch {
                            tvStatus.text = "Failed: ${it.message}"
                            Toast.makeText(requireContext(), "Download failed!", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val bytes = it.body?.bytes()
                    if (bytes != null) {
                        val file = File(requireContext().getExternalFilesDir(null), "trip_report.pdf")
                        FileOutputStream(file).use { fos -> fos.write(bytes) }
                        lastFile = file
                        scope.launch {
                            tvStatus.text = "Saved: ${file.absolutePath}"
                            Toast.makeText(requireContext(), "Report downloaded", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    /**
     * Opens the last downloaded PDF report using viewer
     */
    private fun openReport() {
        if (lastFile == null) {
            Toast.makeText(requireContext(), "No report downloaded yet!", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            lastFile!!
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No PDF viewer installed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
