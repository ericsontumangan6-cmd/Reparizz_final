package com.example.fixflow.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fixflow.R
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentUserRegisterBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import kotlinx.coroutines.launch

class UserRegisterFragment : Fragment() {

    private var _binding: FragmentUserRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserRegisterBinding.inflate(inflater, container, false)
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
        binding.btnBackRegister.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        binding.tvGoToLogin.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_user_login)
        }

        binding.btnSubmitRegister.setOnSingleClickListener {
            val name = binding.etRegisterName.text?.toString()?.trim().orEmpty()
            val email = binding.etRegisterEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etRegisterPassword.text?.toString()?.trim().orEmpty()
            val confirmPassword = binding.etRegisterConfirmPassword.text?.toString()?.trim().orEmpty()

            var isValid = true

            if (name.isEmpty()) {
                binding.tilRegisterName.error = "Full name is required"
                isValid = false
            } else {
                binding.tilRegisterName.error = null
            }

            if (email.isEmpty() || !email.contains("@")) {
                binding.tilRegisterEmail.error = "Enter a valid email address"
                isValid = false
            } else {
                binding.tilRegisterEmail.error = null
            }

            if (password.length < 6) {
                binding.tilRegisterPassword.error = "Password must be at least 6 characters"
                isValid = false
            } else {
                binding.tilRegisterPassword.error = null
            }

            if (confirmPassword != password) {
                binding.tilRegisterConfirmPassword.error = "Passwords do not match"
                isValid = false
            } else {
                binding.tilRegisterConfirmPassword.error = null
            }

            if (!isValid) return@setOnSingleClickListener

            lifecycleScope.launch {
                val existing = repository.getUserByEmail(email)
                if (existing != null) {
                    binding.tilRegisterEmail.error = "An account with this email already exists"
                    Toast.makeText(requireContext(), "Email already registered. Try logging in.", Toast.LENGTH_SHORT).show()
                } else {
                    val userId = repository.registerUser(name, email, password, SessionManager.ROLE_USER)
                    sessionManager.saveSession(userId, email, name, SessionManager.ROLE_USER)
                    Toast.makeText(requireContext(), "Account created successfully! Welcome to RepaRizz.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.nav_user_dashboard)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
