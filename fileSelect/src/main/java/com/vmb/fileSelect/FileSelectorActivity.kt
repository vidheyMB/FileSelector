package com.vmb.fileSelect

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_file_selector.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FileSelectorActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FileSelector"

        /** Request camera permission code*/
        private const val PERMISSION_REQUEST_CODE = 101

        /** camera image uri (contains path for file destination) */
        private lateinit var outputFileUri: Uri

        /** Start Intent request code for result fetch */
        private const val OPEN_DOCUMENT_REQUEST_CODE = 190

        /** Get file extensions for document fetch */
        private lateinit var filesExtensions:Array<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_selector)

        if(intent!=null)
            filesExtensions = intent.extras?.getStringArray("FileExtension")!!

        // initial
        progressLayout.visibility = View.GONE

        // hide action bar
        supportActionBar?.hide()

        if(checkPermission()) {
            // open document
            openCameraOrDocument()
        }else{
            requestPermission()
        }


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

        if(filesExtensions.isNullOrEmpty())
            filesExtensions.toMutableList().add("*/*")  // set default ALL files

        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, filesExtensions
                /* arrayOf(
                    "application/pdf", // .pdf
                    "text/plain", // .txt
                    "image/*",// images
                    "application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                    "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                    "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                )*/*/
            )
        }
    }

    /** Get the response data and output in base64 format */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_CANCELED){
            // close on request canceled
            cancel()

        }else if (resultCode == RESULT_OK) {
            if (requestCode == OPEN_DOCUMENT_REQUEST_CODE) {

                // If data is null check outputFileUri
                val uri = if (data != null) data.data else outputFileUri

                if (uri != null) {

                    // Send uri to convert Base64 String
                    FileSelector.getUriForConverter(uri)
                    // finish activity
                    finish()

                } else cancel() // finish activity
            }
        } else {
            // close on back press
            cancel()
        }

    }

    private fun cancel() {
        Log.e(TAG, "onActivityResult: Request Canceled")
        FileSelector.destroy()
        finish()
    }

    /** Request Camera permission */

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()

                // open document
                openCameraOrDocument()

            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        showMessageOKCancel("You need to allow access permissions",
                            DialogInterface.OnClickListener { dialog, which ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermission()
                                }
                            })
                    }
                }
            }
        }

    }


    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                cancel()
            })
            .create()
            .show()
    }

}