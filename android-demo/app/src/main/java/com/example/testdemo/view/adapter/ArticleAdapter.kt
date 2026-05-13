package com.example.testdemo.view.adapter

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.testdemo.R
import com.example.testdemo.model.Article

/**
 * 文章列表适配器
 */
class ArticleAdapter(
    private var articles: List<Article>,
    private val onArticleClick: (Article) -> Unit,
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCover: ImageView = itemView.findViewById(R.id.ivCover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val tvWordCount: TextView = itemView.findViewById(R.id.tvWordCount)
        private val tvCategoryTag: TextView = itemView.findViewById(R.id.tvCategoryTag)

        fun bind(article: Article) {
            tvTitle.text = article.title
            tvDifficulty.text = String.format("难度:%d", article.lexile)
            tvWordCount.text = String.format("%d词", article.wordNum)
            tvCategoryTag.text = article.type
            
            // 根据类型设置分类标签背景颜色
            val tagColor = getCategoryTagColor(article.type)
            val bg = tvCategoryTag.background
            when (bg) {
                is GradientDrawable -> bg.setColor(tagColor)
                is ShapeDrawable -> bg.paint.color = tagColor
                is android.graphics.drawable.ColorDrawable -> bg.color = tagColor
                else -> bg?.setTint(tagColor)
            }
            
            // 使用 Glide 加载带圆角的图片
            Glide.with(itemView.context)
                .load(article.cover)
                .apply(RequestOptions().transform(RoundedCorners(24)))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(ivCover)
            
            itemView.setOnClickListener {
                onArticleClick(article)
            }
        }

        /**
         * 根据文章类型生成对应的标签颜色
         */
        private fun getCategoryTagColor(type: String): Int {
            val colors = arrayOf(
                "#D4D4D4",
                "#FFB366",
                "#66CC66",
                "#DA7575",
                "#559DD0",
                "#CC66CC"
            )
            val index = type.hashCode() % colors.size
            return colors[index].toColorInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount() = articles.size

    fun updateArticles(newArticles: List<Article>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}
