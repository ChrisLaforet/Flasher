package com.chrislaforetsoftware.flasher.pickers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.LinearLayout
import com.chrislaforetsoftware.flasher.R
import com.chrislaforetsoftware.flasher.db.DatabaseHelper


interface IExportFolderPickerListener {
	fun onExportFolderPicked(exportFolderName: String)
}

class ExportFolderPicker(private val context: Context,
				 private val title: String,
				 private val listener: IExportFolderPickerListener) {

	fun selectExportFolder() {
		val exportFolderSelectionBox = AlertDialog.Builder(context)
		exportFolderSelectionBox.setTitle(title)

		val layout: LinearLayout = LinearLayout(exportFolderSelectionBox.context)
		layout.layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT)
		layout.orientation = LinearLayout.VERTICAL

		var names = mutableListOf<String>()
		names.add("Backups")
		names.add("Documents")
		names.add("Download")
		names.add("Temp")
		var selectedItem: Int = 0
		exportFolderSelectionBox.setSingleChoiceItems(names.toTypedArray(), 0) { dialogInterface: DialogInterface, item: Int -> selectedItem = item }

		exportFolderSelectionBox.setPositiveButton(context.getString(R.string.OK)) { dialog, which ->
			listener.onExportFolderPicked(names[selectedItem])
		}
		exportFolderSelectionBox.setNegativeButton(context.getString(R.string.CANCEL)) { dialog, which -> dialog.cancel() }
		exportFolderSelectionBox.show()
	}
}