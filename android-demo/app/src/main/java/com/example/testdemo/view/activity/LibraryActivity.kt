package com.example.testdemo.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.testdemo.R
import com.example.testdemo.view.fragment.HomeFragment
import com.example.testdemo.view.fragment.LibraryFragment
import com.example.testdemo.view.fragment.ListeningFragment
import com.example.testdemo.view.fragment.ProfileFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 文库主页面
 */
class LibraryActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private var tabLayoutBaseHeight = 0

    companion object {
        fun newIntent(activity: AppCompatActivity) = Intent(activity, LibraryActivity::class.java)
        
        private val TAB_TITLES = arrayOf(
            R.string.tab_home,
            R.string.tab_listening,
            R.string.tab_library,
            R.string.tab_profile
        )
        
        private val TAB_ICONS = arrayOf(
            R.drawable.ic_home,
            R.drawable.ic_listening,
            R.drawable.ic_library,
            R.drawable.ic_profile
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_library)
        
        initViews()
        setupViewPager()
        setupTabLayout()
        
        // 将默认标签设置为“文库”（index 2）
        viewPager.setCurrentItem(2, false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Fragment内容延伸到顶部
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            
            // 处理 TabLayout 的底部导航栏
            tabLayout.setPadding(0, 0, 0, systemBars.bottom)
            if (tabLayoutBaseHeight == 0) {
                tabLayoutBaseHeight = tabLayout.layoutParams.height
            }
            val params = tabLayout.layoutParams
            params.height = tabLayoutBaseHeight + systemBars.bottom
            tabLayout.layoutParams = params
            
            insets
        }
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupViewPager() {
        viewPager.adapter = ViewPagerFragmentAdapter(this)
        viewPager.isUserInputEnabled = false
    }

    private fun setupTabLayout() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setText(TAB_TITLES[position])
            tab.setIcon(TAB_ICONS[position])
        }.attach()
    }

    /**
     * ViewPager2的Fragment适配器
     */
    private inner class ViewPagerFragmentAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment.newInstance()
                1 -> ListeningFragment.newInstance()
                2 -> LibraryFragment.newInstance()
                3 -> ProfileFragment.newInstance()
                else -> LibraryFragment.newInstance()
            }
        }
    }
}
