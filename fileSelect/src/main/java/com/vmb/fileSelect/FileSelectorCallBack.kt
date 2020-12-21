package com.vmb.fileSelect

interface FileSelectorCallBack {
    fun onResponse(responseInBase64: String, fileName: String, extension: String)
}