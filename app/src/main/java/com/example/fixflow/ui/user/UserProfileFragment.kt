package com.example.fixflow.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fixflow.MainActivity
import com.example.fixflow.R
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentUserProfileBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
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

        bindUserProfile()
        setupListeners()
    }

    private fun bindUserProfile() {
        if (!sessionManager.isLoggedIn()) {
            binding.tvProfileName.text = "Guest User"
            binding.tvProfileEmail.text = "Not signed in"
            binding.tvProfileRoleBadge.text = "GUEST"
            binding.tvProfileRoleBadge.setBackgroundColor("#757575".toColorInt())
            return
        }

        binding.tvProfileName.text = sessionManager.getUserName()
        binding.tvProfileEmail.text = sessionManager.getUserEmail()
        binding.tvProfileRoleBadge.text = sessionManager.getUserRole()

        val userId = sessionManager.getUserId()
        if (userId != -1L) {
            repository.getShopsBySubmittedUser(userId).observe(viewLifecycleOwner) { submissions ->
                binding.layoutUserSubmissionsContainer.removeAllViews()
                if (submissions.isNullOrEmpty()) {
                    val emptyTv = TextView(requireContext()).apply {
                        text = "You haven't submitted any shop registrations yet."
                        setTextColor("#A7B3C4".toColorInt())
                        textSize = 12f
                        setPadding(0, 8, 0, 8)
                    }
                    binding.layoutUserSubmissionsContainer.addView(emptyTv)
                } else {
                    for (shop in submissions) {
                        val rowTv = TextView(requireContext()).apply {
                            text = "• ${shop.shopName} [Status: ${shop.approvalStatus}]"
                            setTextColor("#FFFFFF".toColorInt())
                            textSize = 13f
                            setPadding(0, 6, 0, 6)
                        }
                        binding.layoutUserSubmissionsContainer.addView(rowTv)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnViewWelcomeRules.setOnSingleClickListener {
            (activity as? MainActivity)?.showAppOpeningWelcomeDialog()
        }

        binding.btnUserLogout.setOnSingleClickListener {
            sessionManager.clearSession()
            Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.nav_welcome)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
