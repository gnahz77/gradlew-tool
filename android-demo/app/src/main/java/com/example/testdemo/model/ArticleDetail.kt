package com.example.testdemo.model

/**
 * 文章详情数据类
 */
data class ArticleDetail(
    val id: Int,
    val cover: String?,
    val title: String?,
    val wordNum: Int?,
    val readCount: Int?,
    val bgmUrl: String?,
    val typeId: Int?,
    val typeName: String?,
    val level: Int?,
    val talkItContent: String?,
    val talkItAudio: String?,
    val imgList: List<String>?,
    val readReportId: Int?,
    val readId: Int?,
    val contentList: List<ArticleContent>?,
    val questionList: List<ArticleQuestion>?
)

/**
 * 文章内容分页
 */
data class ArticleContent(
    val pageNum: Int,
    val imgUrl: String?,
    val audioUrl: String?,
    val audioDuration: Int?,
    val sentence: String?,
    val frameType: Int?,
    val sentenceByXFList: List<ArticleWord>?
)

/**
 * 分词信息
 */
data class ArticleWord(
    val word: String,
    val wb: Int,
    val we: Int
)

/**
 * 阅读理解题目
 */
data class ArticleQuestion(
    val question: String?,
    val questionAudio: String?,
    val questionImg: String?,
    val correctAnswer: String?,
    val answerList: List<ArticleAnswer>?
)

/**
 * 阅读理解选项
 */
data class ArticleAnswer(
    val answer: String?,
    val audio: String?
)
