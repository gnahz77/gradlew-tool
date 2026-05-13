package com.example.testdemo.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * 占位Fragment - 听力
 */
class ListeningFragment : Fragment() {
    
    companion object {
        fun newInstance() = ListeningFragment()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = "听力"
            textSize = 24f
            gravity = android.view.Gravity.CENTER
        }
    }
}
