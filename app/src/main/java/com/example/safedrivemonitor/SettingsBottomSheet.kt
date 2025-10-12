//package com.example.safedrivemonitor
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment
//
///**
// * SettingsBottomSheet:
// * Appears when user taps ⚙️ icon.
// * Allows changing backend URL, language (English/Spanish), and voice (Male/Female).
// */
//class SettingsBottomSheet : BottomSheetDialogFragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        // Inflate the bottomsheet_settings.xml layout
//        val view = inflater.inflate(R.layout.bottomsheet_settings, container, false)
//
//        // UI elements
//        val etServer = view.findViewById<EditText>(R.id.etServer)
//        val spLang = view.findViewById<Spinner>(R.id.spLang)
//        val spGender = view.findViewById<Spinner>(R.id.spGender)
//        val btnApply = view.findViewById<Button>(R.id.btnApply)
//
//        // Access main activity
//        val mainActivity = activity as? MainActivity ?: return view
//
//        // Load saved backend URL
//        etServer.setText(mainActivity.baseUrl)
//
//        // Language spinner setup
//        val langOptions = listOf("English", "Spanish")
//        val langAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, langOptions)
//        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spLang.adapter = langAdapter
//
//        // Pre-select based on saved language
//        val currentLang = if (mainActivity.selectedLang.language == "es") "Spanish" else "English"
//        spLang.setSelection(langOptions.indexOf(currentLang))
//
//        // Gender (voice) spinner setup
//        val genderOptions = listOf("Female", "Male")
//        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
//        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spGender.adapter = genderAdapter
//        spGender.setSelection(genderOptions.indexOf(mainActivity.selectedGender))
//
//        // Apply button click
//        btnApply.setOnClickListener {
//            val newUrl = etServer.text.toString().trim()
//            val selectedLang = if (spLang.selectedItem.toString() == "Spanish") "es" else "en"
//            val selectedGender = spGender.selectedItem.toString()
//
//            // Apply and save settings
//            mainActivity.applySettings(newUrl, selectedLang, selectedGender)
//
//            Toast.makeText(requireContext(), "Settings updated successfully", Toast.LENGTH_SHORT).show()
//            dismiss()  // Close the bottom sheet
//        }
//
//        return view
//    }
//}

package com.example.safedrivemonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * SettingsBottomSheet:
 * Appears when user taps ⚙️ icon.
 * Allows changing backend URL, language (English/Spanish), and voice (Male/Female).
 */
class SettingsBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the bottomsheet_settings.xml layout
        val view = inflater.inflate(R.layout.bottomsheet_settings, container, false)

        // UI references
        val etServer = view.findViewById<EditText>(R.id.etServer)
        val spLang = view.findViewById<Spinner>(R.id.spLang)
        val spGender = view.findViewById<Spinner>(R.id.spGender)
        val btnApply = view.findViewById<Button>(R.id.btnApply)

        // Get reference to main activity
        val mainActivity = activity as? MainActivity ?: return view

        // --- Populate existing values ---
        etServer.setText(mainActivity.baseUrl)

        // --- Language Spinner ---
        val langOptions = listOf("English", "Spanish")
        val langAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            langOptions
        )
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLang.adapter = langAdapter

        // Pre-select based on current language in MainActivity
        val currentLang =
            if (mainActivity.selectedLang.language == "es") "Spanish" else "English"
        spLang.setSelection(langOptions.indexOf(currentLang))

        // --- Voice (Gender) Spinner ---
        val genderOptions = listOf("Female", "Male")
        val genderAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            genderOptions
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGender.adapter = genderAdapter

        // Preselect current voice
        spGender.setSelection(genderOptions.indexOf(mainActivity.selectedGender))

        // --- Apply button ---
        btnApply.setOnClickListener {
            val newUrl = etServer.text.toString().trim()
            val selectedLang =
                if (spLang.selectedItem.toString() == "Spanish") "es" else "en"
            val selectedGender = spGender.selectedItem.toString()

            // Save + apply
            mainActivity.applySettings(newUrl, selectedLang, selectedGender)

            Toast.makeText(
                requireContext(),
                "Settings updated successfully",
                Toast.LENGTH_SHORT
            ).show()

            dismiss() // close bottom sheet
        }

        return view
    }
}

