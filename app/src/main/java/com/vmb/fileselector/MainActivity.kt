package com.vmb.fileselector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vmb.fileSelect.FileSelector
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        file_selector.setOnClickListener {
            FileSelector.build(this)
        }
    }
}