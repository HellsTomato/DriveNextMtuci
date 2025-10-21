package com.example.drivenext

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.drivenext.databinding.ActivityRegisterStep2Binding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterStep2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterStep2Binding
    private lateinit var sessionManager: SessionManager
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        println("DEBUG: RegisterStep2Activity started")

        sessionManager = SessionManager(this)
        setupViews()
        setupTextWatchers()

        // Первоначальная проверка формы
        validateForm()
    }

    private fun setupViews() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.nextButton.setOnClickListener {
            println("DEBUG: Next button clicked in step 2")
            proceedToStep3()
        }

        // Обработчик для календаря (иконка)
        binding.calendarButton.setOnClickListener {
            showDatePicker()
        }

        // Обработчик для всего поля даты (при клике в любом месте поля)
        binding.birthDateEditText.setOnClickListener {
            showDatePicker()
        }
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
                binding.birthDateEditText.setText(formattedDate)
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

        binding.lastNameEditText.addTextChangedListener(textWatcher)
        binding.firstNameEditText.addTextChangedListener(textWatcher)
        binding.birthDateEditText.addTextChangedListener(textWatcher)

        binding.genderRadioGroup.setOnCheckedChangeListener { _, _ ->
            validateForm()
        }
    }

    private fun validateForm() {
        val lastName = binding.lastNameEditText.text.toString().trim()
        val firstName = binding.firstNameEditText.text.toString().trim()
        val birthDate = binding.birthDateEditText.text.toString().trim()
        val isGenderSelected = binding.genderRadioGroup.checkedRadioButtonId != -1

        val isLastNameValid = lastName.isNotEmpty()
        val isFirstNameValid = firstName.isNotEmpty()
        val isBirthDateValid = birthDate.isNotEmpty()

        val isFormValid = isLastNameValid && isFirstNameValid && isBirthDateValid && isGenderSelected

        println("DEBUG: Step2 validation - LastName: $isLastNameValid, FirstName: $isFirstNameValid, BirthDate: $isBirthDateValid, Gender: $isGenderSelected -> FormValid: $isFormValid")

        binding.nextButton.isEnabled = isFormValid

        // Визуальная обратная связь - меняем фон кнопки
        if (isFormValid) {
            binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_700))
        } else {
            binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_700))
        }
    }

    private fun proceedToStep3() {
        println("DEBUG: proceedToStep3() called")

        // Проверка интернета
        if (!NetworkUtils.isInternetAvailable(this)) {
            println("DEBUG: No internet connection")
            showNoInternetScreen("RegisterStep2Activity")
            return
        }

        val lastName = binding.lastNameEditText.text.toString().trim()
        val firstName = binding.firstNameEditText.text.toString().trim()
        val middleName = binding.middleNameEditText.text.toString().trim()
        val birthDate = binding.birthDateEditText.text.toString().trim()
        val gender = when (binding.genderRadioGroup.checkedRadioButtonId) {
            R.id.maleRadioButton -> "Мужской"
            R.id.femaleRadioButton -> "Женский"
            else -> ""
        }

        // Финальная проверка
        if (lastName.isEmpty()) {
            return
        }

        if (firstName.isEmpty()) {
            return
        }

        if (birthDate.isEmpty()) {
            return
        }

        if (gender.isEmpty()) {
            return
        }

        println("DEBUG: All validations passed, saving data and navigating to step 3")

        // Сохраняем данные второго шага
        saveStep2Data(lastName, firstName, middleName, birthDate, gender)

        // Переходим на третий шаг
        navigateToStep3()
    }

    private fun saveStep2Data(lastName: String, firstName: String, middleName: String, birthDate: String, gender: String) {
        println("DEBUG: Saving step 2 data")
        val prefs = getSharedPreferences("RegistrationTemp", MODE_PRIVATE)
        prefs.edit().apply {
            putString("last_name", lastName)
            putString("first_name", firstName)
            putString("middle_name", middleName)
            putString("birth_date", birthDate)
            putString("gender", gender)
            apply()
        }
        println("DEBUG: Step 2 data saved to SharedPreferences")
    }

    private fun navigateToStep3() {
        println("DEBUG: Starting RegisterStep3Activity")
        try {
            val intent = Intent(this, RegisterStep3Activity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            println("DEBUG: Successfully started RegisterStep3Activity")
        } catch (e: Exception) {
            println("ERROR: Failed to start RegisterStep3Activity: ${e.message}")
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