package com.example.testdemo.viewmodel.library

import com.example.testdemo.model.Article
import com.example.testdemo.model.ArticleType
import com.example.testdemo.viewmodel.mvi.UiState

/**
 * 文库页面 State
 */
data class LibraryState(
    val isLoading: Boolean = false,
    // 分类列表
    val categories: List<ArticleType> = emptyList(),
    // 难度列表
    val difficulties: List<Int> = emptyList(),
    // 文章列表
    val articles: List<Article> = emptyList(),
    // 当前选中的分类 ID
    val selectedCategoryId: Int = 0,
    // 当前选中的难度
    val selectedDifficulty: Int? = null,
    val errorMessage: String? = null
) : UiState
