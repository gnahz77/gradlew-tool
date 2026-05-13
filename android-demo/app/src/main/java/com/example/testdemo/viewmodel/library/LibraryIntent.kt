package com.example.testdemo.viewmodel.library

import com.example.testdemo.model.ArticleType
import com.example.testdemo.viewmodel.mvi.UiIntent

/**
 * 文库页面 Intent
 */
sealed class LibraryIntent : UiIntent {
    // 加载初始数据
    object LoadInitial : LibraryIntent()
    /**
     * 选择分类
     * @param category 文章分类
     * @param position 位置索引
     */
    data class SelectCategory(val category: ArticleType, val position: Int) : LibraryIntent()
    /**
     * 选择难度
     * @param difficulty 难度值
     */
    data class SelectDifficulty(val difficulty: Int) : LibraryIntent()
    /**
     * 加载文章列表
     * @param page 页码
     */
    data class LoadArticles(val page: Int) : LibraryIntent()
}
