package com.example.testdemo.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.testdemo.R
import com.example.testdemo.model.ArticleType

/**
 * 分类列表适配器
 */
class CategoryAdapter(
    private var categories: List<ArticleType>,
    private val onCategoryClick: (ArticleType, Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(category: ArticleType, position: Int) {
            textView.text = category.type
            textView.isSelected = position == selectedPosition
            
            // 根据所选内容更新文本颜色
            textView.setTextColor(
                if (position == selectedPosition) {
                    ContextCompat.getColor(textView.context, R.color.color_2B66FF)
                } else {
                    ContextCompat.getColor(textView.context, R.color.color_666666)
                }
            )
            
            textView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onCategoryClick(category, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false) as TextView
        return CategoryViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCategories: List<ArticleType>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}
