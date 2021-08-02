package com.chrislaforetsoftware.flasher

import android.os.Bundle
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
import com.chrislaforetsoftware.flasher.pickers.IDeckPickerListener
import com.chrislaforetsoftware.flasher.serializers.DeckSerializer
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class ExportActivity() : AppCompatActivity(), IDeckPickerListener {

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

		sourceDeck = this.findViewById(R.id.sourceDeck)
		includeFlaggingCheckbox = this.findViewById(R.id.includeFlaggingCheckbox)
		includeStatisticsCheckbox = this.findViewById(R.id.includeStatisticsCheckbox)
		exportButton = this.findViewById(R.id.exportButton)
		exportButton.isEnabled = false
	}

	private fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
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
		exportButton.isEnabled = true
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

	private fun exportDeckToFile(deck: Deck, cards: List<Card>) {
		val filePathName = "Flasher.${deck.id}.${deck.getDeckNameAsFilename()}.json"
		try {
			val outputFile: FileOutputStream = openFileOutput(filePathName, MODE_PRIVATE)
			val outputWriter = OutputStreamWriter(outputFile)

			val serialized = DeckSerializer.serializeDeck(deck, cards, includeFlaggingCheckbox.isChecked, includeStatisticsCheckbox.isChecked)
			outputWriter.write(serialized)

			outputWriter.close()
			Toast.makeText(baseContext,
					"File exported successfully!",
					Toast.LENGTH_SHORT).show()
			finish()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}