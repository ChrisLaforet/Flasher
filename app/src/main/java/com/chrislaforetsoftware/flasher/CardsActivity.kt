package com.chrislaforetsoftware.flasher

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.types.StringDate
import java.util.*

class CardsActivity() : AppCompatActivity() {

	lateinit var deck: Deck;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val extras = intent.extras
		this.deck = extras!!.getSerializable("deck") as Deck

		val actionBar = supportActionBar
		actionBar!!.title = deck.name
		actionBar.subtitle = "Flashcard List"
		actionBar.setDisplayHomeAsUpEnabled(true)

		// TODO: add sort and search icons on action bar
	}

	fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}

	fun showCards() {
		// TODO flesh in the show cards/sorted and so on
	}

	fun onClickCreateActionButton(view: View) {
		val card = Card()
		val now = StringDate(Date())
		card.created = now.representation
		card.deckId = deck.id

		editCardContent(view, card)
	}

	private fun editCardContent(view: View, card: Card) {
		val isNew = card.id == 0
		val cardPromptBox = AlertDialog.Builder(view.context)
		cardPromptBox.setTitle((if (isNew) "Create" else "Edit") + "-Flashcard")

		val layout: LinearLayout = LinearLayout(cardPromptBox.context)
		layout.setLayoutParams(
			LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
		)
		layout.setOrientation(LinearLayout.VERTICAL)

		val faceText = EditText(cardPromptBox.context)
		faceText.inputType = InputType.TYPE_CLASS_TEXT
		faceText.setSingleLine()
		layout.addView(faceText)

		val reverseText = EditText(cardPromptBox.context)
		reverseText.inputType = InputType.TYPE_CLASS_TEXT
		layout.addView(reverseText)

		cardPromptBox.setView(layout)

		cardPromptBox.setPositiveButton("OK") { dialog, which ->
			run {
				val face = faceText.getText().toString()
				if (face.isNotEmpty()) {
					card.face = face
					card.reverse = reverseText.getText().toString()

					val db = DatabaseHelper(this)

					// TODO prevent duplicate face value


					// TODO update deck last use


					try {
						if (isNew) {
							db.createCard(card)
						} else {
							db.updateCard(card)
						}
						showCards()
					} catch (ee: Exception) {
						val messageBox = AlertDialog.Builder(this)
						messageBox.setTitle("Error while saving card")
						messageBox.setMessage("Error saving $face. Is it a duplicate value?")
						messageBox.setCancelable(false)
						messageBox.setNeutralButton("OK", null)
						messageBox.show()
					}
				}
			}
		}

		cardPromptBox.setNegativeButton("Cancel") { dialog, which -> dialog.cancel()
		}

		cardPromptBox.show()
	}
}