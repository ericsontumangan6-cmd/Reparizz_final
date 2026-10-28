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
import com.example.fixflow.databinding.FragmentAdminLoginBinding
import com.example.fixflow.util.PasswordHasher
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import kotlinx.coroutines.launch

class AdminLoginFragment : Fragment() {

    private var _binding: FragmentAdminLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminLoginBinding.inflate(inflater, container, false)
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

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBackAdminLogin.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        binding.tvAdminForgotPassword.setOnSingleClickListener {
            showForgotPasswordDialog()
        }

        binding.btnSubmitAdminLogin.setOnSingleClickListener {
            val email = binding.etAdminLoginEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etAdminLoginPassword.text?.toString()?.trim().orEmpty()

            var isValid = true

            if (email.isEmpty()) {
                binding.tilAdminLoginEmail.error = "Admin email is required"
                isValid = false
            } else {
                binding.tilAdminLoginEmail.error = null
            }

            if (password.isEmpty()) {
                binding.tilAdminLoginPassword.error = "Admin password is required"
                isValid = false
            } else {
                binding.tilAdminLoginPassword.error = null
            }

            if (!isValid) return@setOnSingleClickListener

            lifecycleScope.launch {
                repository.ensureDefaultAdminAndDemoShops()
                val user = repository.getUserByEmail(email)

                if (user == null || !PasswordHasher.verifyPassword(password, user.passwordHash) || user.role != SessionManager.ROLE_ADMIN) {
                    Toast.makeText(requireContext(), "Access Denied: Invalid administrator credentials or role.", Toast.LENGTH_LONG).show()
                } else if (!user.isActive) {
                    Toast.makeText(requireContext(), "Admin account is disabled.", Toast.LENGTH_LONG).show()
                } else {
                    sessionManager.saveSession(user.userId, user.email, user.fullName, user.role)
                    Toast.makeText(requireContext(), "Authenticated as Administrator!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.nav_admin_dashboard)
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
            hint = "Admin Email Address *"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText("admin@malolos.com")
        }
        val etNewPassword = EditText(context).apply {
            hint = "New Admin Password (min 6 characters) *"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etEmail)
        layout.addView(etNewPassword)

        AlertDialog.Builder(context)
            .setTitle("🔑 Reset Administrator Password")
            .setMessage("Enter the admin email address and new administrator password.")
            .setView(layout)
            .setPositiveButton("Reset Password") { dialog, _ ->
                val email = etEmail.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()

                if (email.isEmpty() || newPassword.length < 6) {
                    Toast.makeText(context, "Please enter a valid admin email and new password (>= 6 chars).", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val user = repository.getUserByEmail(email)
                    if (user == null || user.role != SessionManager.ROLE_ADMIN) {
                        Toast.makeText(context, "No administrator account found with that email address.", Toast.LENGTH_LONG).show()
                    } else {
                        repository.resetPasswordByEmail(email, newPassword)
                        Toast.makeText(context, "Admin password updated successfully! Please log in with your new password.", Toast.LENGTH_LONG).show()
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
