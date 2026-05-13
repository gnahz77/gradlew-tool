package com.example.testdemo.viewmodel.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testdemo.data.network.RetrofitClient
import com.example.testdemo.data.repository.LibraryRepository
import com.example.testdemo.model.ArticleType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

/**
 * 文库页面 ViewModel
 */
class LibraryViewModel(
    private val reducer: LibraryReducer = LibraryReducer(),
    private val repository: LibraryRepository = LibraryRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state

    private val intentChannel = Channel<LibraryIntent>(Channel.BUFFERED)

    init {
        processIntents()
    }

    fun sendIntent(intent: LibraryIntent) {
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }

    private fun processIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                val current = _state.value
                _state.value = reducer.reduce(current, intent)
                handleSideEffects(intent)
            }
        }
    }

    private fun handleSideEffects(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.LoadInitial -> loadInitial()
            is LibraryIntent.SelectCategory -> loadArticles(1)
            is LibraryIntent.SelectDifficulty -> loadArticles(1)
            is LibraryIntent.LoadArticles -> loadArticles(intent.page)
        }
    }

    /**
     * 加载初始数据
     */
    private fun loadInitial() {
        viewModelScope.launch {
            val categoriesResult = repository.fetchArticleTypes()
            val difficultyResult = repository.fetchDifficultyList()

            val categories = categoriesResult.getOrElse { emptyList() }
            val difficulties = difficultyResult.getOrElse { emptyList() }

            val allCategories = mutableListOf(ArticleType(0, "精选阅读"))
            allCategories.addAll(categories)

            _state.value = _state.value.copy(
                isLoading = false,
                categories = allCategories,
                difficulties = difficulties,
                selectedDifficulty = difficulties.firstOrNull(),
                errorMessage = categoriesResult.exceptionOrNull()?.message
                    ?: difficultyResult.exceptionOrNull()?.message
            )

            loadArticles(1)
        }
    }

    /**
     * 加载文章列表
     */
    private fun loadArticles(page: Int) {
        viewModelScope.launch {
            val current = _state.value
            val result = repository.fetchArticles(
                lexile = current.selectedDifficulty,
                typeId = if (current.selectedCategoryId == 0) null else current.selectedCategoryId,
                page = page,
                size = 10
            )

            result.onSuccess { data ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    articles = data.list,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}
