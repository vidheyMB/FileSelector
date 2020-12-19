package com.vmb.fileSelect

import java.io.Serializable

/** CallBack to the activity after data fetch*/
interface FileSelectorCallBack : Serializable{
    fun onResponse(response: String, extension: String)
}