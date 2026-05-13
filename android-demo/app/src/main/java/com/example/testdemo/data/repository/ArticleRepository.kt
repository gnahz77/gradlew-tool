package com.example.testdemo.data.repository

import com.example.testdemo.data.network.ApiService
import com.example.testdemo.model.ArticleDetail

/**
 * 文章详情数据仓库
 */
class ArticleRepository(
    private val apiService: ApiService
) {

    /**
     * 获取文章详情
     */
    suspend fun fetchArticleDetail(aid: Int): Result<ArticleDetail> {
        return runCatching {
            val response = apiService.getArticleDetail(aid)
            if (response.code == 0) {
                response.data
            } else {
                throw IllegalStateException(response.msg)
            }
        }
    }
}
