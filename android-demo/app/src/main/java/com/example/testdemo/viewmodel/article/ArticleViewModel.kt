package com.example.testdemo.viewmodel.article

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testdemo.data.network.RetrofitClient
import com.example.testdemo.data.repository.ArticleRepository
import com.example.testdemo.model.ArticleWord
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

/**
 * 文章阅读页面 ViewModel
 */
class ArticleViewModel(
    private val reducer: ArticleReducer = ArticleReducer(),
    private val repository: ArticleRepository = ArticleRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleState())
    val state: StateFlow<ArticleState> = _state

    private val intentChannel = Channel<ArticleIntent>(Channel.BUFFERED)

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var highlightRunnable: Runnable? = null

    init {
        processIntents()
    }

    fun sendIntent(intent: ArticleIntent) {
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }

    private fun processIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                val current = _state.value
                _state.value = reducer.reduce(current, intent)
                handleSideEffects(intent)
            }
        }
    }

    private fun handleSideEffects(intent: ArticleIntent) {
        when (intent) {
            is ArticleIntent.LoadArticle -> loadArticle(intent.aid)
            is ArticleIntent.GoToPage -> startPageAudio()
            is ArticleIntent.NextPage -> startPageAudio()
            is ArticleIntent.PrevPage -> startPageAudio()
            is ArticleIntent.TogglePlayPause -> {
                if (_state.value.isPlaying) {
                    resumeAudio()
                } else {
                    pauseAudio()
                }
            }
            is ArticleIntent.Replay -> {
                replayAudio()
            }
            is ArticleIntent.SetVolume -> {
                updateVolume(_state.value.volume)
            }
            is ArticleIntent.AudioCompleted -> {
                stopHighlightTracking()
            }
            else -> { /* no side effect */ }
        }
    }

    /**
     * 加载文章详情
     */
    private fun loadArticle(aid: Int) {
        viewModelScope.launch {
            val result = repository.fetchArticleDetail(aid)
            result.onSuccess { detail ->
                val totalPages = detail.contentList?.size ?: 0
                _state.value = _state.value.copy(
                    articleDetail = detail,
                    isLoading = false,
                    title = detail.title ?: "",
                    totalPages = totalPages,
                    currentPageIndex = 0,
                    isPlaying = true,
                    highlightIndex = -1,
                    errorMessage = null
                )
                // 自动开始播放第一页
                startPageAudio()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "加载失败"
                )
            }
        }
    }

    /**
     * 开始播放当前页音频
     */
    private fun startPageAudio() {
        val page = _state.value.currentPage ?: return
        val audioUrl = page.audioUrl ?: return

        stopAudio()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setVolume(
                    _state.value.volume / 100f,
                    _state.value.volume / 100f
                )
                setOnPreparedListener { mp ->
                    mp.start()
                    startHighlightTracking()
                }
                setOnCompletionListener {
                    sendIntent(ArticleIntent.AudioCompleted)
                }
                setOnErrorListener { _, _, _ ->
                    sendIntent(ArticleIntent.AudioCompleted)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendIntent(ArticleIntent.AudioCompleted)
        }
    }

    /**
     * 暂停音频
     */
    private fun pauseAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        stopHighlightTracking()
    }

    /**
     * 恢复音频播放
     */
    private fun resumeAudio() {
        mediaPlayer?.let {
            it.start()
            startHighlightTracking()
        } ?: run {
            // 如果没有 mediaPlayer 说明可能还未播放过或已释放，重新开始
            startPageAudio()
        }
    }

    /**
     * 重新播放当前页
     */
    private fun replayAudio() {
        stopAudio()
        startPageAudio()
    }

    /**
     * 更新音量
     */
    private fun updateVolume(volume: Int) {
        val vol = volume / 100f
        mediaPlayer?.setVolume(vol, vol)
    }

    /**
     * 启动高亮词语追踪
     */
    private fun startHighlightTracking() {
        stopHighlightTracking()
        val words = _state.value.currentPage?.sentenceByXFList ?: return

        highlightRunnable = object : Runnable {
            override fun run() {
                val player = mediaPlayer ?: return
                if (!player.isPlaying) return

                val currentPositionMs = player.currentPosition
                val index = findHighlightIndex(words, currentPositionMs)
                if (index != _state.value.highlightIndex) {
                    _state.value = _state.value.copy(highlightIndex = index)
                }

                handler.postDelayed(this, 50)
            }
        }
        handler.post(highlightRunnable!!)
    }

    /**
     * 查找当前应高亮的词语索引
     */
    private fun findHighlightIndex(words: List<ArticleWord>, currentTimeMs: Int): Int {
        for (i in words.indices) {
            val word = words[i]
            if (currentTimeMs in word.wb..word.we) {
                return i
            }
        }
        // 如果在两个词之间的间隙，找最近的前一个已经结束的词
        for (i in words.indices.reversed()) {
            if (currentTimeMs > words[i].we) {
                return i
            }
        }
        return -1
    }

    /**
     * 停止高亮追踪
     */
    private fun stopHighlightTracking() {
        highlightRunnable?.let { handler.removeCallbacks(it) }
        highlightRunnable = null
    }

    /**
     * 停止音频播放
     */
    private fun stopAudio() {
        stopHighlightTracking()
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
