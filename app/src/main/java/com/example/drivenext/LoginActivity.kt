package com.example.drivenext

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drivenext.databinding.ActivityLoginBinding
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    // Регулярное выражение для базовой валидации email
    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )

    // Временный список "зарегистрированных" пользователей для демонстрации
    private val registeredUsers = mapOf(
        "user@example.com" to "password123",
        "test@example.com" to "123456",
        "admin@example.com" to "admin123"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupViews()
        setupTextWatchers()
    }

    private fun setupViews() {
        // Изначально кнопка неактивна
        binding.loginButton.isEnabled = false

        // Кнопка Войти
        binding.loginButton.setOnClickListener {
            performLoginWithValidation()
        }

        // Кнопка Войти через Google
        binding.googleLoginButton.setOnClickListener {
            performGoogleLogin()
        }

        // Текст Зарегистрироваться
        binding.registerButton.setOnClickListener {
            navigateToRegistration()
        }

        // Забыли пароль
        binding.forgotPasswordText.setOnClickListener {
            showForgotPassword()
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

        // Добавляем слушатели к обоим полям
        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
    }

    private fun validateForm() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Кнопка активна только когда оба поля заполнены
        val isFormValid = email.isNotEmpty() && password.isNotEmpty()
        binding.loginButton.isEnabled = isFormValid
    }

    private fun performLoginWithValidation() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем валидность email формата
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Введите корректный адрес электронной почты", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем длину пароля
        if (password.length < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_LONG).show()
            return
        }

        performLogin(email, password)
    }

    private fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    private fun performLogin(email: String, password: String) {
        // Имитация проверки учетных данных
        when {
            !registeredUsers.containsKey(email) -> {
                // Email не найден
                Toast.makeText(this, "Почта не найдена. Проверьте email или зарегистрируйтесь", Toast.LENGTH_LONG).show()
            }
            registeredUsers[email] != password -> {
                // Неверный пароль
                Toast.makeText(this, "Неверный пароль. Попробуйте еще раз", Toast.LENGTH_LONG).show()
            }
            else -> {
                // Успешный вход (без сообщения)
                sessionManager.saveAuthToken("user_token_${System.currentTimeMillis()}")
                navigateToMain()
            }
        }
    }

    private fun performGoogleLogin() {
        // TODO: Реализовать Google OAuth
        // Временная имитация
        sessionManager.saveAuthToken("google_token_${System.currentTimeMillis()}")
        navigateToMain()
    }

    private fun showForgotPassword() {
        Toast.makeText(this, "Функция восстановления пароля в разработке", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRegistration() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}