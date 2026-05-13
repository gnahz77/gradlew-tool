package com.example.testdemo.model

/**
 * 通用API响应数据类
 */
data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val data: T
)
