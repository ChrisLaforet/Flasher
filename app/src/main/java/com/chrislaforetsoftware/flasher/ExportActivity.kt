package com.chrislaforetsoftware.flasher

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.pickers.DeckPicker
import com.chrislaforetsoftware.flasher.pickers.ExportFolderPicker
import com.chrislaforetsoftware.flasher.pickers.IDeckPickerListener
import com.chrislaforetsoftware.flasher.pickers.IExportFolderPickerListener
import com.chrislaforetsoftware.flasher.serializers.DeckSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class ExportActivity() : AppCompatActivity(), IDeckPickerListener, IExportFolderPickerListener {

	private lateinit var folderForExport: TextView
	private lateinit var sourceDeck: TextView
	private lateinit var includeFlaggingCheckbox: CheckBox
	private lateinit var includeStatisticsCheckbox: CheckBox
	private lateinit var exportButton: Button

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_export)

		val actionBar = supportActionBar
		actionBar!!.title = this.getString(R.string.export_deck)
		actionBar.setDisplayHomeAsUpEnabled(true)

		folderForExport = this.findViewById(R.id.folderForExport)
		sourceDeck = this.findViewById(R.id.sourceDeck)
		includeFlaggingCheckbox = this.findViewById(R.id.includeFlaggingCheckbox)
		includeStatisticsCheckbox = this.findViewById(R.id.includeStatisticsCheckbox)
		exportButton = this.findViewById(R.id.exportButton)
		exportButton.isEnabled = false
	}

	private fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}

	fun selectFileToImportClick(view: View) {
		val picker = ExportFolderPicker(this, getString(R.string.folder_for_export_prompt), this)
		picker.selectExportFolder()
	}

	fun selectDeckClick(view: View) {
		val picker = DeckPicker(this, getDatabase(), getString(R.string.deck_to_export_prompt), false, this)
		picker.selectDeck()
	}

	override fun onCreateDeckPicked() {
		// does nothing - export cannot create a deck to export
	}

	override fun onDeckPicked(deckName: String) {
		sourceDeck.text = deckName
		checkActivationForExportButton()
	}

	override fun onExportFolderPicked(exportFolderName: String) {
		folderForExport.text = exportFolderName
		checkActivationForExportButton()
	}

	private fun checkActivationForExportButton() {
		if (folderForExport.text != getString(R.string.select_folder) &&
				sourceDeck.text != getString(R.string.deck_prompt)) {
			exportButton.isEnabled = true
		}
	}

	fun exportDeckClick(view: View) {
		try {
			val deck: Deck? = this.getDatabase().getDeckByName(sourceDeck.text as String)
			if (deck == null) {
				Toast.makeText(this, "Cannot load deck for ${sourceDeck.text}", Toast.LENGTH_LONG).show()
				return
			}
			exportDeckToFile(deck, this.getDatabase().getCardsByDeckId(deck.id))
		} catch (e: Exception) {
			Toast.makeText(baseContext,
					e.message,
					Toast.LENGTH_SHORT).show()
		}
	}

	private fun getFullExportFile(filename: String): File {
		val folder = File(Environment.getExternalStorageDirectory().getAbsolutePath(), folderForExport.text as String)
		if (!folder.exists()) {
			folder.mkdirs()
		}
		return File(folder, filename)
	}

	private fun exportDeckToFile(deck: Deck, cards: List<Card>) {
		val filename = "Flasher.${deck.id}.${deck.getDeckNameAsFilename()}.json"
		try {
			val file = getFullExportFile(filename)
			file.createNewFile()
			val outputFile = FileOutputStream(file)
			val outputWriter = OutputStreamWriter(outputFile)

			val serialized = DeckSerializer.serializeDeck(deck, cards, includeFlaggingCheckbox.isChecked, includeStatisticsCheckbox.isChecked)
			outputWriter.write(serialized)

			outputWriter.close()
			Toast.makeText(baseContext,
					"File exported successfully to " + filename + "!",
					Toast.LENGTH_SHORT).show()
		} catch (e: Exception) {
			Toast.makeText(baseContext,
					"Error while exporting file (" + e.message + ").",
					Toast.LENGTH_SHORT).show()
			e.printStackTrace()
		}
		finish()
	}
}