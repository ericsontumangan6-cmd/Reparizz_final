package com.example.fixflow.ui.inventory

import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fixflow.R
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.entity.SparePart
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentInventoryBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Locale

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FixFlowDatabase.getDatabase(requireContext())
        repository = RepairRepository(
            db.repairJobDao(),
            db.repairEstimateDao(),
            db.repairStatusHistoryDao(),
            db.repairTestDao(),
            db.paymentDao(),
            db.warrantyDao(),
            db.warrantyClaimDao(),
            db.userDao(),
            db.shopDao(),
            db.favoriteDao(),
            db.sparePartDao(),
            db.serviceReviewDao()
        )
        sessionManager = SessionManager(requireContext())

        setupListeners()
        observeInventory()
    }

    private fun observeInventory() {
        repository.getAllSpareParts()?.observe(viewLifecycleOwner) { parts ->
            binding.layoutInventoryContainer.removeAllViews()

            if (parts.isNullOrEmpty()) {
                val emptyTv = TextView(requireContext()).apply {
                    text = "No spare parts registered in stock inventory."
                    setTextColor("#A7B3C4".toColorInt())
                    textSize = 13f
                    setPadding(0, 16, 0, 16)
                }
                binding.layoutInventoryContainer.addView(emptyTv)
                return@observe
            }

            val context = requireContext()
            for (part in parts) {
                val card = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setBackgroundResource(R.drawable.bg_card)
                    setPadding(20, 16, 20, 16)
                    gravity = Gravity.CENTER_VERTICAL
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.bottomMargin = 10
                    layoutParams = params
                }

                val infoLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val nameTv = TextView(context).apply {
                    text = part.partName
                    setTextColor("#FFFFFF".toColorInt())
                    textSize = 14f
                    setTypeface(null, Typeface.BOLD)
                }

                val categoryTv = TextView(context).apply {
                    val lowStockWarning = if (part.isLowStock) " ⚠️ LOW STOCK" else ""
                    text = String.format(Locale.US, "Category: %s • Unit Price: ₱%.2f%s", part.partCategory, part.unitCost, lowStockWarning)
                    setTextColor(if (part.isLowStock) "#FF6B6B".toColorInt() else "#35D6C3".toColorInt())
                    textSize = 11f
                }

                val qtyTv = TextView(context).apply {
                    text = String.format(Locale.US, "In Stock: %d", part.quantityInStock)
                    setTextColor("#A7B3C4".toColorInt())
                    textSize = 12f
                }

                infoLayout.addView(nameTv)
                infoLayout.addView(categoryTv)
                infoLayout.addView(qtyTv)

                val restockBtn = MaterialButton(context).apply {
                    text = "➕ Restock"
                    textSize = 11f
                    setPadding(12, 0, 12, 0)
                    setOnSingleClickListener {
                        showRestockDialog(part)
                    }
                }

                card.addView(infoLayout)
                card.addView(restockBtn)
                binding.layoutInventoryContainer.addView(card)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackInventory.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddSparePart.setOnSingleClickListener {
            showAddSparePartDialog()
        }
    }

    private fun showAddSparePartDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(context).apply {
            hint = "Part Name (e.g. iPhone 13 OLED Assembly) *"
        }
        val etCategory = EditText(context).apply {
            hint = "Category (Screen, Battery, Charging Port, RAM) *"
        }
        val etCost = EditText(context).apply {
            hint = "Unit Price (₱) *"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etQty = EditText(context).apply {
            hint = "Initial Stock Quantity *"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(etName)
        layout.addView(etCategory)
        layout.addView(etCost)
        layout.addView(etQty)

        AlertDialog.Builder(context)
            .setTitle("📦 Register New Spare Part")
            .setView(layout)
            .setPositiveButton("Add to Stock") { dialog, _ ->
                val name = etName.text.toString().trim()
                val category = etCategory.text.toString().trim().ifEmpty { "General" }
                val cost = etCost.text.toString().toDoubleOrNull() ?: 0.0
                val qty = etQty.text.toString().toIntOrNull() ?: 0

                if (name.isEmpty() || cost <= 0 || qty <= 0) {
                    Toast.makeText(context, "Please enter valid part name, price, and stock quantity.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val part = SparePart(0, name, category, qty, cost, 3)
                    repository.insertSparePart(part)
                    Toast.makeText(context, "Spare part $name added to inventory!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRestockDialog(part: SparePart) {
        val context = requireContext()
        val etQty = EditText(context).apply {
            hint = "Additional Stock Quantity to Add *"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val container = LinearLayout(context).apply {
            setPadding(48, 16, 48, 16)
            addView(etQty)
        }

        AlertDialog.Builder(context)
            .setTitle("➕ Restock ${part.partName}")
            .setMessage("Current Stock: ${part.quantityInStock} units.")
            .setView(container)
            .setPositiveButton("Restock") { dialog, _ ->
                val qtyToAdd = etQty.text.toString().toIntOrNull() ?: 0
                if (qtyToAdd <= 0) {
                    Toast.makeText(context, "Enter a valid positive quantity.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    part.quantityInStock += qtyToAdd
                    repository.insertSparePart(part)
                    Toast.makeText(context, "Added $qtyToAdd units to ${part.partName}.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
