package com.example.drivenext

// переход на другие экраны.
import android.content.Intent
// данные, передаваемые в onCreate.
import android.os.Bundle
// чтобы реагировать на ввод текста в полях (email/пароль).
import android.text.Editable
import android.text.TextWatcher
// короткие всплывающие сообщения.
import android.widget.Toast
// базовый класс Activity.
import androidx.appcompat.app.AppCompatActivity
// класс ViewBinding, сгенерированный из activity_login.xml (даёт доступ к view без findViewById).
import com.example.drivenext.databinding.ActivityLoginBinding
// регулярные выражения (валидация email).
import java.util.regex.Pattern

// Объявление Activity и поля
class LoginActivity : AppCompatActivity() {

    // ViewBinding, позволяет обращаться к элементам activity_login.xml как к полям binding.
    private lateinit var binding: ActivityLoginBinding
    // доступ к SharedPreferences (токен, флаги).
    private lateinit var sessionManager: SessionManager

    // Регулярное выражение для базовой валидации email
    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )

    // Временный список "зарегистрированных" пользователей для демонстрации
    private val registeredUsers = mapOf( // имитация БД: словарь “email → пароль”
        "user@example.com" to "password123",
        "test@example.com" to "123456",
        "admin@example.com" to "admin123"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater) // создаёт объект биндинга из XML.
        setContentView(binding.root)

        sessionManager = SessionManager(this) // готовим хранилище токена.
        setupViews() // навешиваем обработчики (кнопки).
        setupTextWatchers() // подписываемся на ввод в полях.
    }

    // Инициализация кнопок и действий
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

    // Слушатели текста (валидация на лету)
    // как только пользователь вводит символ — вызывается validateForm() (вкл/выкл кнопку входа).
    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher { // интерфейс: три метода. Мы используем afterTextChanged, чтобы проверять форму после каждого изменения.
            // пользователь ставит курсор и начинает печатать
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            // он вводит буквы
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            // он закончил ввод (система обновила поле)
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
        }

        // Добавляем слушатели к обоим полям
        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
    }

    // Проверка заполненности формы - простая UX-валидация: не даём жать “Войти” с пустыми полями.
    private fun validateForm() {
        val email = binding.emailEditText.text.toString().trim() // убираем пробелы в начале/конце.
        val password = binding.passwordEditText.text.toString().trim()

        // Кнопка активна только когда оба поля заполнены
        val isFormValid = email.isNotEmpty() && password.isNotEmpty() // оба поля должны быть непустыми.
        binding.loginButton.isEnabled = isFormValid // включаем/выключаем кнопку.
    }

    // Полная проверка перед логином - защитились от очевидных ошибок ввода перед “логином”
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

        performLogin(email, password) // всё ок
    }

    // Проверка формата email (регулярка) - вынесенная функция, чтобы код читался проще.
    private fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches() // проверяет, соответствует ли email шаблону
    }

    // Логин (имитация) + сохранение токена
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

    // Логин через Google (заглушка) - кнопка “Google” просто делает вид, что авторизовала, и сохраняет токен.
    private fun performGoogleLogin() {
        // TODO: Реализовать Google OAuth
        // Временная имитация
        sessionManager.saveAuthToken("google_token_${System.currentTimeMillis()}")
        navigateToMain()
    }

    // “Забыли пароль” (заглушка)
    private fun showForgotPassword() {
        Toast.makeText(this, "Функция восстановления пароля в разработке", Toast.LENGTH_SHORT).show()
    }

    // Переход на регистрацию
    private fun navigateToRegistration() {
        val intent = Intent(this, RegisterActivity::class.java) // открыть экран регистрации.
        startActivity(intent) // выполняем переход.
    }

    // Переход в Main и закрытие Login
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // закрываем LoginActivity, чтобы по Back не вернуться на логин.
    }
}