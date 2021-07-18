package com.chrislaforetsoftware.flasher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.serializers.DeckSerializer
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ImportActivity() : AppCompatActivity() {

    private lateinit var fileToImport: TextView
    private lateinit var destinationDeck: TextView
    private lateinit var overwriteExistingCheckbox: CheckBox
    private lateinit var includeFlaggingCheckbox: CheckBox
    private lateinit var includeStatisticsCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        val actionBar = supportActionBar
        actionBar!!.title = "Import deck"
        actionBar.setDisplayHomeAsUpEnabled(true)

        fileToImport = this.findViewById(R.id.fileToImport)
        destinationDeck = this.findViewById(R.id.destinationDeck)
        overwriteExistingCheckbox = this.findViewById(R.id.overwriteExistingCheckbox)
        includeFlaggingCheckbox = this.findViewById(R.id.includeFlaggingCheckbox)
        includeStatisticsCheckbox = this.findViewById(R.id.includeStatisticsCheckbox)
    }

    private fun getDatabase(): DatabaseHelper {
        return DatabaseHelper(this)
    }


    fun selectFileToImportClick(view: View) {

    }

    fun selectDeckClick(view: View) {

    }

    private fun getSelectedFileToImport(): String {
// FOR NOW - just use hard coded filename
        return "Flasher.1.json"
    }

    private fun getSelectedDeck(): Deck? {
// FOR NOW - create a new deck
return null
//        val decks = this.getDatabase().getDecks();
//        if (decks.isEmpty()) {
//            return null
//        }
//        return decks[0]
    }

    fun importDeckClick(view: View) {
        try {
            val inputFile: FileInputStream = openFileInput(getSelectedFileToImport())
            val inputReader = InputStreamReader(inputFile)
            val serializedContent: String = inputReader.readText()
            inputFile.close()

            importFileToDeck(serializedContent, getSelectedDeck())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun importFileToDeck(serializedContent: String, targetDeck: Deck?) {
        val deckWithCards = DeckSerializer.deserializeDeck(serializedContent)
        if (targetDeck == null) {
// TODO: prevent conflict of name
deckWithCards.deck.name =  deckWithCards.deck.name + "." + getDatabase().getDecks().size
            getDatabase().createDeck(deckWithCards.deck)
        }
        val deck: Deck = targetDeck ?: deckWithCards.deck
        for (card in deckWithCards.cards) {
            //TODO: determine if card already exists and if so just update it!
            card.deckId = deck.id
            getDatabase().createCard(card)
        }

        Toast.makeText(baseContext,
            "Deck ${deck.name} has been imported from file!",
            Toast.LENGTH_SHORT).show()
    }

}