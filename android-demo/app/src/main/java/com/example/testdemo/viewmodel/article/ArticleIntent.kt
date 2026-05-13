package com.example.testdemo.viewmodel.article

import com.example.testdemo.viewmodel.mvi.UiIntent

/**
 * 文章阅读页面 Intent
 */
sealed class ArticleIntent : UiIntent {
    /** 加载文章详情 */
    data class LoadArticle(val aid: Int) : ArticleIntent()
    /** 切换到指定页 */
    data class GoToPage(val pageIndex: Int) : ArticleIntent()
    /** 下一页 */
    object NextPage : ArticleIntent()
    /** 上一页 */
    object PrevPage : ArticleIntent()
    /** 播放/暂停 */
    object TogglePlayPause : ArticleIntent()
    /** 重新播放当前页 */
    object Replay : ArticleIntent()
    /** 切换音量面板显示 */
    object ToggleVolumePanel : ArticleIntent()
    /** 设置音量 */
    data class SetVolume(val volume: Int) : ArticleIntent()
    /** 更新当前高亮词语索引 */
    data class UpdateHighlightIndex(val index: Int) : ArticleIntent()
    /** 音频播放完成 */
    object AudioCompleted : ArticleIntent()
    /** 加载完成，数据就绪 */
    data class DataLoaded(
        val title: String,
        val totalPages: Int
    ) : ArticleIntent()
    /** 加载失败 */
    data class LoadFailed(val error: String) : ArticleIntent()
}
