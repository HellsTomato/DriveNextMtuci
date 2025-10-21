package com.example.drivenext

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {

    companion object {
        const val FORCE_ONBOARDING = true
    }

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
        }, 2000)
    }

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

    private fun navigateToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showNoInternetScreen(callingActivity: String) {
        val intent = Intent(this, NoInternetActivity::class.java)
        intent.putExtra("calling_activity", callingActivity)
        startActivity(intent)
        finish()
    }
}