package com.vmb.fileSelect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


/**
 *  FileSelector is a library to select File or Capture image form mobile device.
 *
 *  Developed by : Vidhey
 * */

object FileSelector {

    // ActivityForResult Code
    const val FileSelectorResult = 1012
    const val FileSelectorData = "data"

    fun open(activity: Activity, fragment: Fragment? = null) {
        /** Call Intent chooser activity*/
        val intent = Intent(activity, FileSelectorActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (fragment == null)
            activity.startActivityForResult(
                intent,
                FileSelectorResult
            ) // Get Result Back in Activity
        else fragment.startActivityForResult(
            intent,
            FileSelectorResult
        ) // Get Result Back in Fragment
    }


    /** Convert any Uri to base64 string*/
    fun filterSelectorConverter(
        context: Context,
        uri: Uri,
        fileSelectorCallBack: FileSelectorCallBack
    ) {
        // show dialog
        ProgressDialogue.showDialog(context)
        // convert uri to base64 string
        GlobalScope.launch(Dispatchers.IO) {
            // File name
            val fileName = getFileName(context, uri)
            // File extension
            val fileExtension = fileName!!.substringAfterLast(".")

            // Convert Uri to Base64String
            val base64String = convertToString(context, uri, fileExtension)

            launch(Dispatchers.Main) {
                ProgressDialogue.dismissDialog() // hide dialog
                fileSelectorCallBack.onResponse(base64String, fileName, fileExtension) // call back interface
            }
        }

    }

    /** Convert any Uri to base64 string*/
    private fun convertToString(context: Context, uri: Uri, extension: String): String {
        val uriString = uri.toString()
        Log.d("data", "onActivityResult: uri = $uriString")

        /** Return Base64 String */
        return try {

            if (isImage(extension)) {  // For Image

                val bitmap = getResizedBitmap(getCapturedImageAsBitmap(context, uri), 512)
                getBitmapToBase64(bitmap) // return base64 string

            } else { // For Other Files

                val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
                val bytes = getBytes(inputStream)
                Log.d("data", "onActivityResult: bytes size =" + bytes!!.size)
                val ansValue = Base64.encodeToString(bytes, Base64.DEFAULT)
                Log.d("data", "onActivityResult: Base64string = $ansValue")

                inputStream.close() // close input stream

                ansValue // return base64 string

            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("error", "onActivityResult: $e")
            ""  // return empty response
        }
    }


    /** Get Bitmap from Uri */
    private fun getCapturedImageAsBitmap(context: Context, selectedPhotoUri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(
                context.contentResolver!!,
                selectedPhotoUri
            )
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                selectedPhotoUri
            )
        }
    }

    /** Get InputStream as ByteArray */
    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream): ByteArray? {
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
    fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    /** Get isImage extension*/
    fun isImage(extension: String): Boolean {
        /** Image Formats*/
        val imageExtension = arrayOf(
            "tif",
            "tiff",
            "bmp",
            "jpg",
            "jpeg",
            "gif",
            "png",
            "eps",
            "raw",
            "cr2",
            "nef",
            "orf",
            "sr2",
            "psd"
        )

        /** Check extension is image*/
        return imageExtension.contains(extension)
    }

    /** Get Bitmap as Base64String */
    fun getBitmapToBase64(bitmap: Bitmap): String {
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

    /** Get File name from Uri */
    @SuppressLint("Recycle")
//    @Throws(Exception::class)
    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

}