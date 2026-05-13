package com.example.testdemo.model

/**
 * 文章数据类
 */
data class Article(
    val id: Int,
    val title: String,
    val wordNum: Int,
    val lexile: Int,
    val typeId: Int,
    val type: String,
    val cover: String,
    val clickRatio: String?,
    val accuracy: String?,
    val accuracyRatio: String?,
    val color: String?,
    val isRead: Int,
    val readTime: String?,
    val stage: String?,
    val perception: String?
)
