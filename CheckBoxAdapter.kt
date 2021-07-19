package developer.alexangan.ru.rewindapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import developer.alexangan.ru.rewindapp.BR
import developer.alexangan.ru.rewindapp.databinding.ItemCheckBoxBinding
import developer.alexangan.ru.rewindapp.model.Option

class CheckBoxAdapter(
    private val pairs: List<Pair<Option, Boolean>>,
    private val listener: OnItemCheckedChangeListener? = null,
    private val areClickable: Boolean = true
) : RecyclerView.Adapter<CheckBoxAdapter.CheckBoxViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckBoxViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        val binding = ItemCheckBoxBinding.inflate(inflater, parent, false)

        return CheckBoxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckBoxViewHolder, position: Int) {
        holder.bind(pairs[position].first)
    }

    inner class CheckBoxViewHolder(
        private val binding: ItemCheckBoxBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Option) {
            binding.checkBox.isClickable = areClickable
            binding.setVariable(BR.item, item)
            binding.checkBox.isChecked = pairs[adapterPosition].second

            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                listener?.onItemCheckedChange(item, isChecked)
            }

            binding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return pairs.size
    }
}

interface OnItemCheckedChangeListener {
    fun onItemCheckedChange(option: Option, isChecked: Boolean)
}