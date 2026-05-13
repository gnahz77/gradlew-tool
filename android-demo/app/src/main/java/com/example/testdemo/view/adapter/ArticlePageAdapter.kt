package com.example.testdemo.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.testdemo.R
import com.example.testdemo.model.ArticleContent
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * 文章阅读分页适配器
 */
class ArticlePageAdapter(
    private val onToggleVolume: () -> Unit,
    private val onReplay: () -> Unit,
    private val onTogglePlayPause: () -> Unit,
    private val onVolumeChanged: (Int) -> Unit
) : RecyclerView.Adapter<ArticlePageAdapter.PageViewHolder>() {

    companion object {
        private const val PAYLOAD_HIGHLIGHT = "payload_highlight"
        private const val PAYLOAD_CONTROLS = "payload_controls"
    }

    private var pages: List<ArticleContent> = emptyList()
    private var isPlaying: Boolean = false
    private var isVolumePanelVisible: Boolean = false
    private var volume: Int = 80
    private var highlightIndex: Int = -1
    private var currentPageIndex: Int = 0

    /**
     * 提交新的页面列表
     */
    fun submitPages(newPages: List<ArticleContent>) {
        pages = newPages
        notifyDataSetChanged()
    }

    /**
     * 更新播放状态和音量面板状态
     */
    fun updateUiState(isPlaying: Boolean, isVolumePanelVisible: Boolean, volume: Int) {
        this.isPlaying = isPlaying
        this.isVolumePanelVisible = isVolumePanelVisible
        this.volume = volume
    }

    /**
     * 更新高亮词语索引
     */
    fun updateHighlight(index: Int) {
        highlightIndex = index
    }

    /**
     * 设置当前页，并刷新前后页面的高亮状态
     */
    fun setCurrentPage(currentPageIndex: Int, previousPageIndex: Int) {
        this.currentPageIndex = currentPageIndex
        if (previousPageIndex >= 0) {
            notifyItemChanged(previousPageIndex, PAYLOAD_HIGHLIGHT)
        }
        notifyItemChanged(currentPageIndex, PAYLOAD_HIGHLIGHT)
    }

    fun notifyHighlightChanged(currentPageIndex: Int) {
        notifyItemChanged(currentPageIndex, PAYLOAD_HIGHLIGHT)
    }

    fun notifyControlsChanged(currentPageIndex: Int) {
        notifyItemChanged(currentPageIndex, PAYLOAD_CONTROLS)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article_page, parent, false)
        return PageViewHolder(view, onToggleVolume, onReplay, onTogglePlayPause, onVolumeChanged)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(pages[position])
        holder.updateControls(isPlaying, isVolumePanelVisible, volume)
        holder.updateHighlight(if (position == currentPageIndex) highlightIndex else -1)
    }

    override fun onBindViewHolder(
        holder: PageViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        if (payloads.contains(PAYLOAD_HIGHLIGHT)) {
            val highlight = if (position == currentPageIndex) highlightIndex else -1
            holder.updateHighlight(highlight)
        }
        if (payloads.contains(PAYLOAD_CONTROLS)) {
            holder.updateControls(isPlaying, isVolumePanelVisible, volume)
        }
    }

    override fun getItemCount(): Int = pages.size

    class PageViewHolder(
        itemView: View,
        onToggleVolume: () -> Unit,
        onReplay: () -> Unit,
        onTogglePlayPause: () -> Unit,
        onVolumeChanged: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivIllustration: ImageView = itemView.findViewById(R.id.ivIllustration)
        private val volumePanel: View = itemView.findViewById(R.id.volumePanel)
        private val seekBarVolume: SeekBar = itemView.findViewById(R.id.seekBarVolume)
        private val btnVolume: ImageView = itemView.findViewById(R.id.btnVolume)
        private val btnReplay: ImageView = itemView.findViewById(R.id.btnReplay)
        private val btnPlayPause: ImageView = itemView.findViewById(R.id.btnPlayPause)
        private val rvWords: RecyclerView = itemView.findViewById(R.id.rvWords)

        private val wordAdapter = WordAdapter()

        init {
            btnVolume.setOnClickListener { onToggleVolume() }
            btnReplay.setOnClickListener { onReplay() }
            btnPlayPause.setOnClickListener { onTogglePlayPause() }

            seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        onVolumeChanged(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            val flexboxLayoutManager = FlexboxLayoutManager(itemView.context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            rvWords.layoutManager = flexboxLayoutManager
            rvWords.adapter = wordAdapter
        }

        fun bind(page: ArticleContent) {
            page.imgUrl?.let { url ->
                Glide.with(itemView)
                    .load(url)
                    .apply(RequestOptions().transform(RoundedCorners(32)))
                    .into(ivIllustration)
            }

            wordAdapter.setWords(page.sentenceByXFList ?: emptyList())
        }

        /**
         * 更新高亮词语
         */
        fun updateHighlight(index: Int) {
            wordAdapter.updateHighlight(index)
        }

        /**
         * 更新播放控制面板
         */
        fun updateControls(isPlaying: Boolean, isVolumePanelVisible: Boolean, volume: Int) {
            btnPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
            volumePanel.visibility = if (isVolumePanelVisible) View.VISIBLE else View.GONE
            seekBarVolume.progress = volume
        }
    }
}
