package com.vmb.fileSelect

import android.app.Activity
import android.content.Intent


/**
 *  FileSelector is a library to select File or Capture image form mobile device
 *
 * */

object FileSelector {

    fun build(activity: Activity, fileSelectorCallBack: FileSelectorCallBack) {
        val intent = Intent(activity, FileSelectorActivity::class.java)
        intent.putExtra("interfaceCall",fileSelectorCallBack)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activity.startActivity(intent)
    }

}