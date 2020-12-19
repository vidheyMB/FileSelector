package com.vmb.fileselector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vmb.fileSelect.FileSelector
import com.vmb.fileSelect.FileSelectorCallBack
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        file_selector.setOnClickListener {
            FileSelector.build(this, object : FileSelectorCallBack {
                override fun onResponse(response: String) {
                    Log.d("TAG_Test", "onResponse: $response")
                }
            })
        }
    }
}