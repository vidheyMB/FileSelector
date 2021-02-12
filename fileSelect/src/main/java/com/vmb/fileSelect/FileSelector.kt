package com.vmb.fileSelect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


/**
 *  FileSelector is a library to select File or Capture image form mobile device.
 *
 *  Developed by : Vidhey
 *  Created on : 18/12/2020
 * */


/**
 *   File types for selection
 * */
enum class FileType{ PDF, IMAGES, Text, MS_WORD, MS_EXCEL, MS_POWER_POINT, ALL }

@SuppressLint("StaticFieldLeak")
object FileSelector {

    //create a new Job
    private val parentJob = Job()
    //create a coroutine context with the job and the dispatcher
    private val coroutineContext : CoroutineContext get() = parentJob + Dispatchers.Default
    //create a coroutine scope with the coroutine context
    val scope = CoroutineScope(coroutineContext)


    /* Host context */
    private lateinit var context: Context
    private lateinit var activity: Activity
    /* Callback to host */
    private lateinit var fileSelectorCallBack: FileSelectorCallBack
    /* Set empty data model */
    private val fileSelectorData = FileSelectorData()
    /* init file types  */
    /** Default set to ALL files */
    private val filesExtensions = arrayOf<String>().toMutableList()
    /* Base64String convertible file extensions */
    private val convertibleExtensions = arrayOf(
        "pdf", "txt", "doc", "docx", "ppt", "pptx", "xls", "xlsx" ,
        "bmp", "jpg", "jpeg", "gif", "png", "eps"
    )


    /** call open function and get response in callback interface */
    fun open(activity: Activity, fileSelectorCallBack: FileSelectorCallBack) {
        this.fileSelectorCallBack = fileSelectorCallBack
        fileSelectorIntent(activity = activity)
    }


