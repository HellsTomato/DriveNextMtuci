package com.example.drivenext // объявляем пакет, где лежит класс

// нужные импорты
import android.content.Intent // для переходов между экранами (Activity)
import android.os.Bundle // данные, передаваемые при создании Activity
import androidx.appcompat.app.AppCompatActivity // базовый класс для экранов
import com.example.drivenext.databinding.ActivityRegistrationSuccessBinding // ViewBinding для доступа к элементам XML

// класс экрана "Регистрация успешно завершена"
class RegistrationSuccessActivity : AppCompatActivity() {

    // создаём переменную для ViewBinding — связывает XML и Kotlin-код
    private lateinit var binding: ActivityRegistrationSuccessBinding

    // создаём переменную SessionManager — управляет токенами и настройками
    private lateinit var sessionManager: SessionManager

    // метод жизненного цикла, вызывается при создании Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // вызываем стандартную реализацию

        // создаём binding, чтобы получить доступ к элементам макета XML
        binding = ActivityRegistrationSuccessBinding.inflate(layoutInflater)

        // устанавливаем в качестве макета экрана корневой layout
        setContentView(binding.root)

        // отладочное сообщение — чтобы увидеть в консоли, что экран запущен
        println("DEBUG: RegistrationSuccessActivity started")

        // инициализируем менеджер сессии
        sessionManager = SessionManager(this)

        // вызываем метод, который настраивает кнопки и действия на экране
        setupViews()
    }

    // функция для настройки элементов интерфейса
    private fun setupViews() {
        // вешаем обработчик клика на кнопку “Далее”
        binding.nextButton.setOnClickListener {
            // выводим сообщение в лог (для отладки)
            println("DEBUG: Next button clicked in success screen")

            // вызываем функцию перехода на главный экран
            navigateToMain()
        }
    }

    // функция для перехода на главный экран приложения
    private fun navigateToMain() {
        // сообщение в лог, что переход начинается
        println("DEBUG: Starting MainActivity from success screen")
        try {
            // создаём интент для запуска MainActivity
            val intent = Intent(this, MainActivity::class.java)

            // флаги Intent:
            // FLAG_ACTIVITY_NEW_TASK — создаёт новую задачу (task)
            // FLAG_ACTIVITY_CLEAR_TASK — очищает предыдущие Activity из стека
            // вместе они гарантируют, что пользователь не сможет вернуться назад на регистрацию
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // запускаем главный экран
            startActivity(intent)

            // закрываем текущую Activity (чтобы нельзя было вернуться назад)
            finish()

            // сообщение в лог — переход успешно выполнен
            println("DEBUG: Successfully started MainActivity")
        } catch (e: Exception) {
            // если произошла ошибка — выводим сообщение об ошибке в консоль
            println("ERROR: Failed to start MainActivity: ${e.message}")

            // печатаем полный стек ошибки (для диагностики)
            e.printStackTrace()

            // уведомление пользователю не показываем (чтобы не мешать UX)
        }
    }
}
