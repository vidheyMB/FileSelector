package com.vmb.fileSelect

import android.net.Uri
import java.io.Serializable

data class FileSelectorData(
    var uri: String?=null,
    var responseInBase64: String?=null,
    var fileName: String?=null,
    var extension: String?=null
):Serializable
