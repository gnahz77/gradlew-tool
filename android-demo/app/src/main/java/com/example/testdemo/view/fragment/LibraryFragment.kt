package com.example.testdemo.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testdemo.R
import com.example.testdemo.view.adapter.ArticleAdapter
import com.example.testdemo.view.adapter.CategoryAdapter
import com.example.testdemo.view.adapter.DifficultyAdapter
import com.example.testdemo.viewmodel.library.LibraryIntent
import com.example.testdemo.viewmodel.library.LibraryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 文库页面Fragment
 */
class LibraryFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvDifficulty: RecyclerView
    private lateinit var rvArticles: RecyclerView

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var difficultyAdapter: DifficultyAdapter
    private lateinit var articleAdapter: ArticleAdapter

    private val viewModel: LibraryViewModel by viewModels()
    private var currentPage = 1
    private var headerBaseHeight = 0

    companion object {
        fun newInstance() = LibraryFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupInsets(view)
        observeState()
        viewModel.sendIntent(LibraryIntent.LoadInitial)
    }

    private fun setupInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 将标题栏延伸到状态栏区域
            val header = v.findViewById<View>(R.id.gradientHeader)
            if (header != null) {
                header.setPadding(0, systemBars.top, 0, 0)
                if (headerBaseHeight == 0) {
                    headerBaseHeight = header.layoutParams.height
                }
                val params = header.layoutParams
                params.height = headerBaseHeight + systemBars.top
                header.layoutParams = params
            }

            insets
        }
    }

    private fun initViews(view: View) {
        // 设置类别 RecyclerView
        rvCategory = view.findViewById(R.id.rvCategory)
        rvCategory.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoryAdapter(emptyList()) { category, position ->
            currentPage = 1
            viewModel.sendIntent(LibraryIntent.SelectCategory(category, position))
        }
        rvCategory.adapter = categoryAdapter

        // 设置难度 RecyclerView
        rvDifficulty = view.findViewById(R.id.rvDifficulty)
        rvDifficulty.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        difficultyAdapter = DifficultyAdapter(emptyList()) { difficulty, _ ->
            currentPage = 1
            viewModel.sendIntent(LibraryIntent.SelectDifficulty(difficulty))
        }
        rvDifficulty.adapter = difficultyAdapter

        // 设置文章 RecyclerView
        rvArticles = view.findViewById(R.id.rvArticles)
        rvArticles.layoutManager = LinearLayoutManager(requireContext())
        articleAdapter = ArticleAdapter(emptyList()) { article ->
            Toast.makeText(requireContext(), "点击了: ${article.title}", Toast.LENGTH_SHORT).show()
        }
        rvArticles.adapter = articleAdapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                if (state.categories.isNotEmpty()) {
                    categoryAdapter.updateCategories(state.categories)
                }
                if (state.difficulties.isNotEmpty()) {
                    difficultyAdapter.updateDifficulties(state.difficulties)
                }
                articleAdapter.updateArticles(state.articles)

                state.errorMessage?.let { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
