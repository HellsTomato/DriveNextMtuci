package com.example.drivenext

// класс Intent используется для перехода между экранами(Activity)
import android.content.Intent
// контейнер, который хранит данные, передаваемые при запуске Activity.
import android.os.Bundle
// нужен, чтобы работать с элементом интерфейса Button.
import android.widget.Button
// базовый класс для экранов (Activity), с поддержкой современных функций и стилей Android.
import androidx.appcompat.app.AppCompatActivity

class NoInternetActivity : AppCompatActivity() {

    // метод жизненного цикла Activity, вызывается один раз при создании экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        // вызываем родительскую реализацию, без этого Activity не запустится.
        super.onCreate(savedInstanceState)
        // подключаем XML-разметку экрана (activity_no_internet.xml).
        setContentView(R.layout.activity_no_internet)

        // находим ссылку на кнопку и задаём действие
        val retryButton = findViewById<Button>(R.id.retry_button)

        // назначаем обработчик нажатия
        retryButton.setOnClickListener {
            checkInternetAndProceed()
        }

        // Проверяем интернет при создании активности
        checkInternetAndProceed()
    }
    // Проверка соединения
    private fun checkInternetAndProceed() {
        if (isInternetAvailable()) {
            // Интернет доступен - возвращаемся к предыдущей логике
            proceedToNextScreen()
        }
        // Если интернета нет - остаемся на этом экране
    }
    // проверка через нашу утилиту
    private fun isInternetAvailable(): Boolean {
        return NetworkUtils.isInternetAvailable(this)
    }

    // Переход на нужный экран
    private fun proceedToNextScreen() {
        // Здесь логика определения, куда переходить
        // Можно передавать информацию через Intent extras

        val callingActivity = intent.getStringExtra("calling_activity")

        when (callingActivity) {
            "SplashActivity" -> {
                // Возвращаемся в SplashActivity для повторной проверки токена
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            "LoginActivity" -> {
                // Возвращаемся в LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            else -> {
                // По умолчанию переходим на главный экран
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }

        finish()
    }
}