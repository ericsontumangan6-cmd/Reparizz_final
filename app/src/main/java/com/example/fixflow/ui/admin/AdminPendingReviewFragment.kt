package com.example.fixflow.ui.admin

import android.graphics.Typeface
import android.os.Bundle
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
import com.example.fixflow.data.entity.ShopEntity
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentAdminPendingReviewBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AdminPendingReviewFragment : Fragment() {

    private var _binding: FragmentAdminPendingReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RepairRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPendingReviewBinding.inflate(inflater, container, false)
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

        binding.btnBackPendingReview.setOnSingleClickListener {
            findNavController().navigateUp()
        }

        observePendingShops()
    }

    private fun observePendingShops() {
        repository.getPendingShops().observe(viewLifecycleOwner) { pendingShops ->
            binding.layoutPendingShopsContainer.removeAllViews()

            if (pendingShops.isNullOrEmpty()) {
                binding.tvEmptyPendingShops.visibility = View.VISIBLE
            } else {
                binding.tvEmptyPendingShops.visibility = View.GONE

                val context = requireContext()
                for (shop in pendingShops) {
                    val card = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundResource(R.drawable.bg_card)
                        setPadding(24, 20, 24, 20)
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.bottomMargin = 16
                        layoutParams = params
                    }

                    val titleTv = TextView(context).apply {
                        text = shop.shopName
                        setTextColor("#FFFFFF".toColorInt())
                        textSize = 16f
                        setTypeface(null, Typeface.BOLD)
                    }

                    val catTv = TextView(context).apply {
                        text = "🏷️ Category: ${shop.category}"
                        setTextColor("#35D6C3".toColorInt())
                        textSize = 12f
                    }

                    val addressTv = TextView(context).apply {
                        text = "📍 Address: ${shop.address}"
                        setTextColor("#A7B3C4".toColorInt())
                        textSize = 12f
                    }

                    val contactTv = TextView(context).apply {
                        text = "📞 Contact: ${shop.contactNumber}"
                        setTextColor("#A7B3C4".toColorInt())
                        textSize = 12f
                    }

                    val actionsLayout = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        val p = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        p.topMargin = 16
                        layoutParams = p
                    }

                    val btnApprove = MaterialButton(context).apply {
                        text = "✓ Approve"
                        setBackgroundColor("#4CAF50".toColorInt())
                        setTextColor("#FFFFFF".toColorInt())
                        val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        p.rightMargin = 8
                        layoutParams = p
                        setOnSingleClickListener {
                            lifecycleScope.launch {
                                repository.approveShop(shop.shopId)
                                Toast.makeText(context, "Approved ${shop.shopName}!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    val btnReject = MaterialButton(context).apply {
                        text = "✗ Reject"
                        setBackgroundColor("#E53935".toColorInt())
                        setTextColor("#FFFFFF".toColorInt())
                        val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        p.leftMargin = 8
                        layoutParams = p
                        setOnSingleClickListener {
                            showRejectReasonDialog(shop)
                        }
                    }

                    actionsLayout.addView(btnApprove)
                    actionsLayout.addView(btnReject)

                    card.addView(titleTv)
                    card.addView(catTv)
                    card.addView(addressTv)
                    card.addView(contactTv)
                    card.addView(actionsLayout)

                    binding.layoutPendingShopsContainer.addView(card)
                }
            }
        }
    }

    private fun showRejectReasonDialog(shop: ShopEntity) {
        val input = EditText(requireContext()).apply {
            hint = "Reason for rejection (e.g. Invalid address)..."
        }
        val container = LinearLayout(requireContext()).apply {
            setPadding(48, 16, 48, 16)
            addView(input)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Reject Shop Submission")
            .setView(container)
            .setPositiveButton("Reject Submission") { _, _ ->
                val reason = input.text.toString().trim()
                lifecycleScope.launch {
                    repository.rejectShop(shop.shopId, reason)
                    Toast.makeText(requireContext(), "Rejected submission: ${shop.shopName}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
