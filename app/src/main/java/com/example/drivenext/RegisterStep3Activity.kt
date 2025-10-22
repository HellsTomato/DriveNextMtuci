package com.example.drivenext // пакет приложения

import android.app.DatePickerDialog // диалог выбора даты
import android.content.Intent // интенты для переходов между экранами
import android.content.pm.PackageManager // проверки разрешений и наличия активити
import android.net.Uri // универсальная ссылка на файл/контент
import android.os.Bundle // данные жизненного цикла Activity
import android.provider.MediaStore // стандартные действия камеры/галереи
import android.text.Editable // интерфейс изменяемого текста
import android.text.TextWatcher // слушатель изменений текста
import android.widget.Toast // всплывающие уведомления
import androidx.activity.result.contract.ActivityResultContracts // контракты получения результатов
import androidx.appcompat.app.AppCompatActivity // базовый класс Activity
import androidx.core.content.ContextCompat // безопасный доступ к ресурсам (цвет, строки)
import androidx.core.content.FileProvider // выдаёт безопасный content:// Uri к файлам
import com.example.drivenext.databinding.ActivityRegisterStep3Binding // ViewBinding класс
import java.io.File // работа с файлами
import java.text.SimpleDateFormat // форматирование дат
import java.util.Calendar // календарные поля (год/месяц/день)
import java.util.Date // текущая дата/время
import java.util.Locale // локаль форматирования

class RegisterStep3Activity : AppCompatActivity() { // экран шага 3 регистрации

    private lateinit var binding: ActivityRegisterStep3Binding // binding к элементам layout'а
    private lateinit var sessionManager: SessionManager // менеджер сессии (токен/флаги)
    private val calendar = Calendar.getInstance() // общий календарь для выбора даты

    private var isLicensePhotoUploaded = false // флаг: загружено фото прав
    private var isPassportPhotoUploaded = false // флаг: загружено фото паспорта
    private var currentPhotoUri: Uri? = null // Uri целевого файла снимка (для камеры)
    private var currentPhotoType: PhotoType = PhotoType.LICENSE // текущий тип загружаемого фото

    enum class PhotoType { LICENSE, PASSPORT } // список поддерживаемых типов фото

    // лаунчер выбора из галереи
    private val galleryLauncher = registerForActivityResult( // регистрируем контракт результата
        ActivityResultContracts.StartActivityForResult() // стартуем активити и ждём результат
    ) { result -> // колбэк результата
        if (result.resultCode == RESULT_OK) { // если пользователь выбрал файл
            result.data?.data?.let { uri -> // достаем Uri выбранной картинки
                handleImageSelection(uri) // обрабатываем выбранное фото
            } ?: run { // если Uri отсутствует
                Toast.makeText(this, "Не удалось выбрать изображение", Toast.LENGTH_SHORT).show() // показываем ошибку
            }
        }
    }

    // лаунчер камеры
    private val cameraLauncher = registerForActivityResult( // регистрируем контракт камеры
        ActivityResultContracts.StartActivityForResult() // запуск камеры и получение результата
    ) { result -> // колбэк результата
        if (result.resultCode == RESULT_OK) { // если фото сделано
            currentPhotoUri?.let { uri -> // если у нас был заранее подготовленный Uri
                handleImageSelection(uri) // используем его
            } ?: run { // иначе пробуем миниатюру (thumbnail)
                result.data?.extras?.get("data")?.let { // достаем Bitmap из extras
                    handleImageSelection(createTempFileFromBitmap(it as android.graphics.Bitmap)) // сохраняем Bitmap во временный файл
                } ?: run { // если нет ни Uri, ни Bitmap
                    Toast.makeText(this, "Фото не было сохранено", Toast.LENGTH_SHORT).show() // сообщаем об ошибке
                }
            }
        }
    }

