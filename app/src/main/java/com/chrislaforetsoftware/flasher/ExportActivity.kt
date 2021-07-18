package com.chrislaforetsoftware.flasher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.serializers.DeckSerializer
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class ExportActivity() : AppCompatActivity() {

	private lateinit var sourceDeck: TextView
	private lateinit var includeFlaggingCheckbox: CheckBox
	private lateinit var includeStatisticsCheckbox: CheckBox

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_export)

		val actionBar = supportActionBar
		actionBar!!.title = "Export deck"
		actionBar.setDisplayHomeAsUpEnabled(true)

		sourceDeck = this.findViewById(R.id.sourceDeck)
		includeFlaggingCheckbox = this.findViewById(R.id.includeFlaggingCheckbox)
		includeStatisticsCheckbox = this.findViewById(R.id.includeStatisticsCheckbox)
	}

	private fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}

	fun selectDeckClick(view: View) {

	}

	private fun getSelectedDeck(): Deck {
// FOR NOW - Just snag the first item in the list of decks
		val decks = this.getDatabase().getDecks();
		if (decks.isEmpty()) {
			throw Exception("A deck has not been selected")
		}
		return decks[0]
	}

	fun exportDeckClick(view: View) {
		try {
			val deck = getSelectedDeck()
			exportDeckToFile(deck, this.getDatabase().getCardsByDeckId(deck.id))
		} catch (e: Exception) {
			Toast.makeText(baseContext,
					e.message,
					Toast.LENGTH_SHORT).show()
		}
	}

	private fun exportDeckToFile(deck: Deck, cards: List<Card>) {
		val filePathName = "Flasher.${deck.id}.json"
		try {
			val outputFile: FileOutputStream = openFileOutput(filePathName, MODE_PRIVATE)
			val outputWriter = OutputStreamWriter(outputFile)

			val serialized = DeckSerializer.serializeDeck(deck, cards)
			outputWriter.write(serialized)

			outputWriter.close()
			Toast.makeText(baseContext,
					"File exported successfully!",
					Toast.LENGTH_SHORT).show()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}