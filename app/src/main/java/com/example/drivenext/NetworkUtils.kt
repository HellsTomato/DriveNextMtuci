package com.example.drivenext

// нужен, чтобы получить доступ к системным сервисам, например к сетевому менеджеру.
import android.content.Context
// ConnectivityManager — системный класс Android, который следит за состоянием сети
import android.net.ConnectivityManager
// используется на новых версиях Android (6.0 и выше), он позволяет точно определить тип соединения (Wi-Fi, мобильные данные и т.д.).
import android.net.NetworkCapabilities
// нужен, чтобы узнать версию Android, потому что на старых и новых устройствах методы отличаются.
import android.os.Build

// один экземпляр на всё приложение
object NetworkUtils {

    // пульт управления сетью — объект, у которого можно спросить, какая сеть активна и какая у неё природа.
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // На новых Android используем новый API, на старых — совместимый устаревший API. Это нужно для поддержки разных устройств.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // ссылка на активную сеть
            val network = connectivityManager.activeNetwork ?: return false
            // получает «паспорт» сети: по каким каналам идёт трафик.
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            // проверяем типы транспорта ,если сеть — это Wi-Fi, мобильная или Ethernet, считаем, что интернет есть (возвращаем true)
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            // скрыть предупреждение компилятора об устаревшем API
            @Suppress("DEPRECATION")
            // старый способ получить инфо о сети (или null
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected // флаг «есть соединение»
        }
    }
}