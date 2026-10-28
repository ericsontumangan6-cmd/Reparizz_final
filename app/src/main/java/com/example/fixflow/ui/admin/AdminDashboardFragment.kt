package com.example.fixflow.ui.admin

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
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentAdminDashboardBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
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
            db.favoriteDao()
        )
        sessionManager = SessionManager(requireContext())

        // Session Guard
        if (!sessionManager.isAdmin()) {
            Toast.makeText(requireContext(), "Access Denied: Administrator role required.", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        observeAdminData()
        setupListeners()
    }

    private fun observeAdminData() {
        repository.getApprovedShopCount().observe(viewLifecycleOwner) { count ->
            binding.tvAdminStatApproved.text = (count ?: 0).toString()
        }

        repository.getPendingShopCount().observe(viewLifecycleOwner) { count ->
            binding.tvAdminStatPending.text = (count ?: 0).toString()
        }

        repository.getRejectedShopCount().observe(viewLifecycleOwner) { count ->
            binding.tvAdminStatRejected.text = (count ?: 0).toString()
        }

        // Staff Account Management Observer
        repository.getAllStaff().observe(viewLifecycleOwner) { staffList ->
            binding.layoutAdminStaffContainer.removeAllViews()
            if (staffList.isNullOrEmpty()) {
                val emptyTv = TextView(requireContext()).apply {
                    text = "No staff accounts created yet. Click 'Add Staff' to register staff."
                    setTextColor("#A7B3C4".toColorInt())
                    textSize = 12f
                    setPadding(0, 8, 0, 8)
                }
                binding.layoutAdminStaffContainer.addView(emptyTv)
            } else {
                val context = requireContext()
                for (staff in staffList) {
                    val card = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setBackgroundResource(R.drawable.bg_input_field)
                        setPadding(20, 16, 20, 16)
                        gravity = Gravity.CENTER_VERTICAL
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.bottomMargin = 8
                        layoutParams = params
                    }

                    val infoLayout = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val nameTv = TextView(context).apply {
                        text = "${staff.fullName} (${if (staff.isActive) "Active" else "Disabled"})"
                        setTextColor(if (staff.isActive) "#FFFFFF".toColorInt() else "#FF6B6B".toColorInt())
                        textSize = 13f
                        setTypeface(null, Typeface.BOLD)
                    }

                    val emailTv = TextView(context).apply {
                        text = "✉️ ${staff.email}"
                        setTextColor("#A7B3C4".toColorInt())
                        textSize = 11f
                    }

                    infoLayout.addView(nameTv)
                    infoLayout.addView(emailTv)

                    val toggleBtn = MaterialButton(context).apply {
                        text = if (staff.isActive) "Disable" else "Enable"
                        textSize = 11f
                        setPadding(12, 0, 12, 0)
                        setOnSingleClickListener {
                            lifecycleScope.launch {
                                repository.setUserActiveStatus(staff.userId, !staff.isActive)
                                val statusText = if (!staff.isActive) "enabled" else "disabled"
                                Toast.makeText(context, "Staff account ${staff.fullName} $statusText.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    card.addView(infoLayout)
                    card.addView(toggleBtn)
                    binding.layoutAdminStaffContainer.addView(card)
                }
            }
        }

        // Customer Account Management Observer
        repository.getAllCustomers().observe(viewLifecycleOwner) { customerList ->
            binding.layoutAdminCustomersContainer.removeAllViews()
            if (customerList.isNullOrEmpty()) {
                val emptyTv = TextView(requireContext()).apply {
                    text = "No registered customer accounts yet."
                    setTextColor("#A7B3C4".toColorInt())
                    textSize = 12f
                    setPadding(0, 8, 0, 8)
                }
                binding.layoutAdminCustomersContainer.addView(emptyTv)
            } else {
                val context = requireContext()
                for (cust in customerList) {
                    val card = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setBackgroundResource(R.drawable.bg_input_field)
                        setPadding(20, 16, 20, 16)
                        gravity = Gravity.CENTER_VERTICAL
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.bottomMargin = 8
                        layoutParams = params
                    }

                    val infoLayout = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val nameTv = TextView(context).apply {
                        text = "${cust.fullName} (${if (cust.isActive) "Active" else "Disabled"})"
                        setTextColor(if (cust.isActive) "#FFFFFF".toColorInt() else "#FF6B6B".toColorInt())
                        textSize = 13f
                        setTypeface(null, Typeface.BOLD)
                    }

                    val emailTv = TextView(context).apply {
                        text = "✉️ ${cust.email}"
                        setTextColor("#A7B3C4".toColorInt())
                        textSize = 11f
                    }

                    infoLayout.addView(nameTv)
                    infoLayout.addView(emailTv)

                    val toggleBtn = MaterialButton(context).apply {
                        text = if (cust.isActive) "Disable" else "Enable"
                        textSize = 11f
                        setPadding(12, 0, 12, 0)
                        setOnSingleClickListener {
                            lifecycleScope.launch {
                                repository.setUserActiveStatus(cust.userId, !cust.isActive)
                                val statusText = if (!cust.isActive) "enabled" else "disabled"
                                Toast.makeText(context, "Customer account ${cust.fullName} $statusText.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    card.addView(infoLayout)
                    card.addView(toggleBtn)
                    binding.layoutAdminCustomersContainer.addView(card)
                }
            }
        }

        repository.getAllShops().observe(viewLifecycleOwner) { shops ->
            binding.layoutAdminAllShopsContainer.removeAllViews()
            if (shops.isNullOrEmpty()) {
                val emptyTv = TextView(requireContext()).apply {
                    text = "No registered shops in Malolos, Bulacan."
                    setTextColor("#A7B3C4".toColorInt())
                    textSize = 12f
                    setPadding(0, 8, 0, 8)
                }
                binding.layoutAdminAllShopsContainer.addView(emptyTv)
            } else {
                val context = requireContext()
                for (shop in shops) {
                    val card = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundResource(R.drawable.bg_input_field)
                        setPadding(24, 16, 24, 16)
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.bottomMargin = 12
                        layoutParams = params
                    }

                    val titleTv = TextView(context).apply {
                        text = "${shop.shopName} [${shop.approvalStatus}]"
                        setTextColor("#FFFFFF".toColorInt())
                        textSize = 14f
                        setTypeface(null, Typeface.BOLD)
                    }

                    val addressTv = TextView(context).apply {
                        text = "📍 ${shop.address}"
                        setTextColor("#A7B3C4".toColorInt())
                        textSize = 12f
                    }

                    card.addView(titleTv)
                    card.addView(addressTv)

                    card.setOnSingleClickListener {
                        val bundle = Bundle().apply {
                            putLong("shopId", shop.shopId)
                        }
                        findNavController().navigate(R.id.nav_admin_edit_shop, bundle)
                    }

                    binding.layoutAdminAllShopsContainer.addView(card)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnAdminLogout.setOnSingleClickListener {
            sessionManager.clearSession()
            Toast.makeText(requireContext(), "Admin session logged out.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.nav_welcome)
        }

        binding.btnAdminAddStaff.setOnSingleClickListener {
            showAddStaffDialog()
        }

        binding.btnAdminViewAnalytics.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_analytics)
        }

        binding.btnAdminViewInventory.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_inventory)
        }

        binding.btnAdminViewAllJobs.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_jobs)
        }

        binding.btnAdminViewPending.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_admin_pending_review)
        }

        binding.btnAdminAddShop.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_shop_registration)
        }
    }

    private fun showAddStaffDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(context).apply {
            hint = "Full Name (e.g. Technician Alex)"
        }
        val etEmail = EditText(context).apply {
            hint = "Email Address (e.g. alex@malolos.com)"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val etPassword = EditText(context).apply {
            hint = "Initial Password (min 6 chars)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etName)
        layout.addView(etEmail)
        layout.addView(etPassword)

        AlertDialog.Builder(context)
            .setTitle("👨‍🔧 Add New Staff Account")
            .setMessage("Create a new staff user with technician repair intake and handling privileges.")
            .setView(layout)
            .setPositiveButton("Create Account") { dialog, _ ->
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (name.isEmpty() || email.isEmpty() || password.length < 6) {
                    Toast.makeText(context, "Please enter valid staff details (password >= 6 chars).", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val existing = repository.getUserByEmail(email)
                    if (existing != null) {
                        Toast.makeText(context, "Email already exists in system.", Toast.LENGTH_SHORT).show()
                    } else {
                        repository.createStaffAccount(name, email, password)
                        Toast.makeText(context, "Staff account for $name created successfully!", Toast.LENGTH_SHORT).show()
                    }
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
