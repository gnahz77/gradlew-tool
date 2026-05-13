package com.example.testdemo.data.repository

import com.example.testdemo.data.network.ApiService
import com.example.testdemo.model.ArticleListResponse
import com.example.testdemo.model.ArticleType

/**
 * 文库数据仓库
 */
class LibraryRepository(
    private val apiService: ApiService
) {

    /**
     * 获取文章类型列表
     */
    suspend fun fetchArticleTypes(): Result<List<ArticleType>> {
        return runCatching {
            val response = apiService.getArticleTypes()
            if (response.code == 0) {
                response.data
            } else {
                throw IllegalStateException(response.msg)
            }
        }
    }

    /**
     * 获取难度等级列表
     */
    suspend fun fetchDifficultyList(): Result<List<Int>> {
        return runCatching {
            val response = apiService.getDifficultyList()
            if (response.code == 0) {
                response.data
            } else {
                throw IllegalStateException(response.msg)
            }
        }
    }

    /**
     * 获取文章列表
     */
    suspend fun fetchArticles(
        lexile: Int?,
        typeId: Int?,
        page: Int,
        size: Int
    ): Result<ArticleListResponse> {
        return runCatching {
            val response = apiService.getArticles(lexile, typeId, page, size)
            if (response.code == 0) {
                response.data
            } else {
                throw IllegalStateException(response.msg)
            }
        }
    }
}
