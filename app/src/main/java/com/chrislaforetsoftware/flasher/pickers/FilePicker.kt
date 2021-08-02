package com.chrislaforetsoftware.flasher.pickers

import android.Manifest
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

private data class PathContainer(var basePath: File, var newPath: File) {}

class FilePicker(): AppCompatActivity() {

    private lateinit var folderList: ListView
    private lateinit var filesList: ListView

    private val stack: Deque<PathContainer> = ArrayDeque<PathContainer>()
    private lateinit var rootPath: File
    private var currentPath: String = File.separator

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
        folderList.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) 	// The item that was clicked
            val folderName = element.toString()
            var folder: File? = null
            if (folderName.startsWith(".. ")) {
                val previousPath = stack.pop()
                folder = previousPath.basePath
                currentPath = getCurrentPath(true)
                if (currentPath.isEmpty()) {
                    currentPath = File.separator
                }
            } else {
                folder = File(createNewPathFromCurrentPath(folderName))
                stack.push(PathContainer(File(getCurrentPath()), folder))
                currentPath = createNewPathFromCurrentPath(folderName, true)
            }
            showCurrentPath(folder ?: rootPath)
        }
        filesList = this.findViewById(R.id.filesList)

        rootPath = Environment.getExternalStorageDirectory()
        showCurrentPath(rootPath)
    }

    private fun getCurrentPath(excludeRootPath: Boolean = false): String {
        val builder: StringBuilder = StringBuilder()
        if (!excludeRootPath) {
            builder.append(rootPath.absolutePath)
        }

        stack.toList().forEach {
            if (it.basePath != rootPath) {
                builder.append(File.separator)
                builder.append(it.basePath.name)
            }
        }

        return builder.toString()
    }

    private fun createNewPathFromCurrentPath(filename: String, excludeRootPath: Boolean = false): String {
        val builder: StringBuilder = StringBuilder()
        builder.append(getCurrentPath(excludeRootPath))
        builder.append(File.separator)
        builder.append(filename)
        return builder.toString()
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
            showCurrentPath()
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

    private fun showCurrentPath() {
        val ab = supportActionBar
        ab!!.subtitle = currentPath
    }
}
