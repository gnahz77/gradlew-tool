package com.example.testdemo.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testdemo.R
import com.example.testdemo.model.ArticleWord

/**
 * 词语流式布局适配器
 */
class WordAdapter : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    private var words: List<ArticleWord> = emptyList()
    private var highlightIndex: Int = -1

    /**
     * 设置词语列表
     */
    fun setWords(newWords: List<ArticleWord>) {
        words = newWords
        highlightIndex = -1
        notifyDataSetChanged()
    }

    /**
     * 更新高亮词语索引
     */
    fun updateHighlight(index: Int) {
        if (index == highlightIndex) return
        val oldIndex = highlightIndex
        highlightIndex = index
        if (oldIndex >= 0 && oldIndex < words.size) {
            notifyItemChanged(oldIndex, Unit)
        }
        if (index >= 0 && index < words.size) {
            notifyItemChanged(index, Unit)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false) as TextView
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.bind(word, position == highlightIndex)
    }

    override fun onBindViewHolder(
        holder: WordViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.bind(words[position], position == highlightIndex)
        }
    }

    override fun getItemCount(): Int = words.size

    /**
     * 词语视图ViewHolder
     */
    class WordViewHolder(private val tvWord: TextView) : RecyclerView.ViewHolder(tvWord) {

        fun bind(word: ArticleWord, isHighlighted: Boolean) {
            // 给每个字之间添加空格以实现字间距效果
            tvWord.text = word.word.toCharArray().joinToString(" ")
            if (isHighlighted) {
                tvWord.setBackgroundResource(R.drawable.bg_word_highlight)
            } else {
                tvWord.background = null
            }
        }
    }
}