    // лаунчер запроса разрешения на камеру
    private val cameraPermissionLauncher = registerForActivityResult( // регистрируем запрос разрешения
        ActivityResultContracts.RequestPermission() // контракт одного разрешения
    ) { isGranted -> // результат запроса
        if (isGranted) { // если разрешение дано
            openCamera() // открываем камеру
        } else { // иначе
            Toast.makeText(this, "Разрешение на камеру отклонено", Toast.LENGTH_LONG).show() // предупреждаем
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) { // точка входа Activity
        super.onCreate(savedInstanceState) // вызываем реализацию базового класса
        binding = ActivityRegisterStep3Binding.inflate(layoutInflater) // создаём binding из XML
        setContentView(binding.root) // показываем layout на экране

        sessionManager = SessionManager(this) // инициализируем менеджер сессии
        setupViews() // навешиваем клики на элементы
        setupTextWatchers() // подписываемся на изменения текста
        validateForm() // первичная проверка формы (кнопка выключена)
    }

    private fun setupViews() { // настройка обработчиков UI
        binding.backButton.setOnClickListener { // кнопка «Назад»
            onBackPressedDispatcher.onBackPressed() // системный возврат назад
        }

        binding.nextButton.setOnClickListener { // кнопка «Далее»
            performFinalRegistration() // финальная валидация и завершение
        }

        binding.issueDateEditText.setOnClickListener { // поле даты выдачи
            showDatePicker() // показываем диалог календаря
        }

        binding.uploadLicenseText.setOnClickListener { // загрузка фото прав
            currentPhotoType = PhotoType.LICENSE // отмечаем текущий тип — права
            showImageSourceDialog() // даём выбрать камеру или галерею
        }

        binding.uploadPassportText.setOnClickListener { // загрузка фото паспорта
            currentPhotoType = PhotoType.PASSPORT // отмечаем текущий тип — паспорт
            showImageSourceDialog() // даём выбрать источник
        }
    }

    private fun showImageSourceDialog() { // диалог выбора источника изображения
        val options = arrayOf("Сделать фото", "Выбрать из галереи", "Отмена") // пункты меню
        androidx.appcompat.app.AlertDialog.Builder(this) // создаем билдер диалога
            .setTitle("Выберите источник фото") // заголовок
            .setItems(options) { dialog, which -> // обработка выбора
                when (which) { // индекс пункта
                    0 -> checkCameraPermission() // «Сделать фото» → проверяем разрешение
                    1 -> openGallery() // «Выбрать из галереи» → открываем SAF
                    2 -> dialog.dismiss() // «Отмена» → закрыть диалог
                }
            }
            .show() // показываем диалог
    }

    private fun checkCameraPermission() { // проверка разрешения на камеру
        when {
            ContextCompat.checkSelfPermission( // узнаём статус
                this, // текущий контекст
                android.Manifest.permission.CAMERA // целевое разрешение
            ) == PackageManager.PERMISSION_GRANTED -> { // если разрешено
                openCamera() // сразу открываем камеру
            }
            else -> { // если нет разрешения
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) // запрашиваем у пользователя
            }
        }
    }

