package com.example.drivenext

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.drivenext.databinding.ActivityRegisterStep3Binding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegisterStep3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterStep3Binding
    private lateinit var sessionManager: SessionManager
    private val calendar = Calendar.getInstance()

    private var isLicensePhotoUploaded = false
    private var isPassportPhotoUploaded = false
    private var currentPhotoUri: Uri? = null
    private var currentPhotoType: PhotoType = PhotoType.LICENSE

    enum class PhotoType { LICENSE, PASSPORT }

    // Лаунчер для галереи
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            } ?: run {
                Toast.makeText(this, "Не удалось выбрать изображение", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Лаунчер для камеры
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            currentPhotoUri?.let { uri ->
                handleImageSelection(uri)
            } ?: run {
                // Если URI не сохранен, пробуем получить данные из Intent
                result.data?.extras?.get("data")?.let {
                    // Обрабатываем thumbnail
                    handleImageSelection(createTempFileFromBitmap(it as android.graphics.Bitmap))
                } ?: run {
                    Toast.makeText(this, "Фото не было сохранено", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Лаунчер для разрешения камеры
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Разрешение на камеру отклонено", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterStep3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupViews()
        setupTextWatchers()
        validateForm()
    }

    private fun setupViews() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.nextButton.setOnClickListener {
            performFinalRegistration()
        }

        binding.issueDateEditText.setOnClickListener {
            showDatePicker()
        }

        binding.uploadLicenseText.setOnClickListener {
            currentPhotoType = PhotoType.LICENSE
            showImageSourceDialog()
        }

        binding.uploadPassportText.setOnClickListener {
            currentPhotoType = PhotoType.PASSPORT
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Сделать фото", "Выбрать из галереи", "Отмена")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Выберите источник фото")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Создаем временный файл для фото
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )

            // Сохраняем URI для использования после съемки
            currentPhotoUri = photoUri

            // Пробуем два подхода:
            // 1. С EXTRA_OUTPUT для сохранения полноразмерного фото
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            // Проверяем, есть ли приложения для обработки этого Intent
            val cameraApps = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (cameraApps.isNotEmpty()) {
                cameraLauncher.launch(intent)
            } else {
                // Если нет приложений с EXTRA_OUTPUT, пробуем без него
                val simpleIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val simpleCameraApps = packageManager.queryIntentActivities(simpleIntent, PackageManager.MATCH_DEFAULT_ONLY)
                if (simpleCameraApps.isNotEmpty()) {
                    currentPhotoUri = null // Сбрасываем URI, будем использовать thumbnail
                    cameraLauncher.launch(simpleIntent)
                } else {
                    Toast.makeText(this, "Камера не доступна. Используйте галерею.", Toast.LENGTH_LONG).show()
                    openGallery()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при открытии камеры", Toast.LENGTH_LONG).show()
            openGallery() // При ошибке открываем галерею
        }
    }

    private fun openGallery() {
        try {
            // Используем самый универсальный Intent
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            galleryLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при открытии галереи", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun createTempFileFromBitmap(bitmap: android.graphics.Bitmap): Uri {
        val file = createImageFile()
        val outputStream = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.close()
        return FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
    }

    private fun handleImageSelection(uri: Uri) {
        when (currentPhotoType) {
            PhotoType.LICENSE -> {
                isLicensePhotoUploaded = true
                binding.uploadLicenseText.text = "Фото загружено ✓"
                binding.uploadLicenseText.isEnabled = false
                binding.uploadLicenseText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                Toast.makeText(this, "Фото водительского удостоверения загружено", Toast.LENGTH_SHORT).show()
            }
            PhotoType.PASSPORT -> {
                isPassportPhotoUploaded = true
                binding.uploadPassportText.text = "Фото загружено ✓"
                binding.uploadPassportText.isEnabled = false
                binding.uploadPassportText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                Toast.makeText(this, "Фото паспорта загружено", Toast.LENGTH_SHORT).show()
            }
        }
        validateForm()
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                binding.issueDateEditText.setText(formattedDate)
                validateForm()
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
        }

        binding.driverLicenseEditText.addTextChangedListener(textWatcher)
        binding.issueDateEditText.addTextChangedListener(textWatcher)
    }

    private fun validateForm() {
        val driverLicense = binding.driverLicenseEditText.text.toString().trim()
        val issueDate = binding.issueDateEditText.text.toString().trim()

        val isDriverLicenseValid = driverLicense.isNotEmpty() && driverLicense.length == 10
        val isIssueDateValid = issueDate.isNotEmpty() && isValidDate(issueDate)

        val isFormValid = isDriverLicenseValid && isIssueDateValid && isLicensePhotoUploaded && isPassportPhotoUploaded

        binding.nextButton.isEnabled = isFormValid

        if (isFormValid) {
            binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_700))
        } else {
            binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_700))
        }
    }

    private fun isValidDate(dateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(dateStr)
            date != null && date <= Date()
        } catch (e: Exception) {
            false
        }
    }

    private fun performFinalRegistration() {
        if (!NetworkUtils.isInternetAvailable(this)) {
            showNoInternetScreen("RegisterStep3Activity")
            return
        }

        val driverLicense = binding.driverLicenseEditText.text.toString().trim()
        val issueDate = binding.issueDateEditText.text.toString().trim()

        if (driverLicense.isEmpty() || issueDate.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все обязательные поля.", Toast.LENGTH_SHORT).show()
            return
        }

        if (driverLicense.length != 10) {
            Toast.makeText(this, "Номер водительского удостоверения должен содержать 10 символов.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidDate(issueDate)) {
            Toast.makeText(this, "Введите корректную дату выдачи.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isLicensePhotoUploaded || !isPassportPhotoUploaded) {
            Toast.makeText(this, "Пожалуйста, загрузите все необходимые фото.", Toast.LENGTH_SHORT).show()
            return
        }

        saveStep3Data(driverLicense, issueDate)
        completeRegistration()
    }

    private fun saveStep3Data(driverLicense: String, issueDate: String) {
        val prefs = getSharedPreferences("RegistrationTemp", MODE_PRIVATE)
        prefs.edit().apply {
            putString("driver_license", driverLicense)
            putString("license_issue_date", issueDate)
            apply()
        }
    }

    private fun completeRegistration() {
        val fakeToken = "complete_registration_token_${System.currentTimeMillis()}"
        sessionManager.saveAuthToken(fakeToken)

        val prefs = getSharedPreferences("RegistrationTemp", MODE_PRIVATE)
        prefs.edit().putBoolean("registration_complete", true).apply()

        navigateToSuccessScreen()
    }

    private fun navigateToSuccessScreen() {
        try {
            val intent = Intent(this, RegistrationSuccessActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNoInternetScreen(callingActivity: String) {
        val intent = Intent(this, NoInternetActivity::class.java)
        intent.putExtra("calling_activity", callingActivity)
        startActivity(intent)
        finish()
    }
}