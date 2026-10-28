package com.example.fixflow.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fixflow.R
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentShopRegistrationBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import kotlinx.coroutines.launch

class ShopRegistrationFragment : Fragment() {

    private var _binding: FragmentShopRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopRegistrationBinding.inflate(inflater, container, false)
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

        setupCategoryDropdown()
        setupListeners()
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf("Cleaning", "Plumbing", "Electronics")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvRegShopCategory.setAdapter(adapter)
        binding.actvRegShopCategory.setText(categories[0], false)
    }

    private fun setupListeners() {
        binding.btnBackRegisterShop.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmitShopRegistration.setOnSingleClickListener {
            val name = binding.etRegShopName.text?.toString()?.trim().orEmpty()
            val category = binding.actvRegShopCategory.text?.toString()?.trim().orEmpty()
            val address = binding.etRegShopAddress.text?.toString()?.trim().orEmpty()
            val contact = binding.etRegShopContact.text?.toString()?.trim().orEmpty()
            val description = binding.etRegShopDescription.text?.toString()?.trim().orEmpty()
            val services = binding.etRegShopServices.text?.toString()?.trim().orEmpty()

            var isValid = true

            if (name.isEmpty()) {
                binding.tilRegShopName.error = "Shop name is required"
                isValid = false
            } else {
                binding.tilRegShopName.error = null
            }

            if (address.isEmpty()) {
                binding.tilRegShopAddress.error = "Address is required"
                isValid = false
            } else if (!address.contains("Malolos", ignoreCase = true) && !address.contains("Bulacan", ignoreCase = true)) {
                binding.tilRegShopAddress.error = "Address must be within Malolos, Bulacan"
                isValid = false
            } else {
                binding.tilRegShopAddress.error = null
            }

            if (contact.isEmpty()) {
                binding.tilRegShopContact.error = "Contact phone number is required"
                isValid = false
            } else {
                binding.tilRegShopContact.error = null
            }

            if (description.isEmpty()) {
                binding.tilRegShopDescription.error = "Description is required"
                isValid = false
            } else {
                binding.tilRegShopDescription.error = null
            }

            if (!isValid) return@setOnSingleClickListener

            val userId = sessionManager.getUserId().let { if (it == -1L) 1L else it }

            lifecycleScope.launch {
                repository.registerShopSubmission(
                    shopName = name,
                    category = category,
                    address = address,
                    contactNumber = contact,
                    description = description,
                    servicesOffered = services,
                    submittedByUserId = userId
                )
                Toast.makeText(requireContext(), "Shop submission received! Awaiting Admin approval.", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
