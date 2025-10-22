package com.example.drivenext

// хранит данные при запуске экрана.
import android.os.Bundle
// базовый класс для Activity.
import androidx.appcompat.app.AppCompatActivity
// используется для переходов между экранами.
import android.content.Intent
// инструменты, чтобы запустить действие с задержкой (в данном случае, через 2 секунды).
import android.os.Handler
import android.os.Looper

// экран загрузки (сплэш-скрин) пару сек
class SplashActivity : AppCompatActivity() {

    companion object {
        const val FORCE_ONBOARDING = true // специальная тестовая настройка. Если она true, приложение всегда показывает онбординг, даже если он уже пройден (удобно для проверки экрана обучения).
    }

    // Объявляем переменную для работы с данными пользователя (токен, онбординг и т.д.) ,lateinit будет инициализирована чуть позже (в onCreate)
    private lateinit var sessionManager: SessionManager

    // запуск экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // подключаем XML-разметку
        setContentView(R.layout.activity_splash)

        // создаём объект для работы с хранилищем пользователя.
        sessionManager = SessionManager(this)

        // запускает задачу на главном потоке (UI
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
        }, 2000)
    }

    // Проверка пользователя и переходы
    private fun checkUserAndNavigate() {
        if (!NetworkUtils.isInternetAvailable(this)) {
            showNoInternetScreen("SplashActivity")
            return
        }

        // Сохраняем логику принудительного онбординга для тестов
        if (FORCE_ONBOARDING) {
            println("DEBUG: Force onboarding enabled - showing onboarding screen")
            navigateToOnboarding()
            return
        }

        // НОВАЯ ЛОГИКА НАВИГАЦИИ (для продакшена)
        if (!sessionManager.isOnboardingCompleted()) {
            println("DEBUG: Onboarding not completed - showing onboarding screen")
            navigateToOnboarding()
            return
        }

        // Если есть токен - переходим на главный экран
        if (sessionManager.isTokenValid()) {
            println("DEBUG: Token valid - navigating to Main")
            navigateToMain()
            return
        }

        // Если онбординг пройден, но токена нет - показываем Welcome экран
        println("DEBUG: Onboarding completed but no valid token - navigating to Welcome")
        navigateToWelcome()
    }

    // Проверяем токен

    // переход на Welcome
    private fun navigateToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    // переход на Onboarding
    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    // переход на Login
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // переход на Main
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Экран “Нет интернета”
    private fun showNoInternetScreen(callingActivity: String) {
        val intent = Intent(this, NoInternetActivity::class.java)
        intent.putExtra("calling_activity", callingActivity)
        startActivity(intent)
        finish()
    }
}