package com.vmb.fileSelect

import android.app.Activity
import android.content.Intent


/**
 *  FileSelector is a library to select File or Capture image form mobile device.
 *
 *  Developed by -> Vidhey
 * */

object FileSelector{

    // ActivityForResult Code
    const val FileSelectorResult = 1012
    const val FileSelectorData = "data"

    fun open(activity: Activity) {
        /** Call Intent chooser activity*/
        val intent = Intent(activity, FileSelectorActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activity.startActivityForResult(intent, FileSelectorResult)

    }

}