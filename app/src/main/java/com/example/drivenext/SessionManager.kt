package com.example.drivenext

// Context — нужен, чтобы получить доступ к системным функциям (файлы, настройки, сервисы).
import android.content.Context
// SharedPreferences — встроенный класс Android для хранения простых настроек.
import android.content.SharedPreferences
// androidx.core.content.edit — упрощённая версия prefs.edit { ... }, чтобы не писать val editor = ... вручную.
import androidx.core.content.edit


class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences("DRIVE_NEXT_APP", Context.MODE_PRIVATE)

    companion object {
        const val ACCESS_TOKEN = "access_token"
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val ONBOARDING_COMPLETED = "onboarding_completed"
    }

    // Сохраняем токен
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(ACCESS_TOKEN, token)
        editor.apply()
    }

    // Получаем токен
    fun fetchAuthToken(): String? {
        return prefs.getString(ACCESS_TOKEN, null)
    }

    // Альтернативный метод для совместимости (возвращает пустую строку вместо null)
    fun getToken(): String {
        return prefs.getString(ACCESS_TOKEN, "") ?: ""
    }

    // Проверяем, первый ли запуск
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(IS_FIRST_LAUNCH, true)
    }

    // Отмечаем, что приложение уже запускалось
    fun setAppLaunched() {
        val editor = prefs.edit()
        editor.putBoolean(IS_FIRST_LAUNCH, false)
        editor.apply()
    }

    // Выход (очистка токена)
    fun logout() {
        val editor = prefs.edit()
        editor.remove(ACCESS_TOKEN)
        editor.apply()
    }

    // Проверяем валидность токена (упрощенная версия)
    fun isTokenValid(): Boolean {
        val token = fetchAuthToken()
        return !token.isNullOrEmpty()
    }

    // Устанавливаем завершение онбординга
    fun setOnboardingCompleted(isCompleted: Boolean) {
        prefs.edit {
            putBoolean(ONBOARDING_COMPLETED, isCompleted)
        }
    }

    // Проверяем завершен ли онбординг
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(ONBOARDING_COMPLETED, false)
    }
}