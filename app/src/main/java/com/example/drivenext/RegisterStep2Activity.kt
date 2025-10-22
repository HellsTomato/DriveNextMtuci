package com.example.drivenext

// готовое системное окно выбора даты (календарь).
import android.app.DatePickerDialog
// переход на другой экран.
import android.content.Intent
// контейнер для данных, которые система передаёт в onCreate.
import android.os.Bundle
// интерфейс «слушателя» изменений текста в EditText.
import android.text.Editable
import android.text.TextWatcher
// // базовый класс экрана.
import androidx.appcompat.app.AppCompatActivity
// безопасное получение ресурсов (цветов) на разных API.
import androidx.core.content.ContextCompat
// ViewBinding для activity_register_step2.xml (даёт доступ к вью по id без findViewById).
import com.example.drivenext.databinding.ActivityRegisterStep2Binding
// форматирование даты в строку (например, dd/MM/yyyy).
import java.text.SimpleDateFormat
// объект календаря (удобно читать/менять год-месяц-день).
import java.util.Calendar
// локаль (язык/регион, влияет на формат).
import java.util.Locale

// Объявление Activity и поля
class RegisterStep2Activity : AppCompatActivity() {

    // bind связывать XML-разметку (вёрстку) с кодом Kotlin/Java
    // бинд к разметке: binding.firstNameEditText, binding.calendarButton, и т.д.
    private lateinit var binding: ActivityRegisterStep2Binding
    // доступ к SharedPreferences (общий менеджер сессии).
    private lateinit var sessionManager: SessionManager
    // текущая дата/время; будем использовать для календаря и форматирования.
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // inflate → создаём binding из XML
        binding = ActivityRegisterStep2Binding.inflate(layoutInflater)
        setContentView(binding.root) // показываем на экране.

        println("DEBUG: RegisterStep2Activity started")

        sessionManager = SessionManager(this) // готовим доступ к SharedPreferences.
        setupViews() // навешиваем клики на «Назад» и «Далее».
        setupTextWatchers() // подписываемся на изменения полей и чекбокса.

        // сразу выставляем корректное состояние кнопки (даже если поля пустые). проверка полей
        validateForm()
    }

    // Клики на кнопки и поле даты
    private fun setupViews() {
        binding.backButton.setOnClickListener { // имитируем системную кнопку «Назад».
            onBackPressed()
        }

        // Обработчик для консоли
        binding.nextButton.setOnClickListener { // попытка перейти к шагу 3 (перед этим проводим «боевую» проверку в proceedToStep3()).
            println("DEBUG: Next button clicked in step 2")
            proceedToStep3()
        }

        // Обработчик для календаря (иконка)
        binding.calendarButton.setOnClickListener {
            showDatePicker()
        }

        // оба открывают календарь (удобство UX: можно нажать и на иконку, и на само поле).

        // Обработчик для всего поля даты (при клике в любом месте поля)
        binding.birthDateEditText.setOnClickListener {
            showDatePicker()
        }
    }

    // Диалог выбора даты (календарь)
    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

// создаём DatePickerDialog(контекст, слушатель, стартовыйГод, стартовыйМесяц, стартовыйДень).
//в лямбде-слушателе, когда пользователь выбрал дату
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay) // кладём выбранные значения в объект календаря,
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // форматируем дату (например, 07/11/2005),
                val formattedDate = dateFormat.format(calendar.time) // показываем отформатированную дату в поле,
                binding.birthDateEditText.setText(formattedDate) // пересчитываем валидность формы (поле даты перестало быть пустым).
                validateForm()
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // запрещаем выбрать дату из будущего (логично для дня рождения).
        datePickerDialog.show()
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
        binding.lastNameEditText.addTextChangedListener(textWatcher)
        binding.firstNameEditText.addTextChangedListener(textWatcher)
        binding.birthDateEditText.addTextChangedListener(textWatcher)

        binding.genderRadioGroup.setOnCheckedChangeListener { _, _ -> // если отметили/сменили пол, тоже валидируем.
            validateForm()
        }
    }

    // Централизованная валидация формы + визуальный фидбек - все условия собраны в одном месте; UI реагирует сразу, без нажатий.
    private fun validateForm() {
        val lastName = binding.lastNameEditText.text.toString().trim() // // trim() убирает пробелы по краям (чтобы “ test@mail.com ” считался как test@mail.com).
        val firstName = binding.firstNameEditText.text.toString().trim()
        val birthDate = binding.birthDateEditText.text.toString().trim()
        val isGenderSelected = binding.genderRadioGroup.checkedRadioButtonId != -1

        // собираем isFormValid — все 4 условия должны быть true.
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

    // «Боевой» переход к шагу 3
    private fun proceedToStep3() {
        println("DEBUG: proceedToStep3() called")

        // Проверка интернета
        if (!NetworkUtils.isInternetAvailable(this)) {
            println("DEBUG: No internet connection")
            showNoInternetScreen("RegisterStep2Activity")
            return
        }

        // собираем все поля, включая отчество (может быть пустым).
        val lastName = binding.lastNameEditText.text.toString().trim()
        val firstName = binding.firstNameEditText.text.toString().trim()
        val middleName = binding.middleNameEditText.text.toString().trim()
        val birthDate = binding.birthDateEditText.text.toString().trim()
        val gender = when (binding.genderRadioGroup.checkedRadioButtonId) {
            // определяем gender по id отмеченной радиокнопки. Если ничего не выбрано — пустая строка.
            R.id.maleRadioButton -> "Мужской"
            R.id.femaleRadioButton -> "Женский"
            else -> ""
        }

        // Финальная проверка - фамилия/имя/дата/пол должны быть заполнены. Если что-то не так — просто return (можно улучшить: показать ошибку под полем).
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

    // Сохранение данных шага 2
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

    // Переход к шагу 3 (+ анимация и защита try/catch)
    private fun navigateToStep3() {
        println("DEBUG: Starting RegisterStep3Activity")
        try {
            val intent = Intent(this, RegisterStep3Activity::class.java) // создаём Intent и запускаем RegisterStep3Activity.
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // плавное появление/исчезновение.
            println("DEBUG: Successfully started RegisterStep3Activity")
        } catch (e: Exception) { // защита от падения: если, например, активити не объявлена в AndroidManifest.xml, не вылетим, а напечатаем ошибку.
            println("ERROR: Failed to start RegisterStep3Activity: ${e.message}")
            e.printStackTrace()
        }
    }

    // Экран «Нет интернета»
    private fun showNoInternetScreen(callingActivity: String) {
        val intent = Intent(this, NoInternetActivity::class.java) // ередаём строку “кто меня вызвал”
        intent.putExtra("calling_activity", callingActivity)
        startActivity(intent)
        finish() // закрываем текущую Activity, чтобы по «Назад» не вернуться сюда без сети.
    }
}