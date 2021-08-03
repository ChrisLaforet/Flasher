package com.chrislaforetsoftware.flasher.pickers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.LinearLayout
import com.chrislaforetsoftware.flasher.R
import com.chrislaforetsoftware.flasher.db.DatabaseHelper


interface IDeckPickerListener {
	fun onCreateDeckPicked()
	fun onDeckPicked(deckName: String)
}

class DeckPicker(private val context: Context,
				 private val database: DatabaseHelper,
				 private val title: String,
				 private val createDeckOptionAllowed: Boolean,
				 private val listener: IDeckPickerListener) {

	fun selectDeck() {
		val deckSelectionBox = AlertDialog.Builder(context)
		deckSelectionBox.setTitle(title)

		val layout: LinearLayout = LinearLayout(deckSelectionBox.context)
		layout.layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT)
		layout.orientation = LinearLayout.VERTICAL

		database.getDecks()
		var names = mutableListOf<String>()
		if (createDeckOptionAllowed) {
			names.add(context.getString(R.string.create_new_deck))
		}
		names.addAll(database.getDeckNames().sorted())
		var selectedItem: Int = 0
		deckSelectionBox.setSingleChoiceItems(names.toTypedArray(), 0) { dialogInterface: DialogInterface, item: Int -> selectedItem = item }

		deckSelectionBox.setPositiveButton(context.getString(R.string.OK)) { dialog, which ->
			if (selectedItem == 0 && createDeckOptionAllowed) {
				listener.onCreateDeckPicked()
			} else {
				listener.onDeckPicked(names[selectedItem])
			}
		}
		deckSelectionBox.setNegativeButton(context.getString(R.string.CANCEL)) { dialog, which -> dialog.cancel()
		}
		deckSelectionBox.show()
	}
}