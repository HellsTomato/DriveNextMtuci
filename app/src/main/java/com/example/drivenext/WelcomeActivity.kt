package com.example.drivenext // пакет, в котором находится класс (структура проекта)

// импортируем классы, необходимые для работы Activity и переходов
import android.content.Intent // нужен для перехода между экранами (Activity)
import android.os.Bundle // используется в методах жизненного цикла Activity
import androidx.appcompat.app.AppCompatActivity // базовый класс для всех экранов приложения
import com.example.drivenext.databinding.ActivityWelcomeBinding // ViewBinding — связывает XML-разметку и код
import com.example.drivenext.LoginActivity // импортируем экран входа
import com.example.drivenext.RegisterActivity // импортируем экран регистрации

// создаём класс экрана приветствия (Welcome screen), который появляется после онбординга
class WelcomeActivity : AppCompatActivity() {

    // объявляем переменную binding — она даёт доступ к элементам интерфейса XML
    // вместо findViewById используем ViewBinding для удобства и безопасности типов
    private lateinit var binding: ActivityWelcomeBinding

    // основной метод жизненного цикла, срабатывает при создании Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // вызываем базовый метод, чтобы не нарушить работу Android-системы

        // инициализируем binding, “надувая” (inflate) XML-разметку в код
        binding = ActivityWelcomeBinding.inflate(layoutInflater)

        // подключаем разметку к экрану (binding.root — корневой элемент XML)
        setContentView(binding.root)

        // вызываем функцию, которая назначает обработчики кликов для кнопок
        setupClickListeners()
    }

    // функция, в которой задаются действия при нажатии на кнопки
    private fun setupClickListeners() {

        // обработчик кнопки "Войти"
        // при нажатии вызывается функция navigateToLogin(), открывающая экран входа
        binding.btnLogin.setOnClickListener {
            navigateToLogin()
        }

        // обработчик кнопки "Зарегистрироваться"
        // при нажатии вызывается функция navigateToRegistration(), открывающая экран регистрации
        binding.btnRegister.setOnClickListener {
            navigateToRegistration()
        }
    }

    // функция перехода на экран входа (LoginActivity)
    private fun navigateToLogin() {
        // создаём Intent — это “сообщение системе” с указанием, какой экран нужно открыть
        val intent = Intent(this, LoginActivity::class.java)

        // запускаем новый экран (LoginActivity)
        startActivity(intent)
    }

    // функция перехода на экран регистрации (RegisterActivity)
    private fun navigateToRegistration() {
        // создаём Intent для перехода к экрану регистрации
        val intent = Intent(this, RegisterActivity::class.java)

        // запускаем экран регистрации
        startActivity(intent)
    }
}
