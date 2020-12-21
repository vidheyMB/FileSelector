package com.vmb.fileselector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vmb.fileSelect.FileSelector
import com.vmb.fileSelect.FileSelectorData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var base64Result:String
    lateinit var extension:String

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
            base64Result = (data?.getSerializableExtra(FileSelector.FileSelectorData) as FileSelectorData).responseInBase64
            extension = (data?.getSerializableExtra(FileSelector.FileSelectorData) as FileSelectorData).extension
            Log.d("FileSelector", "onActivityResult: $base64Result")
            Log.d("FileSelector", "onActivityResult: $extension")
        }

    }
}