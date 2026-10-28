package com.example.fixflow.ui.dashboard

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fixflow.R
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.FragmentUserDashboardBinding
import com.example.fixflow.ui.jobs.RepairJobAdapter
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import kotlinx.coroutines.launch

class UserDashboardFragment : Fragment() {

    private var _binding: FragmentUserDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RepairJobAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Security Guard
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Please log in to view your repairs.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.nav_welcome)
            return
        }

        binding.tvUserHeaderSubtitle.text = "Welcome, ${sessionManager.getUserName()}! Tracking your device repairs."

        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = RepairJobAdapter { jobId ->
            val bundle = Bundle().apply {
                putString("jobId", jobId)
            }
            findNavController().navigate(R.id.nav_repair_detail, bundle)
        }
        binding.rvUserMyJobs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@UserDashboardFragment.adapter
        }
    }

    private fun observeViewModel() {
        val customerUserId = sessionManager.getUserId()

        viewModel.getCustomerJobsCount(customerUserId).observe(viewLifecycleOwner) { count ->
            binding.tvUserMyDevicesCount.text = (count ?: 0).toString()
        }

        viewModel.getCustomerJobs(customerUserId).observe(viewLifecycleOwner) { jobs ->
            if (jobs.isNullOrEmpty()) {
                binding.tvUserEmptyJobs.visibility = View.VISIBLE
                binding.rvUserMyJobs.visibility = View.GONE
            } else {
                binding.tvUserEmptyJobs.visibility = View.GONE
                binding.rvUserMyJobs.visibility = View.VISIBLE
                adapter.submitList(jobs)

                val pendingApprovalCount = jobs.count { it.currentStatus == "Awaiting Approval" && it.customerApproved == null }
                if (pendingApprovalCount > 0) {
                    binding.cardUserEstimatesNotice.visibility = View.VISIBLE
                } else {
                    binding.cardUserEstimatesNotice.visibility = View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnUserLogout.setOnSingleClickListener {
            sessionManager.clearSession()
            Toast.makeText(requireContext(), "Logged out.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.nav_welcome)
        }

        binding.btnUserProfile.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_user_profile)
        }

        binding.btnUserClaimJob.setOnSingleClickListener {
            showClaimJobDialog()
        }

        binding.btnUserViewPendingEstimates.setOnSingleClickListener {
            val bundle = Bundle().apply {
                putString("filterStatus", "Awaiting Approval")
            }
            findNavController().navigate(R.id.nav_jobs, bundle)
        }

        binding.btnUserTrackStatus.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_jobs)
        }

        binding.btnUserPaymentsHistory.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_jobs)
        }
    }

    private fun showClaimJobDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etJobId = EditText(context).apply {
            hint = "Repair Job ID (e.g. FF-0001) *"
        }
        val etPhone = EditText(context).apply {
            hint = "Contact Number linked to intake *"
            inputType = InputType.TYPE_CLASS_PHONE
        }

        layout.addView(etJobId)
        layout.addView(etPhone)

        val db = FixFlowDatabase.getDatabase(context)
        val repository = RepairRepository(
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

        AlertDialog.Builder(context)
            .setTitle("🔗 Link / Claim Existing Repair Job")
            .setMessage("Enter your Repair Job ID and the phone number provided during shop intake to securely link the device to your account.")
            .setView(layout)
            .setPositiveButton("Link Device") { dialog, _ ->
                val jobId = etJobId.text.toString().trim().uppercase()
                val phone = etPhone.text.toString().trim()

                if (jobId.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(context, "Please enter both Repair Job ID and contact phone number.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val job = repository.getJobByIdSync(jobId)
                    if (job == null) {
                        Toast.makeText(context, "No repair job found with ID $jobId.", Toast.LENGTH_LONG).show()
                    } else {
                        val cleanJobPhone = job.contactNumber.replace("[^0-9]".toRegex(), "")
                        val cleanInputPhone = phone.replace("[^0-9]".toRegex(), "")

                        if (cleanJobPhone.endsWith(cleanInputPhone) || cleanInputPhone.endsWith(cleanJobPhone) || job.contactNumber.contains(phone)) {
                            repository.linkCustomerToJob(jobId, sessionManager.getUserId())
                            Toast.makeText(context, "Success! Repair job $jobId is now linked to your account.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Contact number verification failed for Job ID $jobId.", Toast.LENGTH_LONG).show()
                        }
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
