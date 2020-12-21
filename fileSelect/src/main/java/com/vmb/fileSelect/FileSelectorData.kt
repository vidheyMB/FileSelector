package com.vmb.fileSelect

import java.io.Serializable

data class FileSelectorData(
    val responseInBase64: String,
    val extension: String
):Serializable