    /** Redirect to Intent chooser Activity */
    private fun fileSelectorIntent(
        activity: Activity? = null
    ) {
        /** Call Intent chooser activity*/
         this.context = activity!!  // set Context
         this.activity = activity  // set Activity

        Log.d("GetObjectId", "fileSelectorObjectID: " + System.identityHashCode(FileSelector))

        if(this::context.isInitialized) {

            if (filesExtensions.isNullOrEmpty())
                filesExtensions.add("*/*")  // set default ALL files
            Log.d("TAG", "fileSelectorIntent_Track: Initialized")
            val intent = Intent(activity, FileSelectorActivity::class.java)
            intent.putExtra("FileExtension", filesExtensions.toTypedArray())
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent) // Call Activity

            // show dialog
            ProgressDialogue.showDialog(context)

        }else{
            Log.e("FileSelector", "fileSelectorIntent: context is not initialized", )
        }
    }


    /** Get Uri from FileSelectorActivity after File / Image selected */
    fun getUriForConverter(uri: Uri){
        // call uri to base64 converter
        if(this::context.isInitialized) {
            filterSelectorConverter(context, uri)
        }else{
            Log.e("FileSelector", "fileSelectorIntent_Track : context is null",)
        }
    }

    fun destroy(){
        // hide dialog
        ProgressDialogue.dismissDialog()
    }

    /** Convert any Uri to base64 string*/
    private fun filterSelectorConverter(
        context: Context,
        uri: Uri
    ) {
        scope.launch {

            // File name
            val fileName = getFileName(context, uri)
            // File extension
            val fileExtension = fileName!!.substringAfterLast(".")

            val base64String: String
            // Convert Uri to Base64String if fileExtension present
            if (convertibleExtensions.contains(fileExtension)) {
                base64String = convertToString(context, uri, fileExtension)
            } else {
                base64String = "" // if file format not convertible to base64 than empty result send
                fileSelectorData.thumbnail = getThumbnail(context, "etc") // set default thumbnail

                Log.e("FileSelector_error ", " Followed extensions can only be converted to base64 string, but other results you can get (like: uri, etc..) -> ")
                convertibleExtensions.forEachIndexed { index, s ->
                    Log.e("$index ", " $s ")
                }
            }

            fileSelectorData.uri = uri.toString()
            fileSelectorData.responseInBase64 = base64String
            fileSelectorData.fileName = fileName
            fileSelectorData.extension = fileExtension

            launch(Dispatchers.Main) {
                ProgressDialogue.dismissDialog() // hide dialog
                fileSelectorCallBack.onResponse(fileSelectorData) // call back interface
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

                val bitmap = getResizedBitmap(getCapturedImageAsBitmap(context, uri), 512) // get resized bitmap
                fileSelectorData.imageBitmap = bitmap //set bitmap

                fileSelectorData.thumbnail = fileSelectorData.imageBitmap //set thumbnail

                val bytes = getBytesFromBitmap(bitmap) // get bytes from bitmap
                Log.d("data", "onActivityResult: bytes size =" + bytes!!.size)
                fileSelectorData.bytes = bytes // set bytes

                val base64StringResponse = Base64.encodeToString(bytes, Base64.DEFAULT) // return base64 string
                Log.d("data", "onActivityResult: Base64string = $base64StringResponse")

                base64StringResponse // return base64 string

            } else { // For Other Files

                val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
                val bytes = getBytes(inputStream)
                Log.d("data", "onActivityResult: bytes size =" + bytes!!.size)
                fileSelectorData.bytes = bytes // set bytes

                fileSelectorData.thumbnail = getThumbnail(context, extension) // set thumbnail

                val base64StringResponse = Base64.encodeToString(bytes, Base64.DEFAULT)
                Log.d("data", "onActivityResult: Base64string = $base64StringResponse")

                inputStream.close() // close input stream

                base64StringResponse // return base64 string

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
        bitmap.compress(Bitmap.CompressFormat.PNG, 10, byteArrayOutputStream)
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
            "psd",
            "webp"
        )

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
    private fun getResizedBitmap(image: Bitmap?, maxSize: Int): Bitmap {
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
    @Throws(Exception::class)
    private fun getFileName(context: Context, uri: Uri): String? {
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


    /** Get Bitmap from Resource drawable */
    private fun getBitmapFromResource(context: Context, id: Int): Bitmap? {
       return BitmapFactory.decodeResource(context.resources, id)
    }

    /** Get Thumbnail based on extensions */
    @Throws(Exception::class)
    private fun getThumbnail(context: Context, extension: String): Bitmap? {
        val docTypes = arrayOf("pdf", "txt", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "etc")

        try {
            docTypes.forEach {
                if(it == extension.toLowerCase()){
                    // return bitmap of thumbnail
                   return getBitmapFromResource(context, context.resources.getIdentifier(extension.toLowerCase(), "drawable", context.packageName))
                }
            }
        } catch (e: Exception) {
        }

        return null // return null bitmap
    }


    /**
     *      "application/pdf", // .pdf
     *      "text/plain", // .txt
     *      "image/*",// images
     *      "application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
     *      "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
     *      "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
     *
     */
     */

    /** Set required file types */
    fun requiredFileTypes(vararg fileTypes: FileType): FileSelector{

        if(fileTypes.isNullOrEmpty()){
            /* Default set to ALL files*/
            filesExtensions.add("*/*")
        }else{
            fileTypes.forEach {
                when(it){
                    FileType.ALL -> filesExtensions.add("*/*")
                    FileType.PDF -> filesExtensions.add("application/pdf")
                    FileType.Text -> filesExtensions.add("text/plain")
                    FileType.IMAGES -> filesExtensions.add("image/*")
                    FileType.MS_WORD -> {
                        filesExtensions.add("application/msword")
                        filesExtensions.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    }
                    FileType.MS_POWER_POINT -> {
                        filesExtensions.add("application/vnd.ms-powerpoint")
                        filesExtensions.add("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                    }
                    FileType.MS_EXCEL -> {
                        filesExtensions.add("application/vnd.ms-excel")
                        filesExtensions.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    }
                }
            }
        }

        return this
    }

}