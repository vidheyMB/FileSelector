package com.vmb.fileSelect

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FileSelectorActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FileSelector"

        /** camera image uri (contains path for file destination) */
        private lateinit var outputFileUri: Uri

        /** Start Intent request code for result fetch */
        private const val OPEN_DOCUMENT_REQUEST_CODE = 190
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_selector)

        // open document
        openCameraOrDocument()

    }

    /** Intent chooser for selection of camera and document composer */
    private fun openCameraOrDocument() {

        /** Camera chooser */
        val cameraIntents: MutableList<Intent> = try {
            getCameraIntent()
        } catch (e: Exception) {
            Log.e(TAG, "Camera : Please check with camera permission and Storage permission")
            e.printStackTrace()
            mutableListOf<Intent>() // set empty list
        }

        /** Document chooser */
        val openDocument = getOpenDocumentIntent()

        // Select FileSystem Options.
        val chooserIntent = Intent.createChooser(openDocument, "Select Source")

        // Add the camera options.
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            cameraIntents.toTypedArray<Parcelable>()
        )

        startActivityForResult(chooserIntent, OPEN_DOCUMENT_REQUEST_CODE)

    }

    /** Open Camera and your optionals here*/
    @SuppressLint("QueryPermissionsNeeded", "SimpleDateFormat")
    private fun getCameraIntent(): MutableList<Intent> {
        // Camera.
        val cameraIntents: MutableList<Intent> = ArrayList()

        // Determine Uri of camera image to save.
        val root = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        val fname: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        // Create the File where the photo should go
        val sdImageMainDirectory = try {
            File.createTempFile(
                "IMAGE_${fname}_",       /* prefix */
                ".png",                 /* suffix */
                root                          /* directory */
            )
        } catch (e: Exception) {
            Log.e(TAG, "Camera : Please check with camera permission")
            e.printStackTrace()
            // Error occurred while creating the File
            null
        }

        // uri for image stored
        outputFileUri = Uri.fromFile(sdImageMainDirectory)

        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val packageManager: PackageManager = packageManager

        val listCam = packageManager.queryIntentActivities(captureIntent, 0)

        for (res in listCam) {
            val packageName = res.activityInfo.packageName
            val intent = Intent(captureIntent)
            intent.component = ComponentName(packageName, res.activityInfo.name)
            intent.setPackage(packageName)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            cameraIntents.add(intent)
        }

        return cameraIntents
    }

    /** Open documents and your optionals here*/
    private fun getOpenDocumentIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/pdf", // .pdf
                    "text/plain", // .txt
                    "image/*",// images
                    "application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                    "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                    "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                    "application/csv", // .Ms office
                )
            )
        }
    }

    /** Get the response data and output in base64 format */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == OPEN_DOCUMENT_REQUEST_CODE) {

                // If data is null check outputFileUri
                val uri = if (data != null) data.data else outputFileUri

                if (uri != null) {
                    // File name
                    val fileName = getFileName(uri)
                    // File extension
//                    val fileExtension = uri.toString().substringAfterLast(".")
                    val fileExtension = fileName!!.substringAfterLast(".")

                    // convert uri to base64 string
                    GlobalScope.launch {
                        val base64String = convertToString(uri, fileExtension)

                        // return the result back to activity
                        val intent = Intent()
                        intent.putExtra(
                            FileSelector.FileSelectorData, FileSelectorData(
                                responseInBase64 = base64String,
                                fileName = fileName,
                                extension = fileExtension
                            )
                        )
                        setResult(FileSelector.FileSelectorResult, intent)
                        // finish activity
                        finish()
                    }
                } else finish() // finish activity
            }
        } else {
            // close on back press
            finish()
        }

    }


    /** Convert any Uri to base64 string*/
    private fun convertToString(uri: Uri, extension: String): String {
        val uriString = uri.toString()
        Log.d("data", "onActivityResult: uri = $uriString")

        /** Return Base64 String */
        return try {

            if (isImage(extension)) {  // For Image

                val bitmap = getResizedBitmap(getCapturedImageAsBitmap(uri), 512)
                getBitmapToBase64(bitmap) // return base64 string

            } else { // For Other Files

                val inputStream: InputStream = contentResolver.openInputStream(uri)!!
                val bytes = getBytes(inputStream)
                Log.d("data", "onActivityResult: bytes size =" + bytes!!.size)
                val ansValue = Base64.encodeToString(bytes, Base64.DEFAULT)
                Log.d("data", "onActivityResult: Base64string = $ansValue")
                ansValue // return base64 string

            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("error", "onActivityResult: $e")
            ""  // return empty response
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

    /** Get Bitmap from Uri */
    private fun getCapturedImageAsBitmap(selectedPhotoUri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(
                application.contentResolver!!,
                selectedPhotoUri
            )
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                application.contentResolver,
                selectedPhotoUri
            )
        }
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
    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
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