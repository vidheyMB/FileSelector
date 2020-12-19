package com.vmb.fileSelect

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
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

    companion object{
        private const val TAG = "FileSelector"

        /** FileSelector callback response as base64 string */
        private lateinit var fileSelectorCallBack:FileSelectorCallBack

        /** camera image uri (contains path for file destination) */
        private lateinit var outputFileUri:Uri

        /** Start Intent resquest code for result fetch */
        private const val OPEN_DOCUMENT_REQUEST_CODE = 190
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_selector)

        // To retrieve object in second Activity
        fileSelectorCallBack = intent.getSerializableExtra("interfaceCall") as FileSelectorCallBack

        // open document
        openDocument()

    }

    /** Intent chooser for selection of camera and document composer */
    private fun openDocument() {

        /** Camera chooser */
        val cameraIntents: MutableList<Intent> = getCameraIntent()

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
            Log.e(TAG, "Camera : Please check with camera permission", )
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
    private fun getOpenDocumentIntent() : Intent{
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/pdf", // .pdf
                    "application/vnd.oasis.opendocument.text", // .odt
                    "text/plain", // .txt
                    "image/*" // images
                )
            )
        }
    }

    /** Get the response data and output in base64 format */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK){
           if(requestCode == OPEN_DOCUMENT_REQUEST_CODE){

               // If data is null check outputFileUri
               val uri = if(data!=null) data.data else outputFileUri

               // convert uri to base64 string
               GlobalScope.launch {
                  val base64String = convertToString(uri!!)
                    // callback interface
                   fileSelectorCallBack.onResponse(base64String)
                   // finish activity
                   finish()
               }
           }
        }else{
            // close on back press
            finish()
        }

    }


    /** Convert any Uri to base64 string*/
    private fun convertToString(uri: Uri): String {
        val uriString = uri.toString()
        Log.d("data", "onActivityResult: uri$uriString")

      return try {
            val inputStream: InputStream = contentResolver.openInputStream(uri)!!
            val bytes = getBytes(inputStream)
            Log.d("data", "onActivityResult: bytes size=" + bytes!!.size)
            val ansValue = Base64.encodeToString(bytes, Base64.DEFAULT)
            Log.d("data", "onActivityResult: Base64string = $ansValue")
            ansValue // return base64 string
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("error", "onActivityResult: $e")
            ""  // return empty response
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

}