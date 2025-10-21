package com.example.drivenext

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.drivenext.databinding.ActivityRegisterBinding
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager

    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            performRegistration()
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
        }

        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.confirmPasswordEditText.addTextChangedListener(textWatcher)

        binding.termsCheckbox.setOnCheckedChangeListener { _, _ ->
            validateForm()
        }
    }

    private fun validateForm() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        val isTermsAccepted = binding.termsCheckbox.isChecked

        val isEmailValid = validateEmail(email)
        val isPasswordValid = password.length >= 6
        val isConfirmPasswordValid = password == confirmPassword && confirmPassword.isNotEmpty()

        val isFormValid = isEmailValid && isPasswordValid && isConfirmPasswordValid && isTermsAccepted

        binding.nextButton.isEnabled = isFormValid

        // Визуальная обратная связь - меняем фон кнопки
        if (isFormValid) {
            binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_700))
        } else {
            binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_700))
        }
    }

    private fun performRegistration() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        val isTermsAccepted = binding.termsCheckbox.isChecked

        // Проверка интернета
        if (!NetworkUtils.isInternetAvailable(this)) {
            showNoInternetScreen("RegisterActivity")
            return
        }

        if (!validateEmail(email)) {
            Toast.makeText(this, "Введите корректный адрес электронной почты.", Toast.LENGTH_LONG).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_LONG).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Пароли не совпадают.", Toast.LENGTH_LONG).show()
            return
        }

        if (!isTermsAccepted) {
            Toast.makeText(this, "Необходимо согласиться с условиями обслуживания и политикой конфиденциальности.", Toast.LENGTH_LONG).show()
            return
        }

        saveStep1Data(email, password)
        navigateToStep2()
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && EMAIL_PATTERN.matcher(email).matches()
    }

    private fun saveStep1Data(email: String, password: String) {
        val prefs = getSharedPreferences("RegistrationTemp", MODE_PRIVATE)
        prefs.edit().apply {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    private fun navigateToStep2() {
        try {
            val intent = Intent(this, RegisterStep2Activity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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