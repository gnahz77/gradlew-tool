package com.example.testdemo.viewmodel.article

import com.example.testdemo.model.ArticleContent
import com.example.testdemo.model.ArticleDetail
import com.example.testdemo.viewmodel.mvi.UiState

/**
 * 文章阅读页面 State
 */
data class ArticleState(
    val isLoading: Boolean = false,
    /** 文章详情 */
    val articleDetail: ArticleDetail? = null,
    /** 文章标题 */
    val title: String = "",
    /** 当前页索引（从0开始） */
    val currentPageIndex: Int = 0,
    /** 总页数 */
    val totalPages: Int = 0,
    /** 是否正在播放 */
    val isPlaying: Boolean = false,
    /** 是否显示音量面板 */
    val isVolumePanelVisible: Boolean = false,
    /** 音量 0-100 */
    val volume: Int = 80,
    /** 当前高亮词语索引 */
    val highlightIndex: Int = -1,
    /** 错误信息 */
    val errorMessage: String? = null
) : UiState {

    /** 当前页内容 */
    val currentPage: ArticleContent?
        get() = articleDetail?.contentList?.getOrNull(currentPageIndex)
}
