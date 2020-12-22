package com.vmb.fileSelect

import android.graphics.Bitmap
import android.net.Uri
import java.io.Serializable

data class FileSelectorData(
    var uri: String?=null,
    var responseInBase64: String?=null,
    var fileName: String?=null,
    var extension: String?=null,
    var bytes: ByteArray?=null,
    var imageBitmap: Bitmap?=null,
    var thumbnail: Bitmap?=null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileSelectorData

        if (bytes != null) {
            if (other.bytes == null) return false
            if (!bytes.contentEquals(other.bytes)) return false
        } else if (other.bytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes?.contentHashCode() ?: 0
    }
}
