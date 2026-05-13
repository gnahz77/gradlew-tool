package com.example.testdemo.viewmodel.article

import com.example.testdemo.viewmodel.mvi.Reducer

/**
 * 文章阅读页面 Reducer
 */
class ArticleReducer : Reducer<ArticleState, ArticleIntent> {

    override fun reduce(currentState: ArticleState, intent: ArticleIntent): ArticleState {
        return when (intent) {
            is ArticleIntent.LoadArticle -> currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            is ArticleIntent.DataLoaded -> currentState.copy(
                isLoading = false,
                title = intent.title,
                totalPages = intent.totalPages,
                errorMessage = null
            )

            is ArticleIntent.LoadFailed -> currentState.copy(
                isLoading = false,
                errorMessage = intent.error
            )

            is ArticleIntent.GoToPage -> {
                val pageIndex = intent.pageIndex.coerceIn(0, currentState.totalPages - 1)
                currentState.copy(
                    currentPageIndex = pageIndex,
                    highlightIndex = -1,
                    isPlaying = true
                )
            }

            is ArticleIntent.NextPage -> {
                if (currentState.currentPageIndex < currentState.totalPages - 1) {
                    currentState.copy(
                        currentPageIndex = currentState.currentPageIndex + 1,
                        highlightIndex = -1,
                        isPlaying = true
                    )
                } else {
                    currentState
                }
            }

            is ArticleIntent.PrevPage -> {
                if (currentState.currentPageIndex > 0) {
                    currentState.copy(
                        currentPageIndex = currentState.currentPageIndex - 1,
                        highlightIndex = -1,
                        isPlaying = true
                    )
                } else {
                    currentState
                }
            }

            is ArticleIntent.TogglePlayPause -> currentState.copy(
                isPlaying = !currentState.isPlaying
            )

            is ArticleIntent.Replay -> currentState.copy(
                highlightIndex = -1,
                isPlaying = true
            )

            is ArticleIntent.ToggleVolumePanel -> currentState.copy(
                isVolumePanelVisible = !currentState.isVolumePanelVisible
            )

            is ArticleIntent.SetVolume -> currentState.copy(
                volume = intent.volume.coerceIn(0, 100)
            )

            is ArticleIntent.UpdateHighlightIndex -> currentState.copy(
                highlightIndex = intent.index
            )

            is ArticleIntent.AudioCompleted -> currentState.copy(
                isPlaying = false
            )
        }
    }
}
