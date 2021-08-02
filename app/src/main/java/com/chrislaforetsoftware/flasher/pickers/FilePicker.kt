package com.chrislaforetsoftware.flasher.pickers

import android.Manifest
import android.app.Activity;
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.chrislaforetsoftware.flasher.R
import java.io.File
import java.util.*

class FilePicker(): AppCompatActivity() {

    private lateinit var folderList: ListView
    private lateinit var filesList: ListView
    private val stack: Deque<File> = ArrayDeque<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_picker)

        val actionBar = supportActionBar
        actionBar!!.title = this.getString(R.string.choose_file_prompt)
        actionBar.setDisplayHomeAsUpEnabled(true)

        if (!setupPermissions()) {
            Toast.makeText(baseContext,
                    "Not permitted to read files",
                    Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        folderList = this.findViewById(R.id.folderList)
        filesList = this.findViewById(R.id.filesList)

//        val roots = File.listRoots()
//        showCurrentPath(roots[0])
//        showCurrentPath(File("/sdcard"))
        showCurrentPath(Environment.getExternalStorageDirectory())
//        showCurrentPath(File("/sdcard"))
    }

    private fun setupPermissions(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun showCurrentPath(file: File)
    {
        try {
            val files: MutableList<File> = mutableListOf()
            val folders: MutableList<File> = mutableListOf()
            val canRead = file.canRead()
            val fileList = file.listFiles()
            fileList?.forEach {
                if (it.isDirectory) {
                    folders.add(it)
                } else if (it.isFile) {
                    files.add(it)
                }
            }

            folders.sortBy { it.name }
            files.sortBy { it.name }

            showFolders(folders)
            showFiles(files)

        } catch (ex: Exception) {
            Toast.makeText(baseContext,
                    ex.message,
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFolders(folders: List<File>) {
        val folderCount = if (stack.isEmpty()) folders.size else folders.size + 1
        val listItems = arrayOfNulls<String>(folderCount)
        var offset = 0
        if (stack.isNotEmpty()) {
            listItems[offset++] = "..  (" + this.getString(R.string.go_back) + ")"
        }
        folders.forEach {
            listItems[offset++] = it.name
        }

        val adapter: ArrayAdapter<String?> = ArrayAdapter(
                this.applicationContext,
                R.layout.file_listview,
                listItems
        )
        folderList.adapter = adapter
    }

    private fun showFiles(files: List<File>) {
        val listItems = arrayOfNulls<String>(files.size)
        var offset = 0
         files.forEach {
            listItems[offset++] = it.name
        }

        val adapter: ArrayAdapter<String?> = ArrayAdapter(
                this.applicationContext,
                R.layout.file_listview,
                listItems
        )
        filesList.adapter = adapter
    }
}
