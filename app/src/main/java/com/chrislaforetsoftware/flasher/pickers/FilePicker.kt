package com.chrislaforetsoftware.flasher.pickers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.chrislaforetsoftware.flasher.R
import java.io.File
import java.util.*

private data class PathHolder(var parentPath: File?, var currentPath: File) {}

class FilePicker(): AppCompatActivity() {

    private lateinit var folderList: ListView
    private lateinit var filesList: ListView

    private val pathStack: Deque<PathHolder> = ArrayDeque<PathHolder>()
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
                    getString(R.string.not_permitted_to_read_files),
                    Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        folderList = this.findViewById(R.id.folderList)
        filesList = this.findViewById(R.id.filesList)

        folderList.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) 	// The item that was clicked
            val folderName = element.toString()
            var folder: File? = null
            if (folderName.startsWith(".. ")) {
                val previousPath = pathStack.pop()
                folder = previousPath.parentPath ?: rootPath
            } else {
                folder = File(createNewPathFromCurrentPath(folderName))
                pathStack.push(PathHolder(File(getCurrentPath()), folder))
            }
            currentPath = getCurrentDisplayPath()
            showCurrentPath(folder)
        }

        filesList.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) 	// The item that was clicked
            val filename = element.toString()

            val result = Intent()
            result.data = Uri.parse(getCurrentPath(false) + File.separator + filename)
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        rootPath = Environment.getExternalStorageDirectory()
        pathStack.push(PathHolder(null, rootPath))
        showCurrentPath(rootPath)
    }

    private fun getCurrentDisplayPath(): String {
        val displayPath = getCurrentPath(true)
        if (displayPath.isEmpty()) {
            return File.separator
        }
        return displayPath
    }

    private fun getCurrentPath(excludeRootPath: Boolean = false): String {
        val builder: StringBuilder = StringBuilder()

        val paths = pathStack.toList()
        paths.reversed().forEach {
            if (it.parentPath != null) {
                builder.append(File.separator)
                builder.append(it.currentPath.name)
            } else if (!excludeRootPath) {
                builder.append(rootPath.absolutePath)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    !Environment.isExternalStorageManager()) {
            return false
        }
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
        val showGoBackOption = pathStack.size > 1
        val folderCount = if (showGoBackOption) folders.size + 1 else folders.size
        val listItems = arrayOfNulls<String>(folderCount)
        var offset = 0
        if (showGoBackOption) {
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
