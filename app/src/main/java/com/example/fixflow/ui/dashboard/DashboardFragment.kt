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
import com.example.fixflow.databinding.FragmentDashboardBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RecentActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupRoleBasedUi()
        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    private fun setupRoleBasedUi() {
        if (!sessionManager.isLoggedIn()) {
            binding.tvDashboardTitle.text = " RepaRizz Guest View"
            binding.tvDashboardSubtitle.text = "Browsing in Guest Mode. Sign in or create an account for full access."
            binding.btnQuickAddJob.visibility = View.GONE
            binding.btnDashboardLogout.text = "🔑 Sign In"
            return
        }

        when (sessionManager.getUserRole()) {
            SessionManager.ROLE_ADMIN -> {
                findNavController().navigate(R.id.nav_admin_dashboard)
                return
            }
            SessionManager.ROLE_STAFF -> {
                findNavController().navigate(R.id.nav_staff_dashboard)
                return
            }
            SessionManager.ROLE_USER -> {
                findNavController().navigate(R.id.nav_user_dashboard)
                return
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = RecentActivityAdapter { jobId ->
            val bundle = Bundle().apply {
                putString("jobId", jobId)
            }
            findNavController().navigate(R.id.nav_repair_detail, bundle)
        }
        binding.rvRecentActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@DashboardFragment.adapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        if (sessionManager.isCustomer()) {
            val customerUserId = sessionManager.getUserId()
            viewModel.getCustomerJobsCount(customerUserId).observe(viewLifecycleOwner) { count ->
                binding.tvTotalJobs.text = (count ?: 0).toString()
            }
        } else {
            viewModel.totalJobsCount.observe(viewLifecycleOwner) { count ->
                binding.tvTotalJobs.text = (count ?: 0).toString()
            }
        }

        viewModel.awaitingDiagnosisCount.observe(viewLifecycleOwner) { count ->
            binding.tvAwaitingDiagnosis.text = (count ?: 0).toString()
        }

        viewModel.awaitingApprovalCount.observe(viewLifecycleOwner) { count ->
            binding.tvAwaitingApproval.text = (count ?: 0).toString()
        }

        viewModel.inRepairCount.observe(viewLifecycleOwner) { count ->
            binding.tvInRepair.text = (count ?: 0).toString()
        }

        viewModel.failedTestingCount.observe(viewLifecycleOwner) { count ->
            binding.tvFailedTesting.text = (count ?: 0).toString()
        }

        viewModel.completedCount.observe(viewLifecycleOwner) { count ->
            binding.tvCompleted.text = (count ?: 0).toString()
        }

        viewModel.awaitingCollectionCount.observe(viewLifecycleOwner) { count ->
            binding.tvAwaitingCollection.text = (count ?: 0).toString()
        }

        viewModel.overdueActiveCount.observe(viewLifecycleOwner) { count ->
            binding.tvOverdueJobs.text = (count ?: 0).toString()
        }

        viewModel.recentActivities.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.tvEmptyActivity.visibility = View.VISIBLE
                binding.rvRecentActivities.visibility = View.GONE
            } else {
                binding.tvEmptyActivity.visibility = View.GONE
                binding.rvRecentActivities.visibility = View.VISIBLE
                adapter.submitList(list)
            }
        }
    }

    private fun setupListeners() {
        binding.btnDashboardLogout.setOnSingleClickListener {
            sessionManager.clearSession()
            Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.nav_welcome)
        }

        binding.btnQuickAddJob.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_new_job)
        }

        binding.btnViewAllJobs.setOnSingleClickListener {
            navigateToJobsWithFilter("All")
        }

        binding.btnShopMap.setOnSingleClickListener {
            findNavController().navigate(R.id.nav_shop_map)
        }

        binding.cardTotalJobs.setOnSingleClickListener {
            navigateToJobsWithFilter("All")
        }

        binding.cardAwaitingDiagnosis.setOnSingleClickListener {
            navigateToJobsWithFilter("Diagnosing")
        }

        binding.cardAwaitingApproval.setOnSingleClickListener {
            navigateToJobsWithFilter("Awaiting Approval")
        }

        binding.cardInRepair.setOnSingleClickListener {
            navigateToJobsWithFilter("Repairing")
        }

        binding.cardFailedTesting.setOnSingleClickListener {
            navigateToJobsWithFilter("Testing")
        }

        binding.cardCompletedRepairs.setOnSingleClickListener {
            navigateToJobsWithFilter("Completed")
        }

        binding.cardAwaitingCollection.setOnSingleClickListener {
            navigateToJobsWithFilter("Awaiting Collection")
        }

        binding.cardOverdueJobs.setOnSingleClickListener {
            navigateToJobsWithFilter("Overdue")
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
