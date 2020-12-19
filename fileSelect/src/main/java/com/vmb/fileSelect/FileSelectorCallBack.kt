package com.vmb.fileSelect

import java.io.Serializable

interface FileSelectorCallBack : Serializable{
    fun onResponse(response: String)
}