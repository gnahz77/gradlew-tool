package com.example.testdemo.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.testdemo.R

/**
 * 难度值列表适配器
 */
class DifficultyAdapter(
    private var difficulties: List<Int>,
    private val onDifficultyClick: (Int, Int) -> Unit
) : RecyclerView.Adapter<DifficultyAdapter.DifficultyViewHolder>() {

    private var selectedPosition = 0

    inner class DifficultyViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(difficulty: Int, position: Int) {
            textView.text = difficulty.toString()
            textView.isSelected = position == selectedPosition
            
            // 根据所选内容更新文本颜色
            textView.setTextColor(
                if (position == selectedPosition) {
                    ContextCompat.getColor(textView.context, R.color.color_2B66FF)
                } else {
                    ContextCompat.getColor(textView.context, R.color.color_999999)
                }
            )
            
            textView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onDifficultyClick(difficulty, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DifficultyViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_difficulty, parent, false) as TextView
        return DifficultyViewHolder(textView)
    }

    override fun onBindViewHolder(holder: DifficultyViewHolder, position: Int) {
        holder.bind(difficulties[position], position)
    }

    override fun getItemCount() = difficulties.size

    fun updateDifficulties(newDifficulties: List<Int>) {
        difficulties = newDifficulties
        notifyDataSetChanged()
    }
}
