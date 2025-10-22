package com.example.drivenext

// data class — это специальный тип класса, который используется для хранения данных.
// OnboardingItem — это шаблон (модель), в котором хранится информация об одном слайде обучения:
//его название, описание и картинка.
data class OnboardingItem(
    val title: String, // заголовок слайда
    val description: String, // описание под заголовком
    val imageRes: Int // ссылка (ID) на картинку из res/drawable
)
