package com.vmb.fileSelect

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/** Get InputStream as ByteArray */
@Throws(IOException::class)
private fun getBytes(inputStream: InputStream): ByteArray? {
    val byteBuffer = ByteArrayOutputStream()
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)
    var len = 0
    while (inputStream.read(buffer).also { len = it } != -1) {
        byteBuffer.write(buffer, 0, len)
    }
    return byteBuffer.toByteArray()
}

/** Get Bitmap as ByteArray */
private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

/** Get isImage extension*/
private fun isImage(extension: String): Boolean {
    /** Image Formats*/
    val imageExtension = arrayListOf<String>()
    imageExtension.add("tif")
    imageExtension.add("tiff")
    imageExtension.add("bmp")
    imageExtension.add("jpg")
    imageExtension.add("jpeg")
    imageExtension.add("gif")
    imageExtension.add("png")
    imageExtension.add("eps")
    imageExtension.add("raw")
    imageExtension.add("cr2")
    imageExtension.add("nef")
    imageExtension.add("orf")
    imageExtension.add("sr2")

    /** Check extension is image*/
    return imageExtension.contains(extension)
}

/** Get Bitmap as Base64String */
private fun getBitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 10, stream) //compress to which format you want.
    val byte_arr = stream.toByteArray()
    return Base64.encodeToString(byte_arr, Base64.DEFAULT)
}

/** Resize Bitmap size */
fun getResizedBitmap(image: Bitmap?, maxSize: Int): Bitmap {
    var width = image?.width
    var height = image?.height
    val bitmapRatio = width!!.toFloat() / height!!.toFloat()
    if (bitmapRatio > 1) {
        width = maxSize
        height = (width / bitmapRatio).toInt()
    } else {
        height = maxSize
        width = (height * bitmapRatio).toInt()
    }
    return Bitmap.createScaledBitmap(image!!, width, height, true)
}
