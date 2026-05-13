package com.example.testdemo.data.network

import com.example.testdemo.model.ApiResponse
import com.example.testdemo.model.ArticleDetail
import com.example.testdemo.model.ArticleListResponse
import com.example.testdemo.model.ArticleType
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 文库相关接口
 */
interface ApiService {

    /**
     * 获取文章类型列表
     */
    @GET("advenglish/library/articleTypeList")
    suspend fun getArticleTypes(): ApiResponse<List<ArticleType>>

    /**
     * 获取文章难度等级列表
     */
    @GET("advenglish/appArticle/selectList")
    suspend fun getDifficultyList(): ApiResponse<List<Int>>

    /**
     * 获取文章列表
     */
    @GET("advenglish/library/articleList")
    suspend fun getArticles(
        @Query("lexile") lexile: Int?,
        @Query("typeId") typeId: Int?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<ArticleListResponse>

    /**
     * 获取文章详情
     */
    @GET("knowledge/article/getArticleDetail")
    suspend fun getArticleDetail(
        @Query("aid") aid: Int
    ): ApiResponse<ArticleDetail>
}
