package com.example.testdemo.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.example.testdemo.R
import com.example.testdemo.view.adapter.ArticlePageAdapter
import com.example.testdemo.viewmodel.article.ArticleIntent
import com.example.testdemo.viewmodel.article.ArticleState
import com.example.testdemo.viewmodel.article.ArticleViewModel
import kotlinx.coroutines.launch

/**
 * 文章阅读页面
 */
class ArticleActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_AID = "extra_aid"
        private const val DEFAULT_AID = 6

        /**
         * 创建跳转到 ArticleActivity 的意图
         */
        fun newIntent(activity: AppCompatActivity, aid: Int? = null): Intent {
            return Intent(activity, ArticleActivity::class.java).apply {
                if (aid != null) {
                    putExtra(EXTRA_AID, aid)
                }
            }
        }
    }

    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvPageIndicator: TextView
    private lateinit var progressPage: ProgressBar
    private lateinit var vpArticle: ViewPager2
    private lateinit var progressLoading: ProgressBar

    private lateinit var viewModel: ArticleViewModel
    private lateinit var pageAdapter: ArticlePageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_article)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[ArticleViewModel::class.java]

        initViews()
        setupListeners()
        observeState()

        val aid = intent.getIntExtra(EXTRA_AID, DEFAULT_AID)
        viewModel.sendIntent(ArticleIntent.LoadArticle(aid))
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvPageIndicator = findViewById(R.id.tvPageIndicator)
        progressPage = findViewById(R.id.progressPage)
        vpArticle = findViewById(R.id.vpArticle)
        progressLoading = findViewById(R.id.progressLoading)

        pageAdapter = ArticlePageAdapter(
            onToggleVolume = { viewModel.sendIntent(ArticleIntent.ToggleVolumePanel) },
            onReplay = { viewModel.sendIntent(ArticleIntent.Replay) },
            onTogglePlayPause = { viewModel.sendIntent(ArticleIntent.TogglePlayPause) },
            onVolumeChanged = { volume -> viewModel.sendIntent(ArticleIntent.SetVolume(volume)) }
        )
        vpArticle.adapter = pageAdapter
        vpArticle.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.sendIntent(ArticleIntent.GoToPage(position))
            }
        })
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private var lastRenderedPageIndex = -1
    private var lastRenderedHighlightIndex = -1
    private var lastRenderedIsPlaying: Boolean? = null
    private var lastRenderedVolumeVisible: Boolean? = null
    private var lastRenderedVolume: Int = -1
    private var lastErrorMessage: String? = null

    /**
     * 渲染状态变化
     */
    private fun renderState(state: ArticleState) {
        // 加载状态
        progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        vpArticle.visibility = if (state.isLoading || state.articleDetail == null) View.GONE else View.VISIBLE

        // 标题
        tvTitle.text = state.title

        // 错误信息
        if (!state.errorMessage.isNullOrBlank() && state.errorMessage != lastErrorMessage) {
            lastErrorMessage = state.errorMessage
            Toast.makeText(this, state.errorMessage, Toast.LENGTH_SHORT).show()
        } else if (state.errorMessage.isNullOrBlank()) {
            lastErrorMessage = null
        }

        if (state.articleDetail == null) return

        // 页码指示
        val currentPageDisplay = state.currentPageIndex + 1
        tvPageIndicator.text = getString(
            R.string.article_page_indicator,
            currentPageDisplay,
            state.totalPages
        )

        // 进度条
        if (state.totalPages > 0) {
            progressPage.progress = (currentPageDisplay * 100) / state.totalPages
        }

        // 页面内容变化时更新插图和词语
        if (lastRenderedPageIndex != state.currentPageIndex) {
            val previousPage = lastRenderedPageIndex
            lastRenderedPageIndex = state.currentPageIndex
            lastRenderedHighlightIndex = -1
            if (vpArticle.currentItem != state.currentPageIndex) {
                vpArticle.setCurrentItem(state.currentPageIndex, false)
            }
            pageAdapter.setCurrentPage(state.currentPageIndex, previousPage)
        }

        // 页面列表变化时更新
        val pages = state.articleDetail.contentList ?: emptyList()
        if (pageAdapter.itemCount != pages.size) {
            pageAdapter.submitPages(pages)
        }

        // 高亮词语变化时更新
        if (lastRenderedHighlightIndex != state.highlightIndex) {
            lastRenderedHighlightIndex = state.highlightIndex
            pageAdapter.updateHighlight(state.highlightIndex)
            pageAdapter.notifyHighlightChanged(state.currentPageIndex)
        }

        val controlsChanged =
            lastRenderedIsPlaying != state.isPlaying ||
                lastRenderedVolumeVisible != state.isVolumePanelVisible ||
                lastRenderedVolume != state.volume
        if (controlsChanged) {
            lastRenderedIsPlaying = state.isPlaying
            lastRenderedVolumeVisible = state.isVolumePanelVisible
            lastRenderedVolume = state.volume
            pageAdapter.updateUiState(state.isPlaying, state.isVolumePanelVisible, state.volume)
            pageAdapter.notifyControlsChanged(state.currentPageIndex)
        }
    }
}
