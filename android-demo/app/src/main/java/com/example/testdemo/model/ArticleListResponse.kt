package com.example.testdemo.model

/**
 * 文章列表响应数据类
 */
data class ArticleListResponse(
    val total: Int,
    val list: List<Article>,
    val pageNum: Int,
    val pageSize: Int,
    val size: Int,
    val startRow: Int,
    val endRow: Int,
    val pages: Int,
    val prePage: Int,
    val nextPage: Int,
    val isFirstPage: Boolean,
    val isLastPage: Boolean,
    val hasPreviousPage: Boolean,
    val hasNextPage: Boolean,
    val navigatePages: Int,
    val navigatepageNums: List<Int>,
    val navigateFirstPage: Int,
    val navigateLastPage: Int,
    val firstPage: Int,
    val lastPage: Int
)
