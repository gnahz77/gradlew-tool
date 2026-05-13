package com.example.testdemo.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * 占位Fragment - 我的
 */
class ProfileFragment : Fragment() {
    
    companion object {
        fun newInstance() = ProfileFragment()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = "我的"
            textSize = 24f
            gravity = android.view.Gravity.CENTER
        }
    }
}
