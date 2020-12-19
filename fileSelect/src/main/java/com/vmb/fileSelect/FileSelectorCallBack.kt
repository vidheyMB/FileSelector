package com.vmb.fileSelect

import android.os.Parcelable
import java.io.Serializable

/** CallBack to the activity after data fetch*/
interface FileSelectorCallBack : Parcelable{
    fun onResponse(response: String, extension: String)
}