package com.example.fixflow.ui.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fixflow.R
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentUserLoginBinding
import com.example.fixflow.util.PasswordHasher
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import kotlinx.coroutines.launch

class UserLoginFragment : Fragment() {

    private var _binding: FragmentUserLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserLoginBinding.inflate(inflater, container, false)
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

        setupRoleChipListeners()
        setupListeners()
    }

    private fun setupRoleChipListeners() {
        binding.chipGroupLoginRoles.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_role_staff -> {
                    binding.tvLoginPortalTitle.text = "🛠️ Staff / Technician Sign-In"
                    binding.tvLoginPortalSubtitle.text = "Enter staff credentials to manage assigned intake and repairs."
                    binding.etUserLoginEmail.setText("staff@malolos.com")
                    binding.etUserLoginPassword.setText("staff123")
                }
                R.id.chip_role_admin -> {
                    binding.tvLoginPortalTitle.text = "⚙️ Administrator Sign-In"
                    binding.tvLoginPortalSubtitle.text = "Enter administrator credentials to access management and analytics."
                    binding.etUserLoginEmail.setText("admin@malolos.com")
                    binding.etUserLoginPassword.setText("admin123")
                }
                else -> {
                    binding.tvLoginPortalTitle.text = "👤 Client / Customer Sign-In"
                    binding.tvLoginPortalSubtitle.text = "Enter your email and password to open your repair dashboard."
                    binding.etUserLoginEmail.setText("customer@malolos.com")
                    binding.etUserLoginPassword.setText("customer123")
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackLogin.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        binding.tvGoToRegister.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_user_register)
        }

        binding.tvUserForgotPassword.setOnSingleClickListener {
            showForgotPasswordDialog()
        }

        binding.btnSubmitUserLogin.setOnSingleClickListener {
            val email = binding.etUserLoginEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etUserLoginPassword.text?.toString()?.trim().orEmpty()

            var isValid = true

            if (email.isEmpty()) {
                binding.tilUserLoginEmail.error = "Email is required"
                isValid = false
            } else {
                binding.tilUserLoginEmail.error = null
            }

            if (password.isEmpty()) {
                binding.tilUserLoginPassword.error = "Password is required"
                isValid = false
            } else {
                binding.tilUserLoginPassword.error = null
            }

            if (!isValid) return@setOnSingleClickListener

            lifecycleScope.launch {
                repository.ensureDefaultAdminAndDemoShops()
                val user = repository.getUserByEmail(email)

                if (user == null || !PasswordHasher.verifyPassword(password, user.passwordHash)) {
                    Toast.makeText(requireContext(), "Invalid credentials. Check email & password.", Toast.LENGTH_LONG).show()
                } else if (!user.isActive) {
                    Toast.makeText(requireContext(), "Account disabled. Please contact administrator.", Toast.LENGTH_LONG).show()
                } else {
                    sessionManager.saveSession(user.userId, user.email, user.fullName, user.role)
                    Toast.makeText(requireContext(), "Welcome back, ${user.fullName} (${user.role})!", Toast.LENGTH_SHORT).show()

                    val role = sessionManager.getUserRole()
                    when (role) {
                        SessionManager.ROLE_ADMIN -> findNavController().navigate(R.id.nav_admin_dashboard)
                        SessionManager.ROLE_STAFF -> findNavController().navigate(R.id.nav_staff_dashboard)
                        SessionManager.ROLE_USER -> findNavController().navigate(R.id.nav_shop_map)
                        else -> findNavController().navigate(R.id.nav_shop_map)
                    }
                }
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etEmail = EditText(context).apply {
            hint = "Account Email Address *"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val etNewPassword = EditText(context).apply {
            hint = "New Password (min 6 characters) *"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etEmail)
        layout.addView(etNewPassword)

        AlertDialog.Builder(context)
            .setTitle("🔑 Reset Account Password")
            .setMessage("Enter your registered email address and your new password.")
            .setView(layout)
            .setPositiveButton("Reset Password") { dialog, _ ->
                val email = etEmail.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()

                if (email.isEmpty() || newPassword.length < 6) {
                    Toast.makeText(context, "Please enter a valid email and new password (>= 6 chars).", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val success = repository.resetPasswordByEmail(email, newPassword)
                    if (success) {
                        Toast.makeText(context, "Password updated successfully! Please log in with your new password.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No registered user found with that email address.", Toast.LENGTH_LONG).show()
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
