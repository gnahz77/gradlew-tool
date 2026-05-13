package com.example.testdemo.view.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testdemo.R

/**
 * 主页面
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btn_1).setOnClickListener {
            startActivity(EditProfileActivity.newIntent(this))
        }
        findViewById<Button>(R.id.btn_2).setOnClickListener {
            startActivity(LibraryActivity.newIntent(this))
        }
        findViewById<Button>(R.id.btn_3).setOnClickListener {
            startActivity(ArticleActivity.newIntent(this))
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_1 -> {
                startActivity(EditProfileActivity.newIntent(this))
            }
            R.id.btn_2 -> {
                startActivity(LibraryActivity.newIntent(this))
            }
            R.id.btn_3 -> {
                startActivity(ArticleActivity.newIntent(this))
            }
        }
    }

}
