package com.example.testdemo.viewmodel.mvi

/**
 * MVI Reducer 基类
 */
interface Reducer<S : UiState, I : UiIntent> {
    fun reduce(currentState: S, intent: I): S
}
