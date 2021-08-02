package com.chrislaforetsoftware.flasher

import android.content.Intent
import android.net.Uri
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
import com.chrislaforetsoftware.flasher.pickers.FilePicker
import com.chrislaforetsoftware.flasher.pickers.IDeckPickerListener
import com.chrislaforetsoftware.flasher.serializers.DeckSerializer
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class ImportActivity() : AppCompatActivity(), IDeckPickerListener {

    private lateinit var fileToImport: TextView
    private lateinit var destinationDeck: TextView
    private lateinit var overwriteExistingCheckbox: CheckBox
    private lateinit var includeFlaggingCheckbox: CheckBox
    private lateinit var includeStatisticsCheckbox: CheckBox
    private lateinit var importButton: Button

    private var isCreateNewDeck = false
    private var sourceFileToImport: Uri? = null

    private val SOURCE_FILE_SELECTED = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        val actionBar = supportActionBar
        actionBar!!.title = getString(R.string.import_deck)
        actionBar.setDisplayHomeAsUpEnabled(true)

        fileToImport = this.findViewById(R.id.fileToImport)
        destinationDeck = this.findViewById(R.id.destinationDeck)
        overwriteExistingCheckbox = this.findViewById(R.id.overwriteExistingCheckbox)
        includeFlaggingCheckbox = this.findViewById(R.id.includeFlaggingCheckbox)
        includeStatisticsCheckbox = this.findViewById(R.id.includeStatisticsCheckbox)
        importButton = this.findViewById(R.id.importButton)
        importButton.isEnabled = false
    }

    private fun getDatabase(): DatabaseHelper {
        return DatabaseHelper(this)
    }

    fun selectFileToImportClick(view: View) {
// TODO: remove FileSelection xml and class
//        val intent = Intent(this, FileSelection::class.java);
//        startActivityForResult(intent, 1)

        val intent = Intent(this, FilePicker::class.java)
        startActivityForResult(intent, SOURCE_FILE_SELECTED)
    }

    fun selectDeckClick(view: View) {
        val picker = DeckPicker(this, getDatabase(), getString(R.string.destination_deck_prompt), true, this)
        picker.selectDeck()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == RESULT_OK && requestCode == SOURCE_FILE_SELECTED) {
            sourceFileToImport = resultData!!.data
            fileToImport.text = sourceFileToImport!!.lastPathSegment
            checkActivationForImportButton()
        }
    }

    override fun onCreateDeckPicked() {
        destinationDeck.text = getString(R.string.create_new_deck)
        isCreateNewDeck = true
        checkActivationForImportButton()
    }

    override fun onDeckPicked(deckName: String) {
        destinationDeck.text = deckName
        isCreateNewDeck = false
        checkActivationForImportButton()
    }

    private fun checkActivationForImportButton() {
        if (fileToImport.text != getString(R.string.file_prompt) &&
                destinationDeck.text != getString(R.string.deck_prompt)) {
            importButton.isEnabled = true
        }
    }

    fun importDeckClick(view: View) {
        try {
            val filename: String = fileToImport.text as String
            var targetDeck: Deck? = null
            if (isCreateNewDeck) {
                targetDeck = Deck()
                targetDeck.name = filename.replace(".json", "") + "." + getDatabase().getDecks().size
                getDatabase().createDeck(targetDeck)
            } else {
                val deckName: String = destinationDeck.text as String
                targetDeck = getDatabase().getDeckByName(deckName)
            }

            if (targetDeck == null) {
                Toast.makeText(this, "Cannot get or create deck for import", Toast.LENGTH_LONG).show()
                return
            }

            val inputFile = File(sourceFileToImport!!.toString())
            val inputFileStream: FileInputStream = FileInputStream(inputFile)
            val inputReader = InputStreamReader(inputFileStream)
            val serializedContent: String = inputReader.readText()
            inputFileStream.close()

            importFileToDeck(serializedContent, targetDeck)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun importFileToDeck(serializedContent: String, targetDeck: Deck) {
        val cardFaces: MutableMap<String, Int> = mutableMapOf()
        getDatabase().getCardsByDeckId(targetDeck.id).forEach { it -> cardFaces[it.face] = it.id }

        val deckWithCards = DeckSerializer.deserializeDeck(serializedContent)
        for (card in deckWithCards.cards) {
            if (cardFaces.containsKey(card.face)) {
                if (overwriteExistingCheckbox.isChecked) {
                    val cardId: Int = cardFaces[card.face] as Int
                    val existingCard: Card? = getDatabase().getCardById(cardId)

                    if (existingCard == null) {
                        saveNewCardAndAddToLookup(card, targetDeck, cardFaces)
                    } else {
                        saveNewDetailsToExistingCard(existingCard, card)
                    }
                }
            } else {
                saveNewCardAndAddToLookup(card, targetDeck, cardFaces)
            }
        }

        Toast.makeText(baseContext,
                "Deck ${targetDeck.name} has been imported from file!",
                Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveNewCardAndAddToLookup(card: Card, targetDeck: Deck, cardFaces: MutableMap<String, Int>) {
        card.deckId = targetDeck.id
        getDatabase().createCard(card)
        cardFaces[card.face] = card.id
    }

    private fun saveNewDetailsToExistingCard(existingCard: Card, importedCard: Card) {
        existingCard.reverse = importedCard.reverse
        if (includeFlaggingCheckbox.isChecked) {
            existingCard.flagged = importedCard.flagged
        }
        if (includeStatisticsCheckbox.isChecked) {
            existingCard.correct = importedCard.correct
            existingCard.quizzes = importedCard.quizzes
            existingCard.misses = importedCard.misses
        }
        getDatabase().updateCard(existingCard)
    }
}