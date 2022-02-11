package com.rafiul.renameapp

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private val SELECT_PICTURES = -9888
    private val list: MutableList<Uri> = mutableListOf()
    var extUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var btn: Button = findViewById(R.id.choose)
        var btn2: Button = findViewById(R.id.rename)


        btn.setOnClickListener(View.OnClickListener {
            list.clear()
            if (checkPermission()) {
                choose()
            } else {
                Log.d("no permission===== ", "onCreate: ")
                requestPermission()
            }
        })
        btn2.setOnClickListener(View.OnClickListener {
            if (list.size < 1)
                Toast.makeText(this@MainActivity, "Select Multiple Files", Toast.LENGTH_SHORT)
                    .show()
            renameOperation()
//            renameOperation2()
        })

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if(it.resultCode == RESULT_OK) {
                Toast.makeText(this@MainActivity, "Photo renamed successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Photo couldn't be rename", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun renameOperation2() {
//        val contentUri: Uri? = when (split[0]) {
//            "image" -> {
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            }
//            "video" -> {
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//            }
//            "audio" -> {
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//            }
//            "document" -> {
//                MediaStore.Files.getContentUri("external")
//            }
//            else -> null
//        }
        var c = 0
        for (item in list) {
            c += 1
            val currentFile = File(item.path)
            if (currentFile.exists()) {
                val newFile =
                    File(currentFile.parentFile, "the_rafiul${c}." + currentFile.extension)
//                val fromUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,item.id)
            }
            Log.d("no permission===== ", "onCreate: ${item}")

        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", applicationContext.packageName))
//                startActivityForResult(intent, 2296)
                activityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//                startActivityForResult(intent, 2296)
                activityResultLauncher.launch(intent)
            }
        }
    }

    private var activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(this@MainActivity, "Permission granted", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this@MainActivity, "Permission denied", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    private fun checkPermission(): Boolean {
        val x = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            false
        }
        Log.d("TAG", "checkPermission:------------ $x")
        return x
    }

    fun getIdFromDisplayName(displayName: String): Long? {
        val projection: Array<String>
        projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val cursor = contentResolver.query(
            extUri, projection,
            MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?", arrayOf(displayName), null
        )!!
        cursor.moveToFirst()
        if (cursor.count > 0) {
            val columnIndex = cursor.getColumnIndex(projection[0])
            val fileId = cursor.getLong(columnIndex)
            cursor.close()
            return fileId
        }
        return null
    }


    private fun renameOperation() {
        var c = 0
        var fu: Uri
        for (item in list) {
            c += 1
            item.let { fileUri ->
                fu = fileUri
                contentResolver.query(fileUri, null, null, null, null)
            }?.use { returnCursor ->
                val nameIndex =
                    returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = returnCursor!!.getColumnIndex(OpenableColumns.SIZE)

                returnCursor.moveToFirst()
                var id: Long = getIdFromDisplayName(returnCursor.getString(nameIndex))!!
//                var id: Long =  returnCursor.getLong(returnCursor.getColumnIndexOrThrow(BaseColumns._ID))
                val fromUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                ContentValues().also {
                    try {

                        it.put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                        contentResolver.update(fromUri, it, null, null)
                        it.clear()
                        //updating file details
                        it.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "test1${c}")
                        it.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                        contentResolver.update(fromUri, it, null, null)
                    }
                    catch (e: SecurityException){
                        Log.d("err", "err ===========================t: ${e} ")
                        val intentSender = when{
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->{
                                MediaStore.createWriteRequest(contentResolver, listOf(fromUri))
                            }else -> null
                        }

                        intentSender?.let { sender ->
                            intentSenderLauncher.launch(
                                IntentSenderRequest.Builder(sender).build()
                            )
                        }
                    }
                }

//                var mUri = ContentUris.withAppendedId(fu, id)
//                var contentValues = ContentValues()
////                contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1)
//                contentValues.put(
//                    MediaStore.Files.FileColumns.DISPLAY_NAME,
//                    "xx${returnCursor.getString(nameIndex)}"
//                )
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    contentResolver.update(mUri, contentValues, null);
//                }

                Log.d("TAG", "path ===========================t: $fromUri ")
                Log.d(
                    "TAG", "nameIndex ===========================t: ${
                        returnCursor.getString(nameIndex)
                    } "
                )
                Log.d(
                    "TAG", "sizeIndex ===========================t: ${
                        returnCursor.getLong(sizeIndex)
                    } "
                )
            }
        }
    }

    private fun choose() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type =
            "*/*" //allows any image file type. Change * to specific extension to limit it

//**These following line is the important one!
//**These following line is the important one!
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        resultLauncher.launch(intent)
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURES)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {
                Log.d("TAG", "result ok--- : ${result.data!!}")
                if (result.data!!.clipData != null) { // for multiple file selected
                    val count = result.data!!.clipData!!.itemCount
                    var currentItem = 0

                    while (currentItem < count) {
                        list.add(result.data!!.clipData!!.getItemAt(currentItem).uri)
//                        val v=result.data!!.clipData!!.getItemAt(currentItem).uri.getName(this@MainActivity)
//                        result.data!!.clipData!!.getItemAt(currentItem).uri.let { fileUri ->
//                            //do something with the image (save it to some directory or whatever you need to do with it here)
////                        list.add(fileUri)
//                            contentResolver.query(fileUri, null, null, null, null)
//                        }?.use { returnCursor ->
//                            val nameIndex =
//                                returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                            val sizeIndex = returnCursor!!.getColumnIndex(OpenableColumns.SIZE)
//
//                            returnCursor.moveToFirst()
//
//                            Log.d(
//                                "TAG", "nameIndex ===========================t: ${
//                                    returnCursor.getString(nameIndex)
//                                } "
//                            )
////                            Log.d("TAG", "nameIndex ===========================t: $v ")
//                        }
                        currentItem = currentItem + 1
                    }

                    Log.d("TAG", "onActivityResult: " + list.size)
                } else if (result.data!!.data != null) { // for single file selected
                    val imagePath = result.data!!.data!!
                    list.add(imagePath)
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                }
            }
            Toast.makeText(
                this@MainActivity,
                "${list.size} Files selected",
                Toast.LENGTH_SHORT
            ).show()

        }

    //it is also working
    fun Uri.getName(context: Context): String {
        val returnCursor = context.contentResolver.query(this, null, null, null, null)
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor!!.moveToFirst()
        val fileName = returnCursor.getString(nameIndex)
        returnCursor!!.close()
        return fileName
    }

    //============ deprecated ============
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Log.d("TAG", "result ok ===========================t: ")
//        if (requestCode == SELECT_PICTURES) {
//            if (resultCode == RESULT_OK) {
//                Log.d("TAG", "result ok ===========================t: ")
//                if (data!!.clipData != null) {
//                    val count = data!!.clipData!!.itemCount
//                    var currentItem = 0
//                    val list: ArrayList<Uri> = ArrayList()
//                    while (currentItem < count) {
//                        val fileUri: Uri = data.clipData!!.getItemAt(currentItem).uri
//                        //do something with the image (save it to some directory or whatever you need to do with it here)
//                        list.add(fileUri)
//                        currentItem = currentItem + 1
//                    }
//                    Toast.makeText(this@MainActivity, "${list.size}", Toast.LENGTH_SHORT).show()
//                    Log.d("TAG", "onActivityResult: " + list.size)
//                } else if (data!!.data != null) {
//                    val imagePath = data.data!!.path
//                    //do something with the image (save it to some directory or whatever you need to do with it here)
//                }
//            }
//        }
//    }
}