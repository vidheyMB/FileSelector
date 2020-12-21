package com.vmb.fileselector

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vmb.fileSelect.FileSelector
import com.vmb.fileSelect.FileSelectorCallBack
import com.vmb.fileSelect.FileSelectorData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var base64Result:String
    lateinit var fileExtension:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        file_selector.setOnClickListener {
            FileSelector.open(this)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == FileSelector.FileSelectorResult){
            val uri = Uri.parse(data?.getStringExtra(FileSelector.FileSelectorData))
            FileSelector.filterSelectorConverter(this, uri!!, object : FileSelectorCallBack {
                override fun onResponse(responseInBase64: String, fileName: String, extension: String) {
                    base64Result = responseInBase64
                    fileExtension = extension
                    Log.d("FileSelector", "onActivityResult: $base64Result")
                    Log.d("FileSelector", "onActivityResult: $extension")
                }
            })


        }

    }
}