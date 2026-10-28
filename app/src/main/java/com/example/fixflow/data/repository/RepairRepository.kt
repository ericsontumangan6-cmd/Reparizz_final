package com.example.fixflow.data.repository

import androidx.lifecycle.LiveData
import com.example.fixflow.data.dao.FavoriteDao
import com.example.fixflow.data.dao.PaymentDao
import com.example.fixflow.data.dao.RepairEstimateDao
import com.example.fixflow.data.dao.RepairJobDao
import com.example.fixflow.data.dao.RepairStatusHistoryDao
import com.example.fixflow.data.dao.RepairTestDao
import com.example.fixflow.data.dao.ServiceReviewDao
import com.example.fixflow.data.dao.ShopDao
import com.example.fixflow.data.dao.SparePartDao
import com.example.fixflow.data.dao.UserDao
import com.example.fixflow.data.dao.WarrantyClaimDao
import com.example.fixflow.data.dao.WarrantyDao
import com.example.fixflow.data.entity.FavoriteEntity
import com.example.fixflow.data.entity.Payment
import com.example.fixflow.data.entity.RepairEstimate
import com.example.fixflow.data.entity.RepairJob
import com.example.fixflow.data.entity.RepairStatusHistory
import com.example.fixflow.data.entity.RepairTest
import com.example.fixflow.data.entity.ServiceReview
import com.example.fixflow.data.entity.ShopEntity
import com.example.fixflow.data.entity.SparePart
import com.example.fixflow.data.entity.UserEntity
import com.example.fixflow.data.entity.Warranty
import com.example.fixflow.data.entity.WarrantyClaim
import com.example.fixflow.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