    private fun openCamera() { // запуск камеры
        try { // защищаемся от ошибок
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // стандартный интент камеры

            val photoFile = createImageFile() // создаём временный файл для снимка
            val photoUri = FileProvider.getUriForFile( // получаем безопасный Uri к файлу
                this, // контекст
                "${packageName}.fileprovider", // authorities из AndroidManifest
                photoFile // сам файл
            )

            currentPhotoUri = photoUri // запоминаем Uri для обработки результата
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri) // просим камеру писать в этот файл
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // даём право записи

            val cameraApps = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY) // ищем обработчики
            if (cameraApps.isNotEmpty()) { // если есть подходящая камера
                cameraLauncher.launch(intent) // запускаем камеру
            } else { // если EXTRA_OUTPUT не поддерживается
                val simpleIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // простой режим (миниатюра)
                val simpleCameraApps = packageManager.queryIntentActivities(simpleIntent, PackageManager.MATCH_DEFAULT_ONLY) // ищем обработчики
                if (simpleCameraApps.isNotEmpty()) { // если есть простая камера
                    currentPhotoUri = null // сбрасываем Uri (будет thumbnail)
                    cameraLauncher.launch(simpleIntent) // запускаем простой режим
                } else { // если камер нет
                    Toast.makeText(this, "Камера не доступна. Используйте галерею.", Toast.LENGTH_LONG).show() // предупреждаем
                    openGallery() // открываем галерею как альтернативу
                }
            }
        } catch (e: Exception) { // отлавливаем исключения
            e.printStackTrace() // печатаем стек
            Toast.makeText(this, "Ошибка при открытии камеры", Toast.LENGTH_LONG).show() // уведомляем
            openGallery() // альтернативный путь — галерея
        }
    }

    private fun openGallery() { // открытие системного выбора файла (SAF)
        try { // ловим возможные ошибки
            val intent = Intent(Intent.ACTION_GET_CONTENT) // универсальный интент выбора контента
            intent.type = "image/*" // фильтр: только изображения
            intent.addCategory(Intent.CATEGORY_OPENABLE) // только открываемые файлы
            galleryLauncher.launch(intent) // запускаем контракт галереи
        } catch (e: Exception) { // обработка исключений
            e.printStackTrace() // печать стека
            Toast.makeText(this, "Ошибка при открытии галереи", Toast.LENGTH_SHORT).show() // уведомление
        }
    }

    private fun createImageFile(): File { // создание временного файла для фото
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) // метка времени в имени
        val storageDir = getExternalFilesDir(null) // приватная папка приложения (внешнее хранилище)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir) // создание файла с уникальным именем
    }

    private fun createTempFileFromBitmap(bitmap: android.graphics.Bitmap): Uri { // сохранение миниатюры в файл
        val file = createImageFile() // создаём файл
        val outputStream = java.io.FileOutputStream(file) // открываем поток записи
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream) // пишем JPEG с качеством 90%
        outputStream.close() // закрываем поток
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file) // возвращаем безопасный Uri
    }

    private fun handleImageSelection(uri: Uri) { // обработка выбранного/снятого фото
        when (currentPhotoType) { // смотрим текущий тип фото
            PhotoType.LICENSE -> { // если права
                isLicensePhotoUploaded = true // отмечаем флаг загрузки
                binding.uploadLicenseText.text = "Фото загружено ✓" // меняем текст кнопки
                binding.uploadLicenseText.isEnabled = false // запрещаем повторный выбор
                binding.uploadLicenseText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray)) // делаем текст серым
                Toast.makeText(this, "Фото водительского удостоверения загружено", Toast.LENGTH_SHORT).show() // уведомляем
            }
            PhotoType.PASSPORT -> { // если паспорт
                isPassportPhotoUploaded = true // отмечаем флаг загрузки
                binding.uploadPassportText.text = "Фото загружено ✓" // меняем текст кнопки
                binding.uploadPassportText.isEnabled = false // запрещаем повторный выбор
                binding.uploadPassportText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray)) // делаем текст серым
                Toast.makeText(this, "Фото паспорта загружено", Toast.LENGTH_SHORT).show() // уведомляем
            }
        }
        validateForm() // пересчитываем валидность формы (включить/выключить кнопку)
    }

    private fun showDatePicker() { // показ диалога выбора даты
        val year = calendar.get(Calendar.YEAR) // текущий год
        val month = calendar.get(Calendar.MONTH) // текущий месяц
        val day = calendar.get(Calendar.DAY_OF_MONTH) // текущий день

        val datePickerDialog = DatePickerDialog( // создаём диалог с календарём
            this, // контекст
            { _, selectedYear, selectedMonth, selectedDay -> // слушатель выбора даты
                calendar.set(selectedYear, selectedMonth, selectedDay) // устанавливаем выбранную дату
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // формат даты
                val formattedDate = dateFormat.format(calendar.time) // конвертация даты в строку
                binding.issueDateEditText.setText(formattedDate) // подставляем в поле
                validateForm() // валидируем форму заново
            },
            year, month, day // стартовые значения календаря
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // запрещаем будущие даты
        datePickerDialog.show() // показываем диалог
    }

    private fun setupTextWatchers() { // подписка на изменения текста
        val textWatcher = object : TextWatcher { // общий TextWatcher для полей
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {} // не используем
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {} // не используем
            override fun afterTextChanged(s: Editable?) { // вызывается после изменения текста
                validateForm() // проверяем форму
            }
        }

        binding.driverLicenseEditText.addTextChangedListener(textWatcher) // слушаем поле номера прав
        binding.issueDateEditText.addTextChangedListener(textWatcher) // слушаем поле даты выдачи
    }

    private fun validateForm() { // проверка валидности формы
        val driverLicense = binding.driverLicenseEditText.text.toString().trim() // текст из поля номер прав
        val issueDate = binding.issueDateEditText.text.toString().trim() // текст из поля дата выдачи

        val isDriverLicenseValid = driverLicense.isNotEmpty() && driverLicense.length == 10 // номер не пуст и 10 символов
        val isIssueDateValid = issueDate.isNotEmpty() && isValidDate(issueDate) // дата не пустая и корректная

        val isFormValid = isDriverLicenseValid && isIssueDateValid && // поля валидны
                isLicensePhotoUploaded && isPassportPhotoUploaded // оба фото загружены

        binding.nextButton.isEnabled = isFormValid // включаем/выключаем кнопку «Далее»

        val color = if (isFormValid) R.color.purple_700 else R.color.gray_700 // цвет кнопки по состоянию
        binding.nextButton.setBackgroundColor(ContextCompat.getColor(this, color)) // применяем цвет
    }

    private fun isValidDate(dateStr: String): Boolean { // валидация строки даты
        return try { // пытаемся распарсить
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // ожидаемый формат
            sdf.isLenient = false // строгая проверка (запрещены несуществующие даты)
            val date = sdf.parse(dateStr) // парсинг строки в Date
            date != null && date <= Date() // дата существует и не в будущем
        } catch (e: Exception) { // если парсинг не удался
            false // считаем дату некорректной
        }
    }

    private fun performFinalRegistration() { // финальный шаг регистрации
        if (!NetworkUtils.isInternetAvailable(this)) { // нет интернета?
            showNoInternetScreen("RegisterStep3Activity") // показываем экран «Нет интернета»
            return // прекращаем выполнение
        }

        val driverLicense = binding.driverLicenseEditText.text.toString().trim() // номер прав
        val issueDate = binding.issueDateEditText.text.toString().trim() // дата выдачи

        if (driverLicense.isEmpty() || issueDate.isEmpty()) { // пустые поля?
            Toast.makeText(this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show() // предупреждаем
            return // выходим
        }

        if (driverLicense.length != 10) { // неверная длина номера?
            Toast.makeText(this, "Номер водительского удостоверения должен содержать 10 символов.", Toast.LENGTH_SHORT).show() // предупреждаем
            return // выходим
        }

        if (!isValidDate(issueDate)) { // некорректная дата?
            Toast.makeText(this, "Введите корректную дату выдачи.", Toast.LENGTH_SHORT).show() // предупреждаем
            return // выходим
        }

        if (!isLicensePhotoUploaded || !isPassportPhotoUploaded) { // недостаёт фото?
            Toast.makeText(this, "Пожалуйста, загрузите все необходимые фото.", Toast.LENGTH_SHORT).show() // предупреждаем
            return // выходим
        }

        saveStep3Data(driverLicense, issueDate) // сохраняем данные шага 3
        completeRegistration() // завершаем регистрацию
    }

    private fun saveStep3Data(driverLicense: String, issueDate: String) { // сохранение данных шага 3
        val prefs = getSharedPreferences("RegistrationTemp", MODE_PRIVATE) // открываем временные prefs
        prefs.edit().apply { // начинаем редактирование
            putString("driver_license", driverLicense) // записываем номер прав
            putString("license_issue_date", issueDate) // записываем дату выдачи
            apply() // применяем асинхронно
        }
    }

    private fun completeRegistration() { // финал регистрации (эмуляция успеха)
        val fakeToken = "complete_registration_token_${System.currentTimeMillis()}" // генерируем фейковый токен
        sessionManager.saveAuthToken(fakeToken) // сохраняем токен в SessionManager

        val prefs = getSharedPreferences("RegistrationTemp", MODE_PRIVATE) // открываем prefs
        prefs.edit().putBoolean("registration_complete", true).apply() // ставим флаг завершения регистрации

        navigateToSuccessScreen() // переходим на экран успеха
    }

    private fun navigateToSuccessScreen() { // переход на экран «Регистрация завершена»
        try { // защищаемся от возможных ошибок
            val intent = Intent(this, RegistrationSuccessActivity::class.java) // создаём интент
            startActivity(intent) // запускаем Activity
            finish() // закрываем текущую, чтобы не вернуться назад
        } catch (e: Exception) { // если что-то пошло не так
            e.printStackTrace() // печатаем стек ошибки
        }
    }

    private fun showNoInternetScreen(callingActivity: String) { // показ экрана «Нет интернета»
        val intent = Intent(this, NoInternetActivity::class.java) // создаём интент на NoInternetActivity
        intent.putExtra("calling_activity", callingActivity) // передаём, кто вызвал
        startActivity(intent) // запускаем активити
        finish() // закрываем текущую
    }
}
