package com.chrislaforetsoftware.flasher

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.chrislaforetsoftware.flasher.adapters.CardListArrayAdapter
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.types.StringDate
import java.util.*

class CardsActivity() : AppCompatActivity() {

	lateinit var deck: Deck;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_cards)

		val extras = intent.extras
		this.deck = extras!!.getSerializable("deck") as Deck

		val actionBar = supportActionBar
		actionBar!!.title = deck.name
		actionBar.subtitle = "Flashcard List"
		actionBar.setDisplayHomeAsUpEnabled(true)


		// TODO: add sort and search icons on action bar

	}

	override fun onResume() {
		super.onResume()
		showCards()
	}

	private fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}

	private fun showCards() {
		// TODO flesh in the show cards/sorted and so on
		val cardList: List<Card> = this.getDatabase().getCardsByDeckId(deck.id)

		val listView: ListView? = this.findViewById(R.id.card_list)
		if (listView != null) {
			val adapter = CardListArrayAdapter(
					this,
					R.layout.card_listview,
					cardList
			)
			listView.adapter = adapter
		}
	}

	fun onClickCreateCardActionButton(view: View) {
		val card = Card()
		val now = StringDate(Date())
		card.created = now.representation
		card.deckId = deck.id

		editCardContent(view, card)
	}

	fun onClickCheckFlagged(view: View) {
		val checkBox = view as CheckBox
		val card: Card = view.getTag(R.id.TAG_CARD) as Card
		card.flagged = checkBox.isChecked

		val db = DatabaseHelper(this)
		try {
			db.updateCard(card)
		} catch (ee: Exception) {
			val messageBox = AlertDialog.Builder(this)
			messageBox.setTitle("Error while saving card")
			messageBox.setMessage("Error saving flagged changes to ${card.face}")
			messageBox.setCancelable(false)
			messageBox.setNeutralButton("OK", null)
			messageBox.show()
		}
	}

	fun onClickButtonEdit(view: View) {
		val card: Card = view.getTag(R.id.TAG_CARD) as Card
		editCardContent(view, card)
	}

	private fun editCardContent(view: View, card: Card) {
		val isNew = card.id == 0
		val cardPromptBox = AlertDialog.Builder(view.context)
		val title = (if (isNew) "Create" else "Edit") + " Flashcard"
		cardPromptBox.setTitle(title)

		val layout: LinearLayout = LinearLayout(cardPromptBox.context)
		layout.layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT)
		layout.orientation = LinearLayout.VERTICAL

		val facePrompt = TextView(cardPromptBox.context)
		facePrompt.text = "Face value" + ":"
		facePrompt.setPadding(10, 20, 10, 0)
		layout.addView(facePrompt)

		val faceText = EditText(cardPromptBox.context)
		faceText.inputType = InputType.TYPE_CLASS_TEXT
		faceText.setSingleLine()
		faceText.setPadding(10, 10, 10, 10)
		faceText.setText(card.face)
		layout.addView(faceText)

		val reversePrompt = TextView(cardPromptBox.context)
		reversePrompt.text = "Reverse value" + ":"
		reversePrompt.setPadding(10, 30, 10, 0)
		layout.addView(reversePrompt)

		val reverseText = EditText(cardPromptBox.context)
		reverseText.inputType = InputType.TYPE_CLASS_TEXT
		reverseText.setPadding(5, 10, 5, 10)
		reverseText.setText(card.reverse)
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

	fun onClickButtonDelete(view: View) {
		val card: Card = view.getTag(R.id.TAG_CARD) as Card

		val messageBox = AlertDialog.Builder(this)
		messageBox.setTitle("Confirm delete of card")
		messageBox.setMessage("Are you certain you want to delete card for '${card.face}'")
		messageBox.setPositiveButton("Delete") { dialog, which ->
			run {
				deleteCard(card)
			}
		}
		messageBox.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
		messageBox.show()
	}

	private fun deleteCard(card: Card) {
		val db = DatabaseHelper(this)
		try {
			db.deleteCard(card)
			showCards()
		} catch (ee: Exception) {
			val messageBox = AlertDialog.Builder(this)
			messageBox.setTitle("Error while deleting card")
			messageBox.setMessage("Error while deleting ${card.face}.")
			messageBox.setCancelable(false)
			messageBox.setNeutralButton("OK", null)
			messageBox.show()
		}
	}
}