package com.example.fixflow.ui.shop

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fixflow.R
import com.example.fixflow.databinding.FragmentShopMapBinding
import com.example.fixflow.util.setOnSingleClickListener
import java.util.Locale

class ShopMapFragment : Fragment() {

    private var _binding: FragmentShopMapBinding? = null
    private val binding get() = _binding!!

    private var currentScale = 1.0f
    private val minScale = 1.0f
    private val maxScale = 3.2f
    private var scaleGestureDetector: ScaleGestureDetector? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupPinchToZoom()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPinchToZoom() {
        scaleGestureDetector = ScaleGestureDetector(requireContext(), object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                currentScale *= detector.scaleFactor
                currentScale = currentScale.coerceIn(minScale, maxScale)

                binding.mapContainer.pivotX = detector.focusX
                binding.mapContainer.pivotY = detector.focusY
                binding.mapContainer.scaleX = currentScale
                binding.mapContainer.scaleY = currentScale

                binding.tvZoomLevel.text = String.format(Locale.US, "Zoom: %.1fx", currentScale)
                return true
            }
        })

        binding.mapViewport.setOnTouchListener { _, event ->
            scaleGestureDetector?.onTouchEvent(event)
            true
        }
    }

    private fun setupListeners() {
        binding.btnBackMap.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        binding.btnZoomIn.setOnClickListener {
            zoomIn()
        }

        binding.btnZoomOut.setOnClickListener {
            zoomOut()
        }

        // Search Bar Functionality
        binding.btnMapSearch.setOnSingleClickListener {
            performMapSearch()
        }

        binding.etMapSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performMapSearch()
                true
            } else false
        }

        binding.btnGpsRecenter.setOnSingleClickListener {
            resetMapCamera()
            displayFbnDetails()
            Toast.makeText(requireContext(), "GPS Recentered: Malolos Crossing (14.8527° N, 120.8160° E)", Toast.LENGTH_SHORT).show()
        }

        binding.pinFbn.setOnClickListener { displayFbnDetails() }
        binding.btnSelectFbn.setOnClickListener { displayFbnDetails() }

        binding.pinHub.setOnClickListener { displayBulSuBranchDetails() }
        binding.btnSelectReparizz.setOnClickListener { displayBulSuBranchDetails() }

        binding.pinCapitol.setOnClickListener { displayCapitolDetails() }
        binding.btnSelectCapitol.setOnClickListener { displayCapitolDetails() }

        binding.pinMabolo.setOnClickListener { displayMaboloDetails() }
        binding.btnSelectMabolo.setOnClickListener { displayMaboloDetails() }

        // Top Category Quick Chips Zoom & Focus
        binding.chipGroupUtilities.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_electronics -> {
                    displayFbnDetails()
                    Toast.makeText(requireContext(), "Category: Zoomed on RepaRizz HQ & Electronics", Toast.LENGTH_SHORT).show()
                }
                R.id.chip_cleaning -> {
                    displayBulSuBranchDetails()
                    Toast.makeText(requireContext(), "Category: Zoomed on BulSU Campus Branch", Toast.LENGTH_SHORT).show()
                }
                R.id.chip_plumbing -> {
                    displayCapitolDetails()
                    Toast.makeText(requireContext(), "Category: Zoomed on Capitol Tech & Local Hubs", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnAdminApprove.setOnSingleClickListener {
            binding.tvShopStatusBadge.text = "APPROVED & VERIFIED SERVICE CENTER"
            binding.tvShopStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50"))
            Toast.makeText(requireContext(), "Admin Action: Malolos Shop Registration APPROVED!", Toast.LENGTH_SHORT).show()
        }

        binding.btnAdminReject.setOnSingleClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reject Shop Registration")
                .setMessage("Are you sure you want to REJECT this Malolos shop branch registration?")
                .setPositiveButton("Yes, Reject") { _, _ ->
                    binding.tvShopStatusBadge.text = "REJECTED BY ADMIN"
                    binding.tvShopStatusBadge.setBackgroundColor(Color.parseColor("#E53935"))
                    Toast.makeText(requireContext(), "Admin Action: Shop Registration REJECTED.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnAdminUpdate.setOnSingleClickListener {
            Toast.makeText(requireContext(), "Admin Action: Malolos Shop Registry Details Updated!", Toast.LENGTH_SHORT).show()
        }

        // Functional Call Button
        binding.btnActionCall.setOnSingleClickListener {
            val rawContact = binding.tvShopContact.text.toString()
            val phoneMatch = Regex("""\(\+63\)\s*[\d-]+""").find(rawContact)?.value ?: "(+63) 917-123-4567"
            try {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneMatch.replace(" ", "")}"))
                startActivity(callIntent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Calling Shop: $phoneMatch", Toast.LENGTH_LONG).show()
            }
        }

        // Functional Directions Button
        binding.btnActionDirections.setOnSingleClickListener {
            val address = binding.tvShopAddress.text.toString().replace("📍 Address: ", "").trim()
            try {
                val gmmIntentUri = Uri.parse("geo:14.8527,120.8160?q=" + Uri.encode(address))
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                startActivity(mapIntent)
            } catch (e: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address)))
                    startActivity(browserIntent)
                } catch (ex: Exception) {
                    Toast.makeText(requireContext(), "Directions to: $address", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnActionJobs.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_jobs)
        }
    }

    private fun performMapSearch() {
        val query = binding.etMapSearch.text?.toString()?.trim()?.lowercase(Locale.getDefault()).orEmpty()
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a shop name or location in Malolos to search.", Toast.LENGTH_SHORT).show()
            return
        }

        when {
            query.contains("fbn") || query.contains("crossing") || query.contains("hq") || query.contains("main") -> {
                displayFbnDetails()
            }
            query.contains("bulsu") || query.contains("guinhawa") || query.contains("student") -> {
                displayBulSuBranchDetails()
            }
            query.contains("capitol") || query.contains("catmon") || query.contains("desktop") -> {
                displayCapitolDetails()
            }
            query.contains("mabolo") || query.contains("central") || query.contains("partner") -> {
                displayMaboloDetails()
            }
            else -> {
                displayFbnDetails()
                Toast.makeText(requireContext(), "Found top result matching '$query' in Malolos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun zoomIn() {
        currentScale = (currentScale + 0.4f).coerceAtMost(maxScale)
        binding.mapContainer.animate()
            .scaleX(currentScale)
            .scaleY(currentScale)
            .setDuration(250)
            .start()
        binding.tvZoomLevel.text = String.format(Locale.US, "Zoom: %.1fx", currentScale)
    }

    private fun zoomOut() {
        currentScale = (currentScale - 0.4f).coerceAtLeast(minScale)
        if (currentScale <= 1.05f) {
            currentScale = 1.0f
            val centerX = binding.mapContainer.width / 2f
            val centerY = binding.mapContainer.height / 2f
            binding.mapContainer.pivotX = centerX
            binding.mapContainer.pivotY = centerY
            binding.mapContainer.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(250)
                .start()
        } else {
            binding.mapContainer.animate()
                .scaleX(currentScale)
                .scaleY(currentScale)
                .setDuration(250)
                .start()
        }
        binding.tvZoomLevel.text = String.format(Locale.US, "Zoom: %.1fx", currentScale)
    }

    private fun resetMapCamera() {
        currentScale = 1.0f
        val centerX = binding.mapContainer.width / 2f
        val centerY = binding.mapContainer.height / 2f
        binding.mapContainer.pivotX = centerX
        binding.mapContainer.pivotY = centerY
        binding.mapContainer.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(350)
            .start()
        binding.tvZoomLevel.text = "Zoom: 1.0x"
    }

    private fun focusAndZoomOnView(targetPin: View?, zoomScale: Float = 2.2f) {
        val container = binding.mapContainer
        val targetX = if (targetPin != null && targetPin.width > 0) {
            targetPin.x + (targetPin.width / 2f)
        } else {
            container.width / 2f
        }

        val targetY = if (targetPin != null && targetPin.height > 0) {
            targetPin.y + (targetPin.height / 2f)
        } else {
            container.height / 2f
        }

        currentScale = zoomScale.coerceIn(minScale, maxScale)

        container.pivotX = targetX
        container.pivotY = targetY
        container.animate()
            .scaleX(currentScale)
            .scaleY(currentScale)
            .setDuration(400)
            .start()

        binding.tvZoomLevel.text = String.format(Locale.US, "Zoom: %.1fx", currentScale)
    }

    private fun displayFbnDetails() {
        focusAndZoomOnView(binding.pinFbn, 2.2f)

        binding.tvShopName.text = "RepaRizz HQ / FBN IT Solution"
        binding.tvShopStatusBadge.text = "✓ APPROVED & VERIFIED SERVICE CENTER"
        binding.tvShopStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50"))
        binding.tvShopCategory.text = "🏷️ Category: Electronics & IT Solutions"
        binding.tvShopAddress.text = "📍 Address: MacArthur Highway, Malolos Crossing, Malolos, 3000 Bulacan, Philippines"
        binding.tvShopContact.text = "📞 Contact: (+63) 917-123-4567 | fbn.malolos@gmail.com"
        binding.tvShopServices.text = "🛠️ Core Services: Electronics Repair, Motherboard Diagnostics, Component Replacement, Ultrasonic Board Cleaning"
        binding.tvShopHours.text = "⏰ Operating Hours: Mon - Sat: 8:00 AM - 6:00 PM"
        Toast.makeText(requireContext(), "Map Camera Focused & Zoomed on RepaRizz HQ (Crossing)", Toast.LENGTH_SHORT).show()
    }

    private fun displayBulSuBranchDetails() {
        focusAndZoomOnView(binding.pinHub, 2.2f)

        binding.tvShopName.text = "RepaRizz BulSU Campus Branch"
        binding.tvShopStatusBadge.text = "✓ VERIFIED AUTHORIZED BRANCH"
        binding.tvShopStatusBadge.setBackgroundColor(Color.parseColor("#35D6C3"))
        binding.tvShopCategory.text = "🏷️ Category: Smartphone & Student Gadget Repair"
        binding.tvShopAddress.text = "📍 Address: Guinhawa, MacArthur Hwy (Near BulSU Gate 2), Malolos, 3000 Bulacan, Philippines"
        binding.tvShopContact.text = "📞 Contact: (+63) 918-987-6543 | bulsu.reparizz@gmail.com"
        binding.tvShopServices.text = "🛠️ Core Services: Smartphone Screen Repair, Laptop Battery Replacement, Water Damage Recovery, Quick Cleaning"
        binding.tvShopHours.text = "⏰ Operating Hours: Mon - Sun: 8:00 AM - 8:00 PM"
        Toast.makeText(requireContext(), "Map Camera Focused & Zoomed on BulSU Branch (Guinhawa)", Toast.LENGTH_SHORT).show()
    }

    private fun displayCapitolDetails() {
        focusAndZoomOnView(binding.pinCapitol, 2.2f)

        binding.tvShopName.text = "Capitol Tech & Electronics Care"
        binding.tvShopStatusBadge.text = "✓ VERIFIED LOCAL SERVICE HUB"
        binding.tvShopStatusBadge.setBackgroundColor(Color.parseColor("#F5A623"))
        binding.tvShopCategory.text = "🏷️ Category: Desktop & Office Electronics"
        binding.tvShopAddress.text = "📍 Address: Paseo del Congreso, Brgy. Catmon, Malolos, 3000 Bulacan, Philippines"
        binding.tvShopContact.text = "📞 Contact: (+63) 919-456-7890 | capitol.electronics@gmail.com"
        binding.tvShopServices.text = "🛠️ Core Services: Desktop System Unit Assembly, Power Supply Repair, Monitor Diagnostics, General Cleaning"
        binding.tvShopHours.text = "⏰ Operating Hours: Mon - Fri: 8:30 AM - 5:30 PM"
        Toast.makeText(requireContext(), "Map Camera Focused & Zoomed on Capitol Tech (Catmon)", Toast.LENGTH_SHORT).show()
    }

    private fun displayMaboloDetails() {
        focusAndZoomOnView(binding.pinMabolo, 2.2f)

        binding.tvShopName.text = "Central Bulacan Repair Center"
        binding.tvShopStatusBadge.text = "✓ REGISTERED SERVICE PARTNER"
        binding.tvShopStatusBadge.setBackgroundColor(Color.parseColor("#9C27B0"))
        binding.tvShopCategory.text = "🏷️ Category: General Gadgets & Utilities"
        binding.tvShopAddress.text = "📍 Address: Mabolo Road, Brgy. Mabolo, Malolos, 3000 Bulacan, Philippines"
        binding.tvShopContact.text = "📞 Contact: (+63) 920-333-4444 | central.mabolo@gmail.com"
        binding.tvShopServices.text = "🛠️ Core Services: Audio Device Diagnostics, Smartwatch Battery Replacement, Tablet Digitizer Repair"
        binding.tvShopHours.text = "⏰ Operating Hours: Mon - Sat: 9:00 AM - 7:00 PM"
        Toast.makeText(requireContext(), "Map Camera Focused & Zoomed on Central Bulacan (Mabolo)", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
