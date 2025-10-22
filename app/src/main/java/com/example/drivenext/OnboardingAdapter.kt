package com.example.drivenext

// нужен, чтобы “надуть” (создать из XML) шаблон одного слайда.
import android.view.LayoutInflater
// базовые элементы интерфейса (родитель и дочерние элементы).
import android.view.View
import android.view.ViewGroup
// виджеты текста и картинки.
import android.widget.TextView
import android.widget.ImageView
// список, который показывает элементы (в нашем случае — слайды).
import androidx.recyclerview.widget.RecyclerView
// подключение разметки (если используется ViewBinding).
import com.example.drivenext.databinding.ItemOnboardingBinding

// адаптер, который отвечает за отображение списка слайдов онбординга.
class OnboardingAdapter(private val items: List<OnboardingItem>) :  // передаём в адаптер список данных (List объектов OnboardingItem).
    RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() { // передаём в адаптер список данных (List объектов OnboardingItem).
        // Он требует реализовать 3 метода:
    //onCreateViewHolder — создание карточки.
    //onBindViewHolder — заполнение карточки данными.
    //getItemCount — сколько карточек в списке.

        // ViewHolder знает, где лежит заголовок, описание и картинка для одного слайда.
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        private val onboardingImage: ImageView = itemView.findViewById(R.id.onboardingImage)

            // Положи данные из модели в соответствующие элементы интерфейса.
        fun bind(item: OnboardingItem) { // связывает данные (OnboardingItem) с визуальными элементами.
            titleText.text = item.title // записываем в TextView.
            descriptionText.text = item.description // записываем в другой TextView.
            onboardingImage.setImageResource(item.imageRes) // ставим картинку (ImageView).
        }
    }

    // Создаём визуальный шаблон для одного слайда - вызывается, когда нужно создать новую карточку (ещё пустую).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context) // LayoutInflater создаёт (надувает) макет item_onboarding.xml. parent.context — контекст родительского списка (RecyclerView).
            .inflate(R.layout.item_onboarding, parent, false) // false — говорит, что пока не нужно добавлять вид в родителя вручную.
        return ViewHolder(view) // возвращаем новый ViewHolder, связанный с этим макетом.
    }

    // вызывается, когда карточка должна показать данные.
    // items[position] — берём элемент по позиции (например, 0, 1, 2...).
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position]) // holder.bind(...) — передаём объект OnboardingItem в ViewHolder,

    }

    // сообщает, сколько элементов в списке (List<OnboardingItem>).без этого RecyclerView не знает, сколько карточек рисовать.
    override fun getItemCount(): Int = items.size
}