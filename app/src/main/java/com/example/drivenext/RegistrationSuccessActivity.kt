package com.example.drivenext

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.drivenext.databinding.ActivityRegistrationSuccessBinding

class RegistrationSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationSuccessBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("DEBUG: RegistrationSuccessActivity started")

        sessionManager = SessionManager(this)
        setupViews()
    }

    private fun setupViews() {
        binding.nextButton.setOnClickListener {
            println("DEBUG: Next button clicked in success screen")
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        println("DEBUG: Starting MainActivity from success screen")
        try {
            val intent = Intent(this, MainActivity::class.java)

            // Очищаем стек активностей и запускаем главный экран
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            println("DEBUG: Successfully started MainActivity")
        } catch (e: Exception) {
            println("ERROR: Failed to start MainActivity: ${e.message}")
            e.printStackTrace()
            // Убрал Toast уведомление об ошибке
        }
    }
}