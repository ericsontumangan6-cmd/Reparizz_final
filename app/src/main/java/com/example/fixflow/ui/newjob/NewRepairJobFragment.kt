package com.example.fixflow.ui.newjob

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fixflow.R
import com.example.fixflow.data.entity.UserEntity
import com.example.fixflow.databinding.FragmentNewRepairJobBinding
import com.example.fixflow.util.SessionManager
import com.example.fixflow.util.setOnSingleClickListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewRepairJobFragment : Fragment() {

    private var _binding: FragmentNewRepairJobBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewJobViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var selectedPhotoUri: Uri? = null
    private var selectedDeadlineCalendar: Calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 2)
    }

    private var customerList: List<UserEntity> = emptyList()
    private var staffList: List<UserEntity> = emptyList()
    private var selectedCustomerUserId: Long? = null
    private var selectedStaffUserId: Long? = null
    private var selectedStaffName: String? = null

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            binding.ivPhotoPreview.setImageURI(uri)
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.btnRemovePhoto.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewRepairJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        if (sessionManager.isCustomer()) {
            Toast.makeText(requireContext(), "Access Denied: Only Staff and Admin can register repair intakes.", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        setupDropdowns()
        setupDatePicker()
        observeViewModel()
        setupListeners()
        viewModel.fetchNextJobId()
    }

    private fun setupDropdowns() {
        val deviceTypes = arrayOf("Smartphone", "Tablet", "Laptop", "Desktop", "Smartwatch", "Audio Device", "Other")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, deviceTypes)
        binding.actvDeviceType.setAdapter(typeAdapter)
        binding.actvDeviceType.setText(deviceTypes[0], false)

        binding.actvDeviceType.setOnClickListener {
            binding.actvDeviceType.showDropDown()
        }
        binding.actvDeviceType.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actvDeviceType.showDropDown()
        }

        updateBrandSuggestions(deviceTypes[0])

        binding.actvDeviceType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = deviceTypes.getOrNull(position) ?: "Smartphone"
            updateBrandSuggestions(selectedType)
        }

        binding.actvDeviceBrand.setOnClickListener {
            binding.actvDeviceBrand.showDropDown()
        }
        binding.actvDeviceBrand.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actvDeviceBrand.showDropDown()
        }

        binding.actvDeviceBrand.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentType = binding.actvDeviceType.text.toString().trim()
                val currentBrand = s?.toString()?.trim().orEmpty()
                updateModelSuggestions(currentType, currentBrand)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.actvDeviceBrand.setOnItemClickListener { _, _, _, _ ->
            val currentType = binding.actvDeviceType.text.toString().trim()
            val currentBrand = binding.actvDeviceBrand.text.toString().trim()
            updateModelSuggestions(currentType, currentBrand)
        }

        binding.actvDeviceModel.setOnClickListener {
            binding.actvDeviceModel.showDropDown()
        }
        binding.actvDeviceModel.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actvDeviceModel.showDropDown()
        }
    }

    private fun updateBrandSuggestions(deviceType: String) {
        val brands = when (deviceType) {
            "Smartphone" -> arrayOf("Apple", "Samsung", "Xiaomi", "OPPO", "vivo", "Realme", "Huawei", "Motorola", "Google", "OnePlus", "Sony", "Infinix", "Tecno", "ASUS", "Lenovo", "Nokia", "Nothing", "Honor", "ZTE", "TCL")
            "Tablet" -> arrayOf("Apple (iPad)", "Samsung (Galaxy Tab)", "Xiaomi (Pad)", "Lenovo", "Huawei", "Realme", "Amazon (Fire)", "Microsoft (Surface)", "ASUS", "Nokia")
            "Laptop" -> arrayOf("Apple (MacBook)", "ASUS", "Acer", "Lenovo", "Dell", "HP", "MSI", "Samsung", "Huawei", "Gigabyte", "Razer", "Microsoft (Surface Pro)")
            "Desktop" -> arrayOf("Custom PC Build", "ASUS", "Acer", "Dell", "HP", "Lenovo", "Apple (iMac / Mac mini)", "MSI")
            "Smartwatch" -> arrayOf("Apple (Apple Watch)", "Samsung (Galaxy Watch)", "Huawei", "Xiaomi", "Garmin", "Fitbit", "Amazfit", "Realme", "OPPO")
            "Audio Device" -> arrayOf("Sony", "Apple (AirPods)", "Bose", "JBL", "Sennheiser", "Marshall", "Samsung (Galaxy Buds)", "Soundcore", "Realme", "Xiaomi")
            else -> arrayOf("Generic", "OEM", "Other Brand")
        }
        val brandAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, brands)
        binding.actvDeviceBrand.setAdapter(brandAdapter)

        val currentType = binding.actvDeviceType.text.toString().trim()
        val currentBrand = binding.actvDeviceBrand.text.toString().trim()
        updateModelSuggestions(currentType, currentBrand)
    }

    private fun updateModelSuggestions(deviceType: String, brand: String) {
        val cleanBrand = brand.lowercase(Locale.ROOT)
        val models = when {
            cleanBrand.contains("apple") || cleanBrand.contains("iphone") -> arrayOf("iPhone 15 Pro Max", "iPhone 15 Pro", "iPhone 15", "iPhone 14 Pro Max", "iPhone 14", "iPhone 13", "iPhone 12", "iPhone 11", "iPad Air", "iPad Pro", "MacBook Air M2", "MacBook Pro M3")
            cleanBrand.contains("samsung") -> arrayOf("Galaxy S24 Ultra", "Galaxy S23 Ultra", "Galaxy S22 Ultra", "Galaxy A54 5G", "Galaxy A34 5G", "Galaxy Z Fold 5", "Galaxy Z Flip 5", "Galaxy Tab S9")
            cleanBrand.contains("xiaomi") -> arrayOf("Xiaomi 14 Pro", "Xiaomi 13T Pro", "Redmi Note 13 Pro+", "Redmi Note 12", "Poco F5", "Poco X6 Pro")
            cleanBrand.contains("oppo") -> arrayOf("Reno 11 Pro 5G", "Reno 10 Pro", "Find N3 Flip", "A98 5G", "A78")
            cleanBrand.contains("vivo") -> arrayOf("V30 Pro 5G", "V29 5G", "Y36", "Y27", "X100 Pro")
            cleanBrand.contains("realme") -> arrayOf("Realme 12 Pro+", "Realme 11 Pro 5G", "Realme C67", "Realme C55")
            cleanBrand.contains("asus") -> arrayOf("ROG Phone 8 Pro", "Zenfone 10", "TUF Gaming F15", "ROG Strix G16", "ZenBook 14")
            cleanBrand.contains("acer") -> arrayOf("Nitro 5", "Predator Helios 300", "Aspire 5", "Swift 3")
            cleanBrand.contains("lenovo") -> arrayOf("Legion 5 Pro", "Ideapad Gaming 3", "ThinkPad X1 Carbon", "Yoga Slim 7")
            cleanBrand.contains("dell") -> arrayOf("Inspiron 15", "G15 Gaming", "XPS 13", "Latitude 3520")
            cleanBrand.contains("hp") -> arrayOf("Victus 16", "Pavilion Gaming", "ENVY x360", "ProBook 450 G8")
            else -> arrayOf("Standard Model", "Pro Model", "Plus Model", "Ultra Model")
        }
        val modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, models)
        binding.actvDeviceModel.setAdapter(modelAdapter)
    }

    private fun setupDatePicker() {
        updateDateDisplay()

        binding.etCompletionDeadline.setOnClickListener {
            val year = selectedDeadlineCalendar.get(Calendar.YEAR)
            val month = selectedDeadlineCalendar.get(Calendar.MONTH)
            val day = selectedDeadlineCalendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDeadlineCalendar.set(Calendar.YEAR, selectedYear)
                    selectedDeadlineCalendar.set(Calendar.MONTH, selectedMonth)
                    selectedDeadlineCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                    val hour = selectedDeadlineCalendar.get(Calendar.HOUR_OF_DAY)
                    val minute = selectedDeadlineCalendar.get(Calendar.MINUTE)

                    TimePickerDialog(
                        requireContext(),
                        { _, selectedHour, selectedMinute ->
                            selectedDeadlineCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                            selectedDeadlineCalendar.set(Calendar.MINUTE, selectedMinute)
                            updateDateDisplay()
                        },
                        hour,
                        minute,
                        false
                    ).show()
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.US)
        binding.etCompletionDeadline.setText(sdf.format(selectedDeadlineCalendar.time))
    }

    private fun observeViewModel() {
        viewModel.generatedJobId.observe(viewLifecycleOwner) { jobId ->
            binding.tvGeneratedJobId.text = jobId
        }

        viewModel.getCustomers().observe(viewLifecycleOwner) { customers ->
            customerList = customers ?: emptyList()
        }

        viewModel.getStaff().observe(viewLifecycleOwner) { staff ->
            staffList = staff ?: emptyList()
        }

        viewModel.saveSuccessEvent.observe(viewLifecycleOwner) { jobId ->
            if (jobId != null) {
                Toast.makeText(requireContext(), "Repair job $jobId created successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveEvent()
                val bundle = Bundle().apply {
                    putString("jobId", jobId)
                }
                findNavController().navigate(R.id.action_new_job_to_repair_detail, bundle)
            }
        }
    }

    private fun setupListeners() {
        binding.btnAttachPhoto.setOnSingleClickListener {
            pickPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnRemovePhoto.setOnSingleClickListener {
            selectedPhotoUri = null
            binding.ivPhotoPreview.setImageURI(null)
            binding.ivPhotoPreview.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
        }

        binding.btnSaveRepairJob.setOnSingleClickListener {
            submitForm()
        }
    }

    private fun submitForm() {
        val customerName = binding.etCustomerName.text?.toString()?.trim().orEmpty()
        val contactNumber = binding.etContactNumber.text?.toString()?.trim().orEmpty()
        val deviceType = binding.actvDeviceType.text.toString().trim()
        val deviceBrand = binding.actvDeviceBrand.text.toString().trim()
        val deviceModel = binding.actvDeviceModel.text.toString().trim()
        val reportedIssue = binding.etReportedIssue.text?.toString()?.trim().orEmpty()
        val deviceCondition = binding.etDeviceCondition.text?.toString()?.trim().orEmpty()
        val accessoriesReceived = binding.etAccessoriesReceived.text?.toString()?.trim().orEmpty()

        var isValid = true

        if (customerName.isEmpty()) {
            binding.tilCustomerName.error = "Customer name is required"
            isValid = false
        } else {
            binding.tilCustomerName.error = null
        }

        if (contactNumber.isEmpty()) {
            binding.tilContactNumber.error = "Contact number is required"
            isValid = false
        } else if (contactNumber.length < 7) {
            binding.tilContactNumber.error = "Enter a valid contact number"
            isValid = false
        } else {
            binding.tilContactNumber.error = null
        }

        if (deviceBrand.isEmpty()) {
            binding.tilDeviceBrand.error = "Device brand is required"
            isValid = false
        } else {
            binding.tilDeviceBrand.error = null
        }

        if (deviceModel.isEmpty()) {
            binding.tilDeviceModel.error = "Device model is required"
            isValid = false
        } else {
            binding.tilDeviceModel.error = null
        }

        if (reportedIssue.isEmpty()) {
            binding.tilReportedIssue.error = "Reported problem description is required"
            isValid = false
        } else {
            binding.tilReportedIssue.error = null
        }

        if (deviceCondition.isEmpty()) {
            binding.tilDeviceCondition.error = "Device physical condition is required"
            isValid = false
        } else {
            binding.tilDeviceCondition.error = null
        }

        if (!isValid) return

        // Link customer account if email or name matches an existing customer
        val matchedCustomer = customerList.find {
            it.fullName.equals(customerName, ignoreCase = true) || it.email.equals(customerName, ignoreCase = true)
        }
        val customerUserId = selectedCustomerUserId ?: matchedCustomer?.userId

        val currentStaffId = sessionManager.getUserId()
        val currentStaffName = sessionManager.getUserName()

        viewModel.createJob(
            customerName = customerName,
            contactNumber = contactNumber,
            customerUserId = customerUserId,
            assignedStaffId = selectedStaffUserId ?: currentStaffId,
            assignedStaffName = selectedStaffName ?: currentStaffName,
            deviceType = deviceType,
            deviceBrand = deviceBrand,
            deviceModel = deviceModel,
            reportedIssue = reportedIssue,
            deviceCondition = deviceCondition,
            accessoriesReceived = accessoriesReceived.ifEmpty { null },
            beforePhotoPath = selectedPhotoUri?.toString(),
            expectedCompletionDate = selectedDeadlineCalendar.timeInMillis
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
