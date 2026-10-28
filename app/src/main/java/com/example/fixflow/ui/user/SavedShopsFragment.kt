package com.example.fixflow.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fixflow.R
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentSavedShopsBinding
import com.example.fixflow.util.SessionManager

class SavedShopsFragment : Fragment() {

    private var _binding: FragmentSavedShopsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedShopsBinding.inflate(inflater, container, false)
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

        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            binding.tvEmptySavedShops.visibility = View.VISIBLE
            binding.tvEmptySavedShops.text = "Please sign in to view your saved favorite shops."
            return
        }

        repository.getFavoriteShopsForUser(userId).observe(viewLifecycleOwner) { shops ->
            if (shops.isNullOrEmpty()) {
                binding.tvEmptySavedShops.visibility = View.VISIBLE
            } else {
                binding.tvEmptySavedShops.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
