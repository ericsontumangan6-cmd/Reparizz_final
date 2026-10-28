package com.example.fixflow.ui.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fixflow.databinding.ItemOnboardingSlideRulesBinding
import com.example.fixflow.databinding.ItemOnboardingSlideThankyouBinding
import com.example.fixflow.databinding.ItemOnboardingSlideWelcomeBinding
import com.example.fixflow.util.setOnSingleClickListener

class OnboardingAdapter(
    private val onClientLoginClick: () -> Unit,
    private val onClientRegisterClick: () -> Unit,
    private val onStaffLoginClick: () -> Unit,
    private val onStaffRegisterClick: () -> Unit,
    private val onAdminLoginClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_WELCOME = 0
        private const val TYPE_RULES = 1
        private const val TYPE_THANKYOU = 2
    }

    override fun getItemCount(): Int = 3

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_WELCOME
            1 -> TYPE_RULES
            else -> TYPE_THANKYOU
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_WELCOME -> {
                val binding = ItemOnboardingSlideWelcomeBinding.inflate(inflater, parent, false)
                WelcomeViewHolder(binding)
            }
            TYPE_RULES -> {
                val binding = ItemOnboardingSlideRulesBinding.inflate(inflater, parent, false)
                RulesViewHolder(binding)
            }
            else -> {
                val binding = ItemOnboardingSlideThankyouBinding.inflate(inflater, parent, false)
                ThankYouViewHolder(binding, onClientLoginClick, onClientRegisterClick, onStaffLoginClick, onStaffRegisterClick, onAdminLoginClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Static content layout items
    }

    class WelcomeViewHolder(binding: ItemOnboardingSlideWelcomeBinding) : RecyclerView.ViewHolder(binding.root)
    class RulesViewHolder(binding: ItemOnboardingSlideRulesBinding) : RecyclerView.ViewHolder(binding.root)

    class ThankYouViewHolder(
        binding: ItemOnboardingSlideThankyouBinding,
        onClientLoginClick: () -> Unit,
        onClientRegisterClick: () -> Unit,
        onStaffLoginClick: () -> Unit,
        onStaffRegisterClick: () -> Unit,
        onAdminLoginClick: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnClientLogin.setOnSingleClickListener { onClientLoginClick() }
            binding.btnClientRegister.setOnSingleClickListener { onClientRegisterClick() }
            binding.btnStaffLogin.setOnSingleClickListener { onStaffLoginClick() }
            binding.btnStaffRegister.setOnSingleClickListener { onStaffRegisterClick() }
            binding.btnAdminLogin.setOnSingleClickListener { onAdminLoginClick() }
        }
    }
}
