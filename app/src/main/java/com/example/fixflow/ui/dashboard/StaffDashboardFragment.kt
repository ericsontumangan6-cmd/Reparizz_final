package com.example.fixflow.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fixflow.R
import com.example.fixflow.databinding.FragmentStaffDashboardBinding
import com.example.fixflow.ui.jobs.RepairJobAdapter
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener

class StaffDashboardFragment : Fragment() {

    private var _binding: FragmentStaffDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RepairJobAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Role Permission Guard
        if (!sessionManager.isStaff()) {
            Toast.makeText(requireContext(), "Access Denied: Staff or Admin role required.", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        binding.tvStaffHeaderSubtitle.text = "Logged in as Staff: ${sessionManager.getUserName()}"

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
        binding.rvStaffQueue.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@StaffDashboardFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.awaitingDiagnosisCount.observe(viewLifecycleOwner) { count ->
            binding.tvStaffStatDiag.text = (count ?: 0).toString()
        }

        viewModel.inRepairCount.observe(viewLifecycleOwner) { count ->
            binding.tvStaffStatRepair.text = (count ?: 0).toString()
        }

        viewModel.getCustomerJobs(sessionManager.getUserId()).observe(viewLifecycleOwner) { jobs ->
            // If jobs list empty, fallback to overall jobs list for staff
            if (jobs.isNullOrEmpty()) {
                binding.tvStaffEmptyQueue.visibility = View.VISIBLE
                binding.rvStaffQueue.visibility = View.GONE
            } else {
                binding.tvStaffEmptyQueue.visibility = View.GONE
                binding.rvStaffQueue.visibility = View.VISIBLE
                adapter.submitList(jobs)
            }
        }
    }

    private fun setupListeners() {
        binding.btnStaffLogout.setOnSingleClickListener {
            sessionManager.clearSession()
            Toast.makeText(requireContext(), "Logged out of Staff portal.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.nav_welcome)
        }

        binding.btnStaffNewIntake.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_new_job)
        }

        binding.btnStaffDiagnosis.setOnSingleClickListener {
            navigateToJobsWithFilter("Diagnosing")
        }

        binding.btnStaffTesting.setOnSingleClickListener {
            navigateToJobsWithFilter("Testing")
        }

        binding.btnStaffUpdateProgress.setOnSingleClickListener {
            navigateToJobsWithFilter("Repairing")
        }

        binding.btnStaffPayments.setOnSingleClickListener {
            navigateToJobsWithFilter("Completed")
        }

        binding.cardStaffAwaitingDiag.setOnSingleClickListener {
            navigateToJobsWithFilter("Diagnosing")
        }

        binding.cardStaffInRepair.setOnSingleClickListener {
            navigateToJobsWithFilter("Repairing")
        }
    }

    private fun navigateToJobsWithFilter(filterStatus: String) {
        val bundle = Bundle().apply {
            putString("filterStatus", filterStatus)
        }
        findNavController().navigate(R.id.nav_jobs, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
