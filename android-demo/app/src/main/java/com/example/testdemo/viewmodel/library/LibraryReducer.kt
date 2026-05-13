package com.example.testdemo.viewmodel.library

import com.example.testdemo.viewmodel.mvi.Reducer

/**
 * 文库页面 Reducer
 */
class LibraryReducer : Reducer<LibraryState, LibraryIntent> {
    override fun reduce(currentState: LibraryState, intent: LibraryIntent): LibraryState {
        return when (intent) {
            is LibraryIntent.LoadInitial -> currentState.copy(isLoading = true, errorMessage = null)
            is LibraryIntent.SelectCategory -> currentState.copy(
                selectedCategoryId = if (intent.position == 0) 0 else intent.category.id,
                errorMessage = null
            )
            is LibraryIntent.SelectDifficulty -> currentState.copy(
                selectedDifficulty = intent.difficulty,
                errorMessage = null
            )
            is LibraryIntent.LoadArticles -> currentState.copy(isLoading = true, errorMessage = null)
        }
    }
}
