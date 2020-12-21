package com.vmb.fileSelect

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment


/**
 *  FileSelector is a library to select File or Capture image form mobile device.
 *
 *  Developed by -> Vidhey
 * */

object FileSelector{

    // ActivityForResult Code
    const val FileSelectorResult = 1012
    const val FileSelectorData = "data"

    fun open(activity: Activity, fragment: Fragment?=null) {
        /** Call Intent chooser activity*/
        val intent = Intent(activity, FileSelectorActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        if(fragment==null)
        activity.startActivityForResult(intent, FileSelectorResult) // Get Result Back in Activity
        else fragment.startActivityForResult(intent, FileSelectorResult) // Get Result Back in Fragment
    }

}