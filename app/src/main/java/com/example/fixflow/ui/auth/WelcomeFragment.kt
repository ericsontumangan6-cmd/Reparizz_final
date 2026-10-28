package com.example.fixflow.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.fixflow.R
import com.example.fixflow.databinding.FragmentWelcomeBinding
import com.example.fixflow.util.setOnSingleClickListener

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOnboardingCarousel()
    }

    private fun setupOnboardingCarousel() {
        val adapter = OnboardingAdapter(
            onClientLoginClick = { findNavController().navigate(R.id.nav_user_login) },
            onClientRegisterClick = { findNavController().navigate(R.id.nav_user_register) },
            onStaffLoginClick = { findNavController().navigate(R.id.nav_user_login) },
            onStaffRegisterClick = { findNavController().navigate(R.id.nav_user_register) },
            onAdminLoginClick = { findNavController().navigate(R.id.nav_admin_login) }
        )

        binding.viewPagerOnboarding.adapter = adapter

        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateSlideUi(position)
            }
        })

        binding.btnSkipOnboarding.setOnSingleClickListener {
            binding.viewPagerOnboarding.currentItem = 2
        }

        binding.btnNextSlide.setOnSingleClickListener {
            val current = binding.viewPagerOnboarding.currentItem
            if (current < 2) {
                binding.viewPagerOnboarding.currentItem = current + 1
            } else {
                findNavController().navigate(R.id.nav_user_login)
            }
        }

        binding.btnPrevSlide.setOnSingleClickListener {
            val current = binding.viewPagerOnboarding.currentItem
            if (current > 0) {
                binding.viewPagerOnboarding.currentItem = current - 1
            }
        }
    }

    private fun updateSlideUi(position: Int) {
        when (position) {
            0 -> {
                binding.tvSlideIndicator.text = "●   ○   ○"
                binding.btnPrevSlide.visibility = View.INVISIBLE
                binding.btnNextSlide.text = "Next ➔"
                binding.btnSkipOnboarding.visibility = View.VISIBLE
            }
            1 -> {
                binding.tvSlideIndicator.text = "○   ●   ○"
                binding.btnPrevSlide.visibility = View.VISIBLE
                binding.btnNextSlide.text = "Next ➔"
                binding.btnSkipOnboarding.visibility = View.VISIBLE
            }
            2 -> {
                binding.tvSlideIndicator.text = "○   ○   ●"
                binding.btnPrevSlide.visibility = View.VISIBLE
                binding.btnNextSlide.text = "Log In / Register 🔑"
                binding.btnSkipOnboarding.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
