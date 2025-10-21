package com.example.drivenext

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NoInternetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        val retryButton = findViewById<Button>(R.id.retry_button)

        retryButton.setOnClickListener {
            checkInternetAndProceed()
        }

        // Проверяем интернет при создании активности
        checkInternetAndProceed()
    }

    private fun checkInternetAndProceed() {
        if (isInternetAvailable()) {
            // Интернет доступен - возвращаемся к предыдущей логике
            proceedToNextScreen()
        }
        // Если интернета нет - остаемся на этом экране
    }

    private fun isInternetAvailable(): Boolean {
        return NetworkUtils.isInternetAvailable(this)
    }

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