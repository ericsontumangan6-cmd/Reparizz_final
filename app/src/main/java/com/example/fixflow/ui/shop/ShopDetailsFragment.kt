package com.example.fixflow.ui.shop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fixflow.R
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.entity.ShopEntity
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentShopDetailsBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import kotlinx.coroutines.launch

class ShopDetailsFragment : Fragment() {

    private var _binding: FragmentShopDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager
    private var currentShop: ShopEntity? = null
    private var isFav: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopDetailsBinding.inflate(inflater, container, false)
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

        val shopId = arguments?.getLong("shopId", -1L) ?: -1L
        if (shopId != -1L) {
            observeShop(shopId)
        } else {
            Toast.makeText(requireContext(), "Error: Invalid shop record.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        setupListeners()
    }

    private fun observeShop(shopId: Long) {
        repository.getShopById(shopId).observe(viewLifecycleOwner) { shop ->
            if (shop != null) {
                currentShop = shop
                bindShopData(shop)
            }
        }

        val userId = sessionManager.getUserId()
        if (userId != -1L) {
            repository.isFavorite(userId, shopId).observe(viewLifecycleOwner) { count ->
                isFav = (count ?: 0) > 0
                binding.btnToggleFavorite.text = if (isFav) "⭐" else "☆"
            }
        }
    }

    private fun bindShopData(shop: ShopEntity) {
        binding.tvDetailsShopName.text = shop.shopName
        binding.tvDetailsCategory.text = "🏷️ Category: ${shop.category}"
        binding.tvDetailsAddress.text = "📍 Address: ${shop.address}"
        binding.tvDetailsContact.text = "📞 Phone: ${shop.contactNumber}"
        binding.tvDetailsHours.text = "⏰ Operating Hours: ${shop.operatingHours ?: "Mon - Sat: 8:00 AM - 6:00 PM"}"
        binding.tvDetailsDescription.text = shop.description ?: "Verified local service center in Malolos, Bulacan."
        binding.tvDetailsServices.text = shop.servicesOffered ?: "General utility and maintenance services."

        when (shop.approvalStatus) {
            "APPROVED" -> {
                binding.tvDetailsStatusBadge.text = "VERIFIED & APPROVED SHOP"
                binding.tvDetailsStatusBadge.setBackgroundColor("#4CAF50".toColorInt())
            }
            "PENDING_APPROVAL" -> {
                binding.tvDetailsStatusBadge.text = "PENDING ADMIN APPROVAL"
                binding.tvDetailsStatusBadge.setBackgroundColor("#FF9800".toColorInt())
            }
            "REJECTED" -> {
                binding.tvDetailsStatusBadge.text = "REJECTED SHOP"
                binding.tvDetailsStatusBadge.setBackgroundColor("#E53935".toColorInt())
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackShopDetails.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        binding.btnToggleFavorite.setOnSingleClickListener {
            val userId = sessionManager.getUserId()
            val shop = currentShop
            if (userId == -1L) {
                Toast.makeText(requireContext(), "Please sign in to save favorite shops.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_user_login)
                return@setOnSingleClickListener
            }

            if (shop != null) {
                lifecycleScope.launch {
                    val added = repository.toggleFavorite(userId, shop.shopId)
                    val msg = if (added) "Saved to favorites!" else "Removed from favorites."
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDetailsCall.setOnSingleClickListener {
            val phone = currentShop?.contactNumber
            if (!phone.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "No valid phone number available for this shop.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDetailsViewMap.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_dashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