class RepairRepository(
    private val repairJobDao: RepairJobDao,
    private val estimateDao: RepairEstimateDao,
    private val historyDao: RepairStatusHistoryDao,
    private val testDao: RepairTestDao,
    private val paymentDao: PaymentDao,
    private val warrantyDao: WarrantyDao,
    private val claimDao: WarrantyClaimDao,
    private val userDao: UserDao,
    private val shopDao: ShopDao,
    private val favoriteDao: FavoriteDao,
    private val sparePartDao: SparePartDao? = null,
    private val serviceReviewDao: ServiceReviewDao? = null
) {

    // --- USER AUTHENTICATION & USER MANAGEMENT ---

    suspend fun registerUser(fullName: String, email: String, passwordRaw: String, role: String = "CUSTOMER"): Long = withContext(Dispatchers.IO) {
        val hashed = PasswordHasher.hashPassword(passwordRaw)
        val newUser = UserEntity(0, fullName, email, hashed, role, true, System.currentTimeMillis())
        return@withContext userDao.insertUser(newUser)
    }

    suspend fun createStaffAccount(fullName: String, email: String, passwordRaw: String): Long = withContext(Dispatchers.IO) {
        val hashed = PasswordHasher.hashPassword(passwordRaw)
        val staffUser = UserEntity(0, fullName, email, hashed, "STAFF", true, System.currentTimeMillis())
        return@withContext userDao.insertUser(staffUser)
    }

    fun getAllStaff(): LiveData<List<UserEntity>> = userDao.getAllStaff()

    suspend fun getAllStaffSync(): List<UserEntity> = withContext(Dispatchers.IO) {
        return@withContext userDao.getAllStaffSync()
    }

    fun getAllCustomers(): LiveData<List<UserEntity>> = userDao.getAllCustomers()

    suspend fun getAllCustomersSync(): List<UserEntity> = withContext(Dispatchers.IO) {
        return@withContext userDao.getAllCustomersSync()
    }

    suspend fun setUserActiveStatus(userId: Long, isActive: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateUserActiveStatus(userId, isActive)
    }

    suspend fun resetPasswordByEmail(email: String, newPasswordRaw: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmailSync(email)
        if (user == null) return@withContext false
        val hashed = PasswordHasher.hashPassword(newPasswordRaw)
        userDao.updatePasswordByEmail(email, hashed)
        return@withContext true
    }

    suspend fun linkCustomerToJob(jobId: String, customerUserId: Long) = withContext(Dispatchers.IO) {
        repairJobDao.updateJobCustomerUser(jobId, customerUserId)
    }

    suspend fun assignStaffToJob(jobId: String, staffId: Long, staffName: String) = withContext(Dispatchers.IO) {
        repairJobDao.updateJobAssignedStaff(jobId, staffId, staffName)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserByEmailSync(email)
    }

    suspend fun getUserById(userId: Long): UserEntity? = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserByIdSync(userId)
    }

    fun getUserByIdLiveData(userId: Long): LiveData<UserEntity> = userDao.getUserById(userId)

    suspend fun ensureDefaultAdminAndDemoShops() = withContext(Dispatchers.IO) {
        // Ensure Default Admin
        val admin = userDao.getUserByEmailSync("admin@malolos.com")
        if (admin == null) {
            val defaultAdmin = UserEntity(
                0,
                "Malolos Admin",
                "admin@malolos.com",
                PasswordHasher.hashPassword("admin123"),
                "ADMIN",
                true,
                System.currentTimeMillis()
            )
            userDao.insertUser(defaultAdmin)
        }

        // Ensure Default Staff
        var defaultStaff = userDao.getUserByEmailSync("staff@malolos.com")
        if (defaultStaff == null) {
            val staffUser = UserEntity(
                0,
                "Tech Juan (Senior Staff)",
                "staff@malolos.com",
                PasswordHasher.hashPassword("staff123"),
                "STAFF",
                true,
                System.currentTimeMillis()
            )
            val staffId = userDao.insertUser(staffUser)
            defaultStaff = userDao.getUserByIdSync(staffId)
        }

        // Ensure Default Customer
        var defaultCustomer = userDao.getUserByEmailSync("customer@malolos.com")
        if (defaultCustomer == null) {
            val custUser = UserEntity(
                0,
                "Maria Santos",
                "customer@malolos.com",
                PasswordHasher.hashPassword("customer123"),
                "CUSTOMER",
                true,
                System.currentTimeMillis()
            )
            val custId = userDao.insertUser(custUser)
            defaultCustomer = userDao.getUserByIdSync(custId)
        }

        // Ensure Demo Repair Job linked to Customer
        if (repairJobDao.allJobsSync.isEmpty() && defaultCustomer != null && defaultStaff != null) {
            val demoJob = RepairJob(
                "FF-0001",
                defaultCustomer.fullName,
                "(+63) 917-555-0199",
                defaultCustomer.userId,
                defaultStaff.userId,
                defaultStaff.fullName,
                "Smartphone",
                "Samsung",
                "Galaxy S22 Ultra",
                "Cracked AMOLED display and touch unresponsive.",
                "Slight scratches on edges, otherwise good.",
                "Original Box, S-Pen",
                null, null, null,
                "Requires screen assembly replacement.",
                "Faulty OLED panel",
                "Replace with original Samsung OLED display module.",
                4500.0,
                1200.0,
                0.0,
                null,
                null,
                RepairJob.STATUS_AWAITING_APPROVAL,
                false,
                System.currentTimeMillis() + (3 * 86400000L),
                5700.0,
                "UNPAID",
                "IN_SHOP",
                0L,
                System.currentTimeMillis(),
                System.currentTimeMillis()
            )
            createNewRepairJob(demoJob)

            val demoEst = RepairEstimate(
                0,
                "FF-0001",
                1,
                4500.0,
                1200.0,
                0.0,
                5700.0,
                "PENDING",
                null,
                0L,
                "Initial repair estimate prepared by Staff."
            )
            estimateDao.insertEstimate(demoEst)
        }

        // Ensure Default Demo Shops in Malolos, Bulacan
        if (shopDao.totalShopCountSync == 0) {
            val demoShops = listOf(
                ShopEntity(
                    0,
                    "FBN IT SOLUTION (Malolos HQ)",
                    "Electronics",
                    "MacArthur Highway, Malolos Crossing, Malolos, 3000 Bulacan",
                    "(+63) 917-123-4567",
                    "Premier IT and electronics service center in Malolos Crossing specializing in motherboard repair, laptop upgrades, and phone servicing.",
                    "Mobile phone repair, Laptop repair, Electronics troubleshooting, Component cleaning",
                    "Mon - Sat: 8:00 AM - 6:00 PM",
                    14.8527,
                    120.8160,
                    "APPROVED",
                    null,
                    1,
                    true,
                    System.currentTimeMillis()
                ),
                ShopEntity(
                    0,
                    "RepaRizz BulSU Branch",
                    "Electronics",
                    "Guinhawa, MacArthur Hwy (Near BulSU Gate 2), Malolos, 3000 Bulacan",
                    "(+63) 918-987-6543",
                    "Express gadget and smartphone repair center serving BulSU students and Malolos residents.",
                    "Screen replacement, Battery replacement, Water damage recovery, Quick cleaning",
                    "Mon - Sun: 9:00 AM - 8:00 PM",
                    14.8560,
                    120.8185,
                    "APPROVED",
                    null,
                    1,
                    true,
                    System.currentTimeMillis()
                ),
                ShopEntity(
                    0,
                    "Capitol Tech & Electronics Care",
                    "Electronics",
                    "Paseo del Congreso, Brgy. Catmon, Malolos, 3000 Bulacan",
                    "(+63) 919-456-7890",
                    "Desktop PC, laptop, and office electronics specialist near the Malolos Provincial Capitol.",
                    "Desktop system unit assembly, Power supply repair, Monitor diagnostics, General cleaning",
                    "Mon - Fri: 8:30 AM - 5:30 PM",
                    14.8480,
                    120.8120,
                    "APPROVED",
                    null,
                    1,
                    true,
                    System.currentTimeMillis()
                ),
                ShopEntity(
                    0,
                    "Malolos Deep Cleaning & Sanitizing",
                    "Cleaning",
                    "Mabolo Road, Brgy. Mabolo, Malolos, 3000 Bulacan",
                    "(+63) 920-333-4444",
                    "Professional home, office, and electronic appliance deep cleaning team in Malolos.",
                    "House cleaning, Office cleaning, Deep cleaning, Ultrasonic electronic cleaning",
                    "Mon - Sat: 8:00 AM - 6:00 PM",
                    14.8420,
                    120.8200,
                    "APPROVED",
                    null,
                    1,
                    true,
                    System.currentTimeMillis()
                ),
                ShopEntity(
                    0,
                    "Bulacan Master Plumbing Care",
                    "Plumbing",
                    "Plaridel-Malolos Bypass Rd, Brgy. Sumapang Matanda, Malolos, 3000 Bulacan",
                    "(+63) 922-555-6666",
                    "Certified residential and commercial plumbing experts for Malolos homes and businesses.",
                    "Pipe repair, Leak detection, Faucet and plumbing installation",
                    "24/7 Emergency Service",
                    14.8600,
                    120.8250,
                    "APPROVED",
                    null,
                    1,
                    true,
                    System.currentTimeMillis()
                )
            )

            for (shop in demoShops) {
                shopDao.insertShop(shop)
            }
        }

        // Seed default spare parts inventory
        if (sparePartDao != null && sparePartDao.getAllPartsSync().isEmpty()) {
            sparePartDao.insertPart(SparePart(0, "Samsung S22 Ultra OLED Assembly", "Screen", 8, 4500.0, 3))
            sparePartDao.insertPart(SparePart(0, "iPhone 13 Pro Max Battery Pack", "Battery", 15, 1800.0, 5))
            sparePartDao.insertPart(SparePart(0, "MacBook Pro M1 Thermal Paste & Fan", "Tools/Paste", 20, 850.0, 5))
            sparePartDao.insertPart(SparePart(0, "Type-C Charging Port Connector", "Charging Port", 30, 350.0, 10))
            sparePartDao.insertPart(SparePart(0, "Laptop DDR4 8GB RAM Module", "Storage/RAM", 12, 1200.0, 4))
        }
    }

    // --- SHOPS & DIRECTORY ---

    fun getApprovedShops(): LiveData<List<ShopEntity>> = shopDao.approvedShops

    fun searchApprovedShops(query: String?, category: String?): LiveData<List<ShopEntity>> {
        val cleanQuery = query?.trim()?.ifEmpty { "" } ?: ""
        val cleanCategory = if (category == "All" || category.isNullOrEmpty()) "" else category
        return shopDao.searchApprovedShops(cleanQuery, cleanCategory)
    }

    fun getShopById(shopId: Long): LiveData<ShopEntity> = shopDao.getShopById(shopId)

    suspend fun getShopByIdSync(shopId: Long): ShopEntity? = withContext(Dispatchers.IO) {
        return@withContext shopDao.getShopByIdSync(shopId)
    }

    fun getPendingShops(): LiveData<List<ShopEntity>> = shopDao.pendingShops

    fun getAllShops(): LiveData<List<ShopEntity>> = shopDao.allShops

    fun getShopsBySubmittedUser(userId: Long): LiveData<List<ShopEntity>> = shopDao.getShopsBySubmittedUser(userId)

    fun getApprovedShopCount(): LiveData<Int> = shopDao.approvedCount
    fun getPendingShopCount(): LiveData<Int> = shopDao.pendingCount
    fun getRejectedShopCount(): LiveData<Int> = shopDao.rejectedCount

    suspend fun registerShopSubmission(
        shopName: String,
        category: String,
        address: String,
        contactNumber: String,
        description: String?,
        servicesOffered: String?,
        submittedByUserId: Long
    ): Long = withContext(Dispatchers.IO) {
        val newShop = ShopEntity(
            0,
            shopName.trim(),
            category.trim(),
            address.trim(),
            contactNumber.trim(),
            description?.trim(),
            servicesOffered?.trim(),
            "Mon - Sat: 8:00 AM - 6:00 PM",
            14.8527,
            120.8160,
            "PENDING_APPROVAL",
            null,
            submittedByUserId,
            false,
            System.currentTimeMillis()
        )
        return@withContext shopDao.insertShop(newShop)
    }

    suspend fun approveShop(shopId: Long) = withContext(Dispatchers.IO) {
        val shop = shopDao.getShopByIdSync(shopId) ?: return@withContext
        shop.approvalStatus = "APPROVED"
        shop.rejectionReason = null
        shopDao.updateShop(shop)
    }

    suspend fun rejectShop(shopId: Long, reason: String?) = withContext(Dispatchers.IO) {
        val shop = shopDao.getShopByIdSync(shopId) ?: return@withContext
        shop.approvalStatus = "REJECTED"
        shop.rejectionReason = reason ?: "Submission declined by Admin."
        shopDao.updateShop(shop)
    }

    suspend fun updateShop(shop: ShopEntity) = withContext(Dispatchers.IO) {
        shopDao.updateShop(shop)
    }

    suspend fun deleteShop(shopId: Long) = withContext(Dispatchers.IO) {
        val shop = shopDao.getShopByIdSync(shopId) ?: return@withContext
        shopDao.deleteShop(shop)
    }

    // --- FAVORITES ---

    fun isFavorite(userId: Long, shopId: Long): LiveData<Int> = favoriteDao.isFavorite(userId, shopId)

    fun getFavoriteShopsForUser(userId: Long): LiveData<List<ShopEntity>> = favoriteDao.getFavoriteShopsForUser(userId)

    suspend fun toggleFavorite(userId: Long, shopId: Long): Boolean = withContext(Dispatchers.IO) {
        val isFav = favoriteDao.isFavoriteSync(userId, shopId) > 0
        if (isFav) {
            favoriteDao.deleteFavorite(userId, shopId)
            return@withContext false
        } else {
            val fav = FavoriteEntity(0, userId, shopId, System.currentTimeMillis())
            favoriteDao.insertFavorite(fav)
            return@withContext true
        }
    }

    // --- REPAIR JOBS (PRESERVED INFRASTRUCTURE) ---

    fun getAllJobs(): LiveData<List<RepairJob>> = repairJobDao.allJobs

    fun getJobById(jobId: String): LiveData<RepairJob?> = repairJobDao.getJobById(jobId)

    suspend fun getJobByIdSync(jobId: String): RepairJob? = withContext(Dispatchers.IO) {
        repairJobDao.getJobByIdSync(jobId)
    }

    fun searchAndFilterJobs(query: String?, status: String?): LiveData<List<RepairJob>> {
        val cleanQuery = query?.trim()?.ifEmpty { "" } ?: ""
        val cleanStatus = if (status.isNullOrEmpty()) "All" else status
        return repairJobDao.searchAndFilterJobs(cleanQuery, cleanStatus, System.currentTimeMillis())
    }

    fun getJobsForCustomer(customerUserId: Long): LiveData<List<RepairJob>> = repairJobDao.getJobsForCustomer(customerUserId)

    fun searchAndFilterCustomerJobs(customerUserId: Long, query: String?, status: String?): LiveData<List<RepairJob>> {
        val cleanQuery = query?.trim()?.ifEmpty { "" } ?: ""
        val cleanStatus = if (status.isNullOrEmpty()) "All" else status
        return repairJobDao.searchAndFilterCustomerJobs(customerUserId, cleanQuery, cleanStatus, System.currentTimeMillis())
    }

    fun getCustomerJobsCount(customerUserId: Long): LiveData<Int> = repairJobDao.getCustomerJobsCount(customerUserId)

    fun getJobsForStaff(staffId: Long): LiveData<List<RepairJob>> = repairJobDao.getJobsForStaff(staffId)

    fun searchAndFilterStaffJobs(staffId: Long, query: String?, status: String?): LiveData<List<RepairJob>> {
        val cleanQuery = query?.trim()?.ifEmpty { "" } ?: ""
        val cleanStatus = if (status.isNullOrEmpty()) "All" else status
        return repairJobDao.searchAndFilterStaffJobs(staffId, cleanQuery, cleanStatus, System.currentTimeMillis())
    }

    fun getHistoryForJob(jobId: String): LiveData<List<RepairStatusHistory>> = historyDao.getHistoryForJob(jobId)

    fun getEstimatesForJob(jobId: String): LiveData<List<RepairEstimate>> = estimateDao.getEstimatesForJob(jobId)

    fun getLatestEstimate(jobId: String): LiveData<RepairEstimate> = estimateDao.getLatestEstimate(jobId)

    fun getTestsForJob(jobId: String): LiveData<List<RepairTest>> = testDao.getTestsForJob(jobId)

    fun getPaymentsForJob(jobId: String): LiveData<List<Payment>> = paymentDao.getPaymentsForJob(jobId)

    fun getTotalJobsCount(): LiveData<Int> = repairJobDao.totalJobsCount

    fun getTotalRepairPaidForJob(jobId: String): LiveData<Double> = paymentDao.getTotalRepairPaidForJob(jobId)

    fun getTotalTipsForJob(jobId: String): LiveData<Double> = paymentDao.getTotalTipsForJob(jobId)

    fun getTotalCashReceivedForJob(jobId: String): LiveData<Double> = paymentDao.getTotalCashReceivedForJob(jobId)

    fun getWarrantyForJob(jobId: String): LiveData<Warranty?> = warrantyDao.getWarrantyForJob(jobId)

    fun getClaimsForJob(jobId: String): LiveData<List<WarrantyClaim>> = claimDao.getClaimsForJob(jobId)

    fun getAllClaims(): LiveData<List<WarrantyClaim>> = claimDao.allClaims

    fun getRecentHistory(limit: Int = 10): LiveData<List<RepairStatusHistory>> = historyDao.getRecentHistory(limit)

    // Dashboard metrics
    fun getAwaitingDiagnosisCount(): LiveData<Int> = repairJobDao.awaitingDiagnosisCount
    fun getAwaitingApprovalCount(): LiveData<Int> = repairJobDao.awaitingApprovalJobsCount
    fun getInRepairCount(): LiveData<Int> = repairJobDao.inRepairJobsCount
    fun getFailedTestingCount(): LiveData<Int> = repairJobDao.failedTestingCount
    fun getCompletedCount(): LiveData<Int> = repairJobDao.completedJobsCount
    fun getAwaitingCollectionCount(): LiveData<Int> = repairJobDao.awaitingCollectionCount
    fun getOverdueActiveCount(): LiveData<Int> = repairJobDao.getOverdueActiveJobsCount(System.currentTimeMillis())

    suspend fun createNewRepairJob(job: RepairJob) = withContext(Dispatchers.IO) {
        repairJobDao.insertJob(job)
        val initialHistory = RepairStatusHistory(
            0,
            job.jobId,
            null,
            job.currentStatus,
            System.currentTimeMillis(),
            "Device intake registered."
        )
        historyDao.insertHistory(initialHistory)
    }

    suspend fun updateAfterPhoto(jobId: String, photoPath: String?) = withContext(Dispatchers.IO) {
        val currentJob = repairJobDao.getJobByIdSync(jobId) ?: return@withContext
        currentJob.afterPhotoPath = photoPath
        currentJob.updatedAt = System.currentTimeMillis()
        repairJobDao.updateJob(currentJob)

        val historyEntry = RepairStatusHistory(
            0,
            jobId,
            currentJob.currentStatus,
            currentJob.currentStatus,
            System.currentTimeMillis(),
            "Attached after-repair completion/release proof photo."
        )
        historyDao.insertHistory(historyEntry)
    }

    suspend fun saveDiagnosisAndEstimate(
        jobId: String,
        diagnosisNotes: String,
        suspectedFault: String?,
        recommendedRepair: String?,
        partsCost: Double,
        laborCost: Double,
        additionalCharges: Double
    ) = withContext(Dispatchers.IO) {
        val currentJob = repairJobDao.getJobByIdSync(jobId) ?: return@withContext
        val newTotal = partsCost + laborCost + additionalCharges
        val oldTotal = currentJob.totalEstimatedCost

        currentJob.diagnosisNotes = diagnosisNotes
        currentJob.suspectedFault = suspectedFault
        currentJob.recommendedRepair = recommendedRepair
        currentJob.estimatedPartsCost = partsCost
        currentJob.estimatedLaborCost = laborCost
        currentJob.additionalCharges = additionalCharges
        currentJob.updatedAt = System.currentTimeMillis()

        if (currentJob.customerApproved == true && newTotal > oldTotal) {
            currentJob.customerApproved = false
            currentJob.currentStatus = RepairJob.STATUS_AWAITING_APPROVAL
            
            val latestEst = estimateDao.getLatestEstimateSync(jobId)
            val nextVersion = if (latestEst != null) latestEst.estimateVersion + 1 else 1

            val newEstimate = RepairEstimate(
                0,
                jobId,
                nextVersion,
                partsCost,
                laborCost,
                additionalCharges,
                newTotal,
                "PENDING",
                null,
                0,
                "Revised estimate requiring updated customer approval."
            )
            estimateDao.insertEstimate(newEstimate)

            val revHistory = RepairStatusHistory(
                0,
                jobId,
                currentJob.currentStatus,
                RepairJob.STATUS_AWAITING_APPROVAL,
                System.currentTimeMillis(),
                "Estimate revised to ₱${String.format(Locale.US, "%.2f", newTotal)}. Required customer re-approval."
            )
            historyDao.insertHistory(revHistory)
        } else if (currentJob.customerApproved == null) {
            val latestEst = estimateDao.getLatestEstimateSync(jobId)
            val nextVersion = if (latestEst != null) latestEst.estimateVersion + 1 else 1

            val newEstimate = RepairEstimate(
                0,
                jobId,
                nextVersion,
                partsCost,
                laborCost,
                additionalCharges,
                newTotal,
                "PENDING",
                null,
                0,
                "Initial repair estimate created."
            )
            estimateDao.insertEstimate(newEstimate)
        }

        repairJobDao.updateJob(currentJob)
    }

    suspend fun recordCustomerDecision(
        jobId: String,
        approved: Boolean,
        contactMethod: String?,
        notes: String?
    ) = withContext(Dispatchers.IO) {
        val currentJob = repairJobDao.getJobByIdSync(jobId) ?: return@withContext
        val prevStatus = currentJob.currentStatus

        currentJob.customerApproved = approved
        if (approved) {
            currentJob.currentStatus = RepairJob.STATUS_REPAIRING
            currentJob.rejectionReason = null
        } else {
            currentJob.currentStatus = RepairJob.STATUS_REJECTED
            currentJob.rejectionReason = notes ?: "Customer declined repair cost."
        }
        currentJob.updatedAt = System.currentTimeMillis()

        repairJobDao.updateJob(currentJob)

        val latestEst = estimateDao.getLatestEstimateSync(jobId)
        if (latestEst != null) {
            latestEst.approvalStatus = if (approved) "APPROVED" else "REJECTED"
            latestEst.contactMethod = contactMethod
            latestEst.approvalTimestamp = System.currentTimeMillis()
            latestEst.staffNotes = notes
            estimateDao.insertEstimate(latestEst)
        }

        val decisionText = if (approved) "Staff recorded customer APPROVAL" else "Staff recorded customer REJECTION"
        val methodText = if (!contactMethod.isNullOrEmpty()) " via $contactMethod" else ""
        val historyEntry = RepairStatusHistory(
            0,
            jobId,
            prevStatus,
            currentJob.currentStatus,
            System.currentTimeMillis(),
            "$decisionText$methodText. Notes: ${notes ?: "None"}"
        )
        historyDao.insertHistory(historyEntry)
    }

    suspend fun updateJobStatus(
        jobId: String,
        newStatus: String,
        staffNotes: String?,
        customerApproved: Boolean? = null,
        rejectionReason: String? = null,
        testingPassed: Boolean? = null
    ) = withContext(Dispatchers.IO) {
        val currentJob = repairJobDao.getJobByIdSync(jobId) ?: return@withContext
        val prevStatus = currentJob.currentStatus

        if (customerApproved != null) {
            currentJob.customerApproved = customerApproved
        }
        if (rejectionReason != null) {
            currentJob.rejectionReason = rejectionReason
        }
        if (testingPassed != null) {
            currentJob.testingPassed = testingPassed
        }

        if (RepairJob.STATUS_AWAITING_COLLECTION.equals(newStatus)) {
            currentJob.collectionStatus = "AWAITING_COLLECTION"
        } else if (RepairJob.STATUS_RELEASED.equals(newStatus)) {
            currentJob.collectionStatus = "RELEASED"
            currentJob.releaseDate = System.currentTimeMillis()
        }

        currentJob.currentStatus = newStatus
        currentJob.updatedAt = System.currentTimeMillis()

        repairJobDao.updateJob(currentJob)

        val historyEntry = RepairStatusHistory(
            0,
            jobId,
            prevStatus,
            newStatus,
            System.currentTimeMillis(),
            staffNotes
        )
        historyDao.insertHistory(historyEntry)
    }

    suspend fun saveTestingChecklist(
        jobId: String,
        tests: List<RepairTest>
    ) = withContext(Dispatchers.IO) {
        testDao.deleteTestsForJob(jobId)
        testDao.insertAllTests(tests)

        val allPassed = tests.all { it.result == "PASSED" }
        val currentJob = repairJobDao.getJobByIdSync(jobId)
        if (currentJob != null) {
            currentJob.testingPassed = allPassed
            repairJobDao.updateJob(currentJob)
        }
    }

    suspend fun recordPayment(
        jobId: String,
        repairAmount: Double,
        tipAmount: Double,
        method: String,
        notes: String?
    ) = withContext(Dispatchers.IO) {
        val totalCashReceived = repairAmount + tipAmount
        val payment = Payment(0, jobId, repairAmount, tipAmount, totalCashReceived, method, System.currentTimeMillis(), notes)
        paymentDao.insertPayment(payment)

        val totalRepairPaid = paymentDao.getTotalRepairPaidForJobSync(jobId) ?: 0.0
        val currentJob = repairJobDao.getJobByIdSync(jobId) ?: return@withContext

        val costToPay = if (currentJob.finalRepairCost > 0) currentJob.finalRepairCost else currentJob.totalEstimatedCost
        if (currentJob.finalRepairCost <= 0) {
            currentJob.finalRepairCost = costToPay
        }

        if (totalRepairPaid >= costToPay && costToPay > 0) {
            currentJob.paymentStatus = "FULLY_PAID"
        } else if (totalRepairPaid > 0) {
            currentJob.paymentStatus = "PARTIALLY_PAID"
        } else {
            currentJob.paymentStatus = "UNPAID"
        }

        repairJobDao.updateJob(currentJob)

        val remainingBal = Math.max(0.0, costToPay - totalRepairPaid)
        val tipNote = if (tipAmount > 0) " (includes ₱${String.format(Locale.US, "%.2f", tipAmount)} tip)" else ""
        val historyEntry = RepairStatusHistory(
            0,
            jobId,
            currentJob.currentStatus,
            currentJob.currentStatus,
            System.currentTimeMillis(),
            "Recorded payment of ₱${String.format(Locale.US, "%.2f", totalCashReceived)}$tipNote via $method. Remaining repair balance: ₱${String.format(Locale.US, "%.2f", remainingBal)}"
        )
        historyDao.insertHistory(historyEntry)
    }

    suspend fun recordDeviceRelease(jobId: String, staffNotes: String?) = withContext(Dispatchers.IO) {
        updateJobStatus(jobId, RepairJob.STATUS_RELEASED, "Device released to customer. $staffNotes".trim())
    }

    suspend fun saveWarranty(warranty: Warranty) = withContext(Dispatchers.IO) {
        warrantyDao.insertWarranty(warranty)
    }

    suspend fun createWarrantyClaim(claim: WarrantyClaim) = withContext(Dispatchers.IO) {
        claimDao.insertClaim(claim)

        val historyEntry = RepairStatusHistory(
            0,
            claim.originalJobId,
            null,
            "Warranty Claim Registered",
            System.currentTimeMillis(),
            "Warranty claim logged: ${claim.reportedIssue}"
        )
        historyDao.insertHistory(historyEntry)
    }

    suspend fun updateWarrantyClaim(claim: WarrantyClaim) = withContext(Dispatchers.IO) {
        claimDao.updateClaim(claim)
    }

    suspend fun generateNextJobId(): String = withContext(Dispatchers.IO) {
        val count = repairJobDao.allJobsSync.size + 1
        return@withContext String.format(Locale.US, "FF-%04d", count)
    }

    data class AnalyticsSummary(
        val totalJobs: Int,
        val awaitingDiagnosis: Int,
        val awaitingApproval: Int,
        val currentlyRepairing: Int,
        val failedTesting: Int,
        val completedJobs: Int,
        val awaitingCollection: Int,
        val overdueActiveJobs: Int,
        val avgTurnaroundTimeHours: Double,
        val totalRepairRevenue: Double,
        val totalCustomerTips: Double,
        val totalCashReceived: Double,
        val totalOutstandingBalances: Double,
        val totalWarrantyClaims: Int,
        val topProblemTypes: List<Pair<String, Int>>
    )

    suspend fun calculateAnalytics(): AnalyticsSummary = withContext(Dispatchers.IO) {
        val allJobs = repairJobDao.allJobsSync
        val allHistory = historyDao.allHistorySync
        val allClaims = claimDao.allClaimsSync

        val totalActiveJobs = allJobs.count { it.currentStatus != RepairJob.STATUS_RELEASED && it.currentStatus != RepairJob.STATUS_REJECTED && it.currentStatus != RepairJob.STATUS_CANCELLED }
        val awaitingDiagnosis = allJobs.count { it.currentStatus == RepairJob.STATUS_DIAGNOSING || it.currentStatus == RepairJob.STATUS_RECEIVED }
        val awaitingApproval = allJobs.count { it.currentStatus == RepairJob.STATUS_AWAITING_APPROVAL }
        val currentlyRepairing = allJobs.count { it.currentStatus == RepairJob.STATUS_REPAIRING }
        val failedTesting = allJobs.count { it.currentStatus == RepairJob.STATUS_TESTING && !it.testingPassed }
        val completedJobs = allJobs.count { it.currentStatus == RepairJob.STATUS_COMPLETED || it.currentStatus == RepairJob.STATUS_AWAITING_COLLECTION || it.currentStatus == RepairJob.STATUS_RELEASED }
        val awaitingCollection = allJobs.count { it.currentStatus == RepairJob.STATUS_AWAITING_COLLECTION || it.collectionStatus == "AWAITING_COLLECTION" }
        val overdueActiveJobs = allJobs.count { it.isOverdue }

        var totalTurnaroundMs = 0L
        var completedCountWithHistory = 0

        val historyByJob = allHistory.groupBy { it.jobId }
        for (job in allJobs) {
            if (RepairJob.STATUS_COMPLETED.equals(job.currentStatus) || RepairJob.STATUS_RELEASED.equals(job.currentStatus)) {
                val histories = historyByJob[job.jobId] ?: emptyList()
                val received = histories.find { RepairJob.STATUS_RECEIVED.equals(it.newStatus) } ?: histories.firstOrNull()
                val completed = histories.find { RepairJob.STATUS_COMPLETED.equals(it.newStatus) } ?: histories.lastOrNull()
                if (received != null && completed != null && completed.timestamp >= received.timestamp) {
                    totalTurnaroundMs += (completed.timestamp - received.timestamp)
                    completedCountWithHistory++
                }
            }
        }

        val avgHours = if (completedCountWithHistory > 0) {
            TimeUnit.MILLISECONDS.toHours(totalTurnaroundMs / completedCountWithHistory).toDouble()
        } else {
            0.0
        }

        val totalRepairRevenue = paymentDao.totalRepairRevenueAllSync ?: 0.0
        val totalCustomerTips = paymentDao.totalTipsAllSync ?: 0.0
        val totalCashReceived = paymentDao.totalCashAllSync ?: 0.0

        var totalOutstandingBalances = 0.0
        for (job in allJobs) {
            if (!RepairJob.STATUS_CANCELLED.equals(job.currentStatus) && !RepairJob.STATUS_REJECTED.equals(job.currentStatus)) {
                val cost = if (job.finalRepairCost > 0) job.finalRepairCost else job.totalEstimatedCost
                val paidRepair = paymentDao.getTotalRepairPaidForJobSync(job.jobId) ?: 0.0
                val bal = Math.max(0.0, cost - paidRepair)
                if (bal > 0) {
                    totalOutstandingBalances += bal
                }
            }
        }

        val problemCounts = allJobs.map { it.reportedIssue.trim() }
            .filter { it.isNotEmpty() }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        return@withContext AnalyticsSummary(
            totalJobs = totalActiveJobs,
            awaitingDiagnosis = awaitingDiagnosis,
            awaitingApproval = awaitingApproval,
            currentlyRepairing = currentlyRepairing,
            failedTesting = failedTesting,
            completedJobs = completedJobs,
            awaitingCollection = awaitingCollection,
            overdueActiveJobs = overdueActiveJobs,
            avgTurnaroundTimeHours = avgHours,
            totalRepairRevenue = totalRepairRevenue,
            totalCustomerTips = totalCustomerTips,
            totalCashReceived = totalCashReceived,
            totalOutstandingBalances = totalOutstandingBalances,
            totalWarrantyClaims = allClaims.size,
            topProblemTypes = problemCounts
        )
    }

    // --- SPARE PARTS INVENTORY SYSTEM ---
    fun getAllSpareParts(): LiveData<List<SparePart>>? = sparePartDao?.getAllParts()

    suspend fun getAllSparePartsSync(): List<SparePart> = withContext(Dispatchers.IO) {
        return@withContext sparePartDao?.getAllPartsSync() ?: emptyList()
    }

    suspend fun insertSparePart(part: SparePart) = withContext(Dispatchers.IO) {
        sparePartDao?.insertPart(part)
    }

    suspend fun deductSparePartStock(partId: Long, qty: Int): Boolean = withContext(Dispatchers.IO) {
        val rows = sparePartDao?.deductStock(partId, qty) ?: 0
        return@withContext rows > 0
    }

    // --- SERVICE REVIEWS & RATINGS SYSTEM ---
    fun getReviewForJob(jobId: String): LiveData<ServiceReview>? = serviceReviewDao?.getReviewForJob(jobId)

    suspend fun submitServiceReview(jobId: String, ratingStars: Int, comment: String?) = withContext(Dispatchers.IO) {
        val review = ServiceReview(0, jobId, ratingStars, comment, System.currentTimeMillis())
        serviceReviewDao?.insertReview(review)
    }

    fun getAverageRating(): LiveData<Double>? = serviceReviewDao?.getAverageRating()
}
