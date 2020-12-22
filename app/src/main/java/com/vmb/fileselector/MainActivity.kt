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
            FileSelector.open(this, object : FileSelectorCallBack {
                override fun onResponse(fileSelectorData: FileSelectorData) {
                    base64Result = fileSelectorData.responseInBase64!!
                    fileExtension = fileSelectorData.extension!!

                    image.setImageBitmap(fileSelectorData.thumbnail)
                    fileName.text = fileSelectorData.fileName

                    Log.d("FileSelector", "onActivityResult: $base64Result")
                    Log.d("FileSelector", "onActivityResult: $fileExtension")
                }
            })
        }

    }

}