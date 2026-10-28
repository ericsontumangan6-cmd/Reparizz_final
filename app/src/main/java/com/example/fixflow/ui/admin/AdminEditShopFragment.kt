package com.example.fixflow.ui.admin

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.entity.ShopEntity
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentAdminEditShopBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import kotlinx.coroutines.launch

class AdminEditShopFragment : Fragment() {

    private var _binding: FragmentAdminEditShopBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager
    private var currentShop: ShopEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEditShopBinding.inflate(inflater, container, false)
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

        setupCategoryDropdown()
        setupListeners()
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf("Cleaning", "Plumbing", "Electronics")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, categories)
        binding.actvEditShopCategory.setAdapter(adapter)
    }

    private fun observeShop(shopId: Long) {
        repository.getShopById(shopId).observe(viewLifecycleOwner) { shop ->
            if (shop != null) {
                currentShop = shop
                binding.etEditShopName.setText(shop.shopName)
                binding.actvEditShopCategory.setText(shop.category, false)
                binding.etEditShopAddress.setText(shop.address)
                binding.etEditShopContact.setText(shop.contactNumber)
                binding.etEditShopDescription.setText(shop.description ?: "")
                binding.etEditShopServices.setText(shop.servicesOffered ?: "")
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackEditShop.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        binding.btnAdminDeleteShop.setOnSingleClickListener {
            val shop = currentShop ?: return@setOnSingleClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Shop Record")
                .setMessage("Are you sure you want to permanently delete ${shop.shopName}?")
                .setPositiveButton("Yes, Delete") { _, _ ->
                    lifecycleScope.launch {
                        repository.deleteShop(shop.shopId)
                        Toast.makeText(requireContext(), "Shop record deleted.", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnSaveEditShop.setOnSingleClickListener {
            val shop = currentShop ?: return@setOnSingleClickListener
            val name = binding.etEditShopName.text?.toString()?.trim().orEmpty()
            val category = binding.actvEditShopCategory.text?.toString()?.trim().orEmpty()
            val address = binding.etEditShopAddress.text?.toString()?.trim().orEmpty()
            val contact = binding.etEditShopContact.text?.toString()?.trim().orEmpty()
            val description = binding.etEditShopDescription.text?.toString()?.trim().orEmpty()
            val services = binding.etEditShopServices.text?.toString()?.trim().orEmpty()

            if (name.isEmpty() || address.isEmpty() || contact.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnSingleClickListener
            }

            shop.shopName = name
            shop.category = category
            shop.address = address
            shop.contactNumber = contact
            shop.description = description
            shop.servicesOffered = services

            lifecycleScope.launch {
                repository.updateShop(shop)
                Toast.makeText(requireContext(), "Shop details updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
