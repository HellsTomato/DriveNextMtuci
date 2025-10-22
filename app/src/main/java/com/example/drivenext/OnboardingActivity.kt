package com.example.drivenext

// “команда + конверт с данными” для перехода на другой экран.
import android.content.Intent
// контейнер данных, который система передаёт в onCreate.
import android.os.Bundle
import android.widget.Button
// виджеты интерфейса: кнопка и текст.
import android.widget.TextView
// базовый класс экрана (Activity).
import androidx.appcompat.app.AppCompatActivity
// компонент для пролистывания страниц (свайпы).
import androidx.viewpager2.widget.ViewPager2
// библиотечный индикатор точек (видна текущая страница).
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

// объявляем экран и наследуемся от базового класса Activity.
class OnboardingActivity : AppCompatActivity() {

    // lateinit var — “инициализирую позже, но это точно не null” (мы присвоим значения в onCreate).
    private lateinit var sessionManager: SessionManager // доступ к SharedPreferences (флаги/токен).
    private lateinit var viewPager: ViewPager2 // пейджер со слайдами.
    private lateinit var skipButton: TextView // Пропустить
    private lateinit var nextButton: Button // Далее
    private lateinit var indicator: DotsIndicator // точки под слайдами.

    // Данные для слайдов
    private val onboardingItems = listOf( // создаём список моделей
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
        initViews() // найдём вьюхи и повесим обработчики.
        setupViewPager() // подключим адаптер и индикатор.
        updateUI() // приведём кнопки к корректному виду под текущую страницу.
    }

    // Инициализация вью и клики - связали кнопки с логикой: пропуск, перелистывание, завершение.
    private fun initViews() {
        // достаём элемент из разметки по ID.
        viewPager = findViewById(R.id.viewPager)
        skipButton = findViewById(R.id.skipButton)
        nextButton = findViewById(R.id.nextButton)
        indicator = findViewById(R.id.indicator)

        skipButton.setOnClickListener {
            completeOnboarding() // skip: сразу завершаем онбордин
        }

        nextButton.setOnClickListener {
            // если не последняя страница: currentItem + 1 → пролистываем
            if (viewPager.currentItem < onboardingItems.size - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                completeOnboarding() // завершаем онбордин
            }
        }
    }

    // Настройка ViewPager2 и индикатора
    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingItems) // адаптер, который рисует карточки из списка данных.
        viewPager.adapter = adapter

        // привязали точки к пейджеру (они сами обновляются при скролле).
        indicator.attachTo(viewPager)

        // регистрируем “слушателя”, который следит за тем, когда пользователь перелистнул страницу.
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() { // создаём объект, который реагирует на события пролистывания.
            override fun onPageSelected(position: Int) { // вызывается каждый раз, когда пользователь остановился на новой странице.
                updateUI()
                // вызываем твою функцию, которая обновляет кнопки:
                //если страница последняя → кнопка “Поехали”, “Пропустить” скрыт;
                //иначе → “Далее”, “Пропустить” виден.
            }
        })
    }

    // Переключение UI под текущую страницу
    private fun updateUI() {
        when (viewPager.currentItem) { // индекс текущей страницы
            onboardingItems.size - 1 -> {
                // Последняя страница
                nextButton.text = "Поехали"
                skipButton.visibility = TextView.GONE // прячем/показываем “Пропустить”
            }

            else -> {
                nextButton.text = "Далее"
                skipButton.visibility = TextView.VISIBLE
            }
        }
    }


    // Завершение онбординга и переход дальше
    private fun completeOnboarding() {
        println("DEBUG: Starting completeOnboarding") // отладочные сообщения видно только в консоли
        sessionManager.setOnboardingCompleted(true) // пишем в SharedPreferences флаг: онбординг пройден.
        println("DEBUG: Onboarding completed flag set to: ${sessionManager.isOnboardingCompleted()}")

        val intent = Intent(this, WelcomeActivity::class.java) // формируем переход на экран Welcome.
        println("DEBUG: Starting WelcomeActivity")
        startActivity(intent) // запускаем Welcome.

        println("DEBUG: Finishing OnboardingActivity")
        finish() // закрываем текущую Activity
    }
}