package com.example.drivenext

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var viewPager: ViewPager2
    private lateinit var skipButton: TextView
    private lateinit var nextButton: Button
    private lateinit var indicator: DotsIndicator

    private val onboardingItems = listOf(
        OnboardingItem(
            "Аренда автомобилей",
            "Открой для себя удобный и доступный способ передвижения",
            R.drawable.onboarding_1 // Добавьте ваши изображения
        ),
        OnboardingItem(
            "Безопасно и удобно",
            "Арендуй автомобиль и наслаждайся его удобством",
            R.drawable.onboarding_2
        ),
        OnboardingItem(
            "Лучшие предложения",
            "Выбирай понравившееся среди сотен доступных автомобилей",
            R.drawable.onboarding_3
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        sessionManager = SessionManager(this)
        initViews()
        setupViewPager()
        updateUI()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        skipButton = findViewById(R.id.skipButton)
        nextButton = findViewById(R.id.nextButton)
        indicator = findViewById(R.id.indicator)

        skipButton.setOnClickListener {
            completeOnboarding()
        }

        nextButton.setOnClickListener {
            if (viewPager.currentItem < onboardingItems.size - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                completeOnboarding()
            }
        }
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = adapter

        // Индикатор точек
        indicator.attachTo(viewPager)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUI()
            }
        })
    }

    private fun updateUI() {
        when (viewPager.currentItem) {
            onboardingItems.size - 1 -> {
                // Последняя страница
                nextButton.text = "Поехали"
                skipButton.visibility = TextView.GONE
            }

            else -> {
                nextButton.text = "Далее"
                skipButton.visibility = TextView.VISIBLE
            }
        }
    }


    private fun completeOnboarding() {
        println("DEBUG: Starting completeOnboarding")
        sessionManager.setOnboardingCompleted(true)
        println("DEBUG: Onboarding completed flag set to: ${sessionManager.isOnboardingCompleted()}")

        val intent = Intent(this, WelcomeActivity::class.java)
        println("DEBUG: Starting WelcomeActivity")
        startActivity(intent)

        println("DEBUG: Finishing OnboardingActivity")
        finish()
    }
}