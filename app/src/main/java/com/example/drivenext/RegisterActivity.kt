package com.example.drivenext

// «команда» открыть другой экран (Activity) + возможность добавить данные (extras).
import android.content.Intent
// контейнер для данных, которые система передаёт в onCreate.
import android.os.Bundle
// интерфейс «слушателя» изменений текста в EditText.
import android.text.Editable
import android.text.TextWatcher
// маленькие всплывающие сообщения.
import android.widget.Toast
// базовый класс экрана.
import androidx.appcompat.app.AppCompatActivity
// утилита поддержки: безопасно достаёт ресурсы (цвета, строки) на разных API.
import androidx.core.content.ContextCompat
// класс, сгенерированный ViewBinding из activity_register.xml. Даёт binding.button, binding.emailEditText и т.д.
import com.example.drivenext.databinding.ActivityRegisterBinding
// регулярки (валидация email).
import java.util.regex.Pattern

// Объявление Activity, поля и регулярка, объявляем экран «Регистрация».
class RegisterActivity : AppCompatActivity() {

    // переменная создаётся чуть позже (в onCreate), но не null.
    private lateinit var binding: ActivityRegisterBinding
    // наш класс-обёртка над SharedPreferences.
    private lateinit var sessionManager: SessionManager
    // регулярное выражение, проверяет формат email:

    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )

    // onCreate: создаём UI, настраиваем экран
    // метод жизненного цикла Activity, вызывается один раз при создании экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        // вызываем родительскую реализацию, без этого Activity не запустится.
        super.onCreate(savedInstanceState)
        // создаёт объект биндинга (из XML наполняет binding реальными view - (кнопки, поля, картинки)).
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root) // показываем на экране корневой view из биндинга.

        sessionManager = SessionManager(this) // готовим доступ к SharedPreferences.
        setupViews() // навешиваем клики на «Назад» и «Далее».
        setupTextWatchers() // подписываемся на изменения полей и чекбокса.

        // сразу выставляем корректное состояние кнопки (даже если поля пустые). проверка полей
        validateForm()
    }

    // Навесили обработчики на кнопки
    private fun setupViews() {
        binding.backButton.setOnClickListener { // имитируем системную кнопку «Назад».
            onBackPressed()
        }

        binding.nextButton.setOnClickListener { // при нажатии «Далее» запускаем полную проверку (валидация + переход).
            performRegistration()
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

        // Когда пользователь ставит или убирает галочку,
        //мы пересчитываем (validateForm()) всё состояние формы,
        //чтобы кнопка “Далее” всегда точно отражала — можно ли продолжить регистрацию.

        // // Добавляем слушатели к обоим полям
        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.confirmPasswordEditText.addTextChangedListener(textWatcher)

        // при любом клике по чекбоксу снова валидируем форму.
        binding.termsCheckbox.setOnCheckedChangeListener { _, _ ->
            validateForm()
        }
    }

    // Централизованная валидация формы + визуальный фидбек - все условия собраны в одном месте; UI реагирует сразу, без нажатий.
    private fun validateForm() {
        val email = binding.emailEditText.text.toString().trim() // trim() убирает пробелы по краям (чтобы “ test@mail.com ” считался как test@mail.com).
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        val isTermsAccepted = binding.termsCheckbox.isChecked

        val isEmailValid = validateEmail(email) // (регулярка).
        val isPasswordValid = password.length >= 6 // не короче 6 символов
        val isConfirmPasswordValid = password == confirmPassword && confirmPassword.isNotEmpty() // пароли совпадают и подтверждение не пустое (важно: иначе два пустых «совпадут»).

        // isTermsAccepted → чекбокс отмечен., isFormValid → все условия должны быть true.,
        val isFormValid = isEmailValid && isPasswordValid && isConfirmPasswordValid && isTermsAccepted

        binding.nextButton.isEnabled = isFormValid

        // Визуальная обратная связь - меняем фон кнопки
        if (isFormValid) {
            binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_700))
        } else {
            binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_700))
        }
    }

    // Повторно читаем поля (не доверяем только «мягкой» валидации).
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

        // Если всё ок → saveStep1Data(...) (временное сохранение) → navigateToStep2() (переход к следующему экрану).
        saveStep1Data(email, password)
        navigateToStep2()
    }

    // Проверка email регуляркой
    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && EMAIL_PATTERN.matcher(email).matches()
    }

    // Временное сохранение данных шага 1
    private fun saveStep1Data(email: String, password: String) {
        val prefs = getSharedPreferences("RegistrationTemp", MODE_PRIVATE)
        prefs.edit().apply {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    // Переход на шаг 2 + анимация
    private fun navigateToStep2() {
        try {
            val intent = Intent(this, RegisterStep2Activity::class.java) // команда открыть экран шага 2.
            startActivity(intent) // выполняем переход.
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // применяем плавную анимацию появления/исчезновения.
        } catch (e: Exception) {
            e.printStackTrace() // перестраховка (например, если забыли объявить Activity в манифесте).
        }
    }

    // Нет интернета → отправка на NoInternetActivity
    private fun showNoInternetScreen(callingActivity: String) {
        val intent = Intent(this, NoInternetActivity::class.java)
        intent.putExtra("calling_activity", callingActivity)
        startActivity(intent)
        finish() // закрываем RegisterActivity, чтобы по «Назад» не вернуться сюда без связи.
    }
}