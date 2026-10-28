package com.example.fixflow

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.fixflow.data.db.FixFlowDatabase
import com.example.fixflow.data.repository.RepairRepository
import com.example.fixflow.databinding.ActivityMainBinding
import com.example.fixflow.util.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        sessionManager.clearSession() // Always start fresh on launch on the Full-Screen Welcome Greetings Carousel

        // Seed DB
        val db = FixFlowDatabase.getDatabase(this)
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

        lifecycleScope.launch {
            repository.ensureDefaultAdminAndDemoShops()
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        if (navController != null) {
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.nav_dashboard,
                    R.id.nav_admin_dashboard,
                    R.id.nav_staff_dashboard,
                    R.id.nav_user_dashboard,
                    R.id.nav_jobs,
                    R.id.nav_new_job,
                    R.id.nav_analytics,
                    R.id.nav_shop_map,
                    R.id.nav_user_profile -> {
                        binding.bottomNav.visibility = View.VISIBLE

                        // Sync selected tab highlight
                        val expectedMenuId = when (destination.id) {
                            R.id.nav_jobs -> R.id.nav_jobs
                            R.id.nav_new_job -> R.id.nav_new_job
                            R.id.nav_analytics -> R.id.nav_analytics
                            else -> R.id.nav_dashboard
                        }
                        if (binding.bottomNav.selectedItemId != expectedMenuId) {
                            binding.bottomNav.menu.findItem(expectedMenuId)?.isChecked = true
                        }
                    }
                    else -> {
                        binding.bottomNav.visibility = View.GONE
                    }
                }
            }

            binding.bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_dashboard -> {
                        val target = when {
                            sessionManager.isAdmin() -> R.id.nav_admin_dashboard
                            sessionManager.isStaff() -> R.id.nav_staff_dashboard
                            sessionManager.isCustomer() -> R.id.nav_user_dashboard
                            else -> R.id.nav_dashboard
                        }
                        if (navController.currentDestination?.id != target) {
                            navController.navigate(target)
                        }
                        true
                    }
                    R.id.nav_jobs -> {
                        if (navController.currentDestination?.id != R.id.nav_jobs) {
                            navController.navigate(R.id.nav_jobs)
                        }
                        true
                    }
                    R.id.nav_new_job -> {
                        if (navController.currentDestination?.id != R.id.nav_new_job) {
                            navController.navigate(R.id.nav_new_job)
                        }
                        true
                    }
                    R.id.nav_analytics -> {
                        if (navController.currentDestination?.id != R.id.nav_analytics) {
                            navController.navigate(R.id.nav_analytics)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    fun showAppOpeningWelcomeDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 16)
            setBackgroundColor("#1B2230".toColorInt())
        }

        val tvSubtitle = TextView(this).apply {
            text = "✨ \"Giving broken gadgets their rizz back.\""
            setTextColor("#35D6C3".toColorInt())
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        }

        val tvGreeting = TextView(this).apply {
            text = "Good day! Welcome to RepaRizz, your complete mobile repair shop management, tracking, and service report system."
            setTextColor("#FFFFFF".toColorInt())
            textSize = 13f
            setPadding(0, 0, 0, 12)
        }

        val tvRulesHeader = TextView(this).apply {
            text = "📜 Official Shop Rules & Guidelines:"
            setTextColor("#FF9800".toColorInt())
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 8, 0, 4)
        }

        val tvRulesBody = TextView(this).apply {
            text = "1. 💵 Philippine Peso (₱): All estimates, parts, labor, and tips are in ₱.\n" +
                   "2. 🛠️ 8-Stage Repair Process: Received ➔ Diagnosing ➔ Awaiting Approval ➔ Repairing ➔ Testing ➔ Completed ➔ Collection ➔ Released.\n" +
                   "3. 📋 Customer Approvals: Repair work starts only after customer approval.\n" +
                   "4. 🛡️ Quality Testing & Warranty: Hardware testing & downloadable digital warranty.\n" +
                   "5. 💳 Separate Tip Tracking: Customer tips are recorded separately from repair fees."
            setTextColor("#A7B3C4".toColorInt())
            textSize = 12f
            setPadding(0, 0, 0, 12)
        }

        val tvThankYou = TextView(this).apply {
            text = "🙏 Thank you for choosing RepaRizz! We are committed to fast, transparent, and quality gadget repairs."
            setTextColor("#35D6C3".toColorInt())
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 4, 0, 12)
        }

        layout.addView(tvSubtitle)
        layout.addView(tvGreeting)
        layout.addView(tvRulesHeader)
        layout.addView(tvRulesBody)
        layout.addView(tvThankYou)

        AlertDialog.Builder(this)
            .setTitle("📱 Welcome to RepaRizz!")
            .setView(layout)
            .setPositiveButton("🚀 Finish & Enter App") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}
