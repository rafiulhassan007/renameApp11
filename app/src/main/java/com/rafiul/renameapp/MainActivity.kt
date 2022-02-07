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

        })
        btn2.setOnClickListener(View.OnClickListener {

        })



    }

}