package com.example.drivenext // пакет, в котором находится класс (структура проекта)

// импортируем необходимые классы
import android.os.Bundle // используется для передачи данных при создании Activity
import androidx.appcompat.app.AppCompatActivity // базовый класс для экранов (Activity)

// создаём основной экран приложения — MainActivity
// это “главное окно”, куда попадает пользователь после авторизации или регистрации
class MainActivity : AppCompatActivity() {

    // метод жизненного цикла Activity — вызывается при создании экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // вызываем реализацию родительского метода (обязательно)

        // устанавливаем макет, который будет отображаться на экране
        // R.layout.activity_main — это XML-файл в папке res/layout
        // именно он определяет, какие элементы интерфейса пользователь увидит (текст, кнопки, и т.д.)
        setContentView(R.layout.activity_main)
    }
}
