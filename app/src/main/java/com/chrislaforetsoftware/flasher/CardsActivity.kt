package com.chrislaforetsoftware.flasher

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.view.*
import android.widget.*
import com.chrislaforetsoftware.flasher.adapters.CardListArrayAdapter
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.types.StringDate
import java.util.*


class CardsActivity() : AppCompatActivity() {

	private lateinit var deck: Deck
	private lateinit var activeFilterNotification: TextView

	var showFace = true
	var sortAscending = true
	var filterOn: String = ""

	private lateinit var clearFlagImage: Drawable
	private lateinit var redFlagImage: Drawable

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_cards)

		val extras = intent.extras
		this.deck = extras!!.getSerializable(DECK_EXTRA) as Deck

		val actionBar = supportActionBar
		actionBar!!.title = deck.name
		actionBar.subtitle = getString(R.string.activity_title_flashcard_list)
		activeFilterNotification = this.findViewById(R.id.active_filter_notification)
		activeFilterNotification.visibility = View.GONE
		actionBar.setDisplayHomeAsUpEnabled(true)

		clearFlagImage = getDrawable(R.drawable.clearflag) as Drawable
		redFlagImage = getDrawable(R.drawable.redflag) as Drawable
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater: MenuInflater = menuInflater
		inflater.inflate(R.menu.menu_cards, menu)
		return true
	}

	override fun onResume() {
		super.onResume()
		showCards()
	}

	private fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// Handle item selection
		return when (item.itemId) {
			R.id.flip_cards -> {
				showFace = !showFace
				showCards()
				true
			}
			R.id.sort_order -> {
				sortAscending = !sortAscending
				showCards()
				true
			}
			R.id.filter_card -> {
				promptForFilter()
				true
			}
			R.id.start_quiz -> {
				promptForQuiz()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun promptForFilter() {
		val filterFor: EditText = EditText(this)
		filterFor.isSingleLine = true
		AlertDialog.Builder(this)
				.setTitle(getString(R.string.filter_prompt_title))
				.setView(filterFor)
				.setPositiveButton(
					getString(R.string.filter_button_text),
					DialogInterface.OnClickListener { dialog, whichButton ->
						if (filterFor.text.isNotEmpty()) {
							filterOn = filterFor.text.toString()
						}
						showCards()
					})
				.setNegativeButton(
					getString(R.string.clear_button_title),
					DialogInterface.OnClickListener { dialog, whichButton ->
						filterOn = ""
						showCards()
					}).show()
	}

	private fun promptForQuiz() {
		val choices = arrayOf(
			"Learning language",
			"My native language",
			"Flagged cards only",
			"New cards only",
			"Failed cards only"
		)
		val checkedChoices = booleanArrayOf(true, false, false, false, false)
		AlertDialog.Builder(this)
				.setTitle(getString(R.string.start_quiz_prompt))
				.setMultiChoiceItems(choices, checkedChoices) { dialog, which, isChecked ->
					checkedChoices[which] = isChecked
				}
				.setPositiveButton(
					getString(R.string.start_quiz_button),
					DialogInterface.OnClickListener { dialog, whichButton ->
						startQuiz(
							checkedChoices[0],
							checkedChoices[1],
							checkedChoices[2],
							checkedChoices[3],
							checkedChoices[4]
						)
					})
				.setNegativeButton(
					getString(R.string.clear_button_title),
					DialogInterface.OnClickListener { dialog, whichButton ->
					}).show()
	}

	private fun startQuiz(
		isLearning: Boolean,
		isNative: Boolean,
		isFlaggedOnly: Boolean,
		isNewOnly: Boolean,
		isFailedOnly: Boolean
	) {
		val intent = Intent(this, QuizActivity::class.java)
		intent.putExtra(DECK_EXTRA, deck)
		if (isLearning && isNative) {
			intent.putExtra(QUIZ_LANGUAGES_EXTRA, IS_BOTH_LANGUAGES)
		} else if (isNative) {
			intent.putExtra(QUIZ_LANGUAGES_EXTRA, IS_NATIVE_LANGUAGE)
		} else {
			intent.putExtra(QUIZ_LANGUAGES_EXTRA, IS_LEARNING_LANGUAGE)
		}
		when {
			isFlaggedOnly -> {
				intent.putExtra(QUIZ_LIMIT_EXTRA, IS_FLAGGED_ONLY)
			}
			isNewOnly -> {
				intent.putExtra(QUIZ_LIMIT_EXTRA, IS_NEW_ONLY)
			}
			isFailedOnly -> {
				intent.putExtra(QUIZ_LIMIT_EXTRA, IS_FAILED_ONLY)
			}
		}
		startActivity(intent)
	}

	private fun showCards() {
		var cardList: List<Card> = sortCards(this.getDatabase().getCardsByDeckId(deck.id))
		if (filterOn.isNotEmpty()) {
			activeFilterNotification.visibility = View.VISIBLE
			activeFilterNotification.text = "Filtering cards on: ${ filterOn }"
			cardList = cardList.filter {
				val lowercaseFace = it.face.toLowerCase()
				val lowercaseReverse = it.reverse.toLowerCase()
				val lowercaseFilterOn = filterOn.toLowerCase()
				lowercaseFace.indexOf(lowercaseFilterOn) >= 0 || lowercaseReverse.indexOf(
					lowercaseFilterOn
				) >= 0
			}
		} else {
			activeFilterNotification.visibility = View.GONE
		}

		val listView: ListView? = this.findViewById(R.id.card_list)
		if (listView != null) {
			val adapter = CardListArrayAdapter(
				this,
				R.layout.card_listview,
				showFace,
				cardList
			)
			listView.adapter = adapter
		}
	}

	private fun sortCards(cards: List<Card>): List<Card> {
		if (sortAscending) {
			return cards.sortedBy { if (showFace) it.getFaceForSort() else it.getReverseForSort() }
		}
		return cards.sortedByDescending { if (showFace) it.getFaceForSort() else it.getReverseForSort() }
	}

	fun onClickCreateCardActionButton(view: View) {
		val card = Card()
		val now = StringDate(Date())
		card.created = now.representation
		card.deckId = deck.id

		editCardContent(view, card)
	}

	fun onClickCheckFlagged(view: View) {
		val button = view as ImageButton
		val card: Card = view.getTag(R.id.TAG_CARD) as Card
		card.flagged = !card.flagged
		button.setImageDrawable(if (card.flagged) redFlagImage else clearFlagImage)

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
		val createTitle = getString(R.string.title_create)
		val editTitle = getString(R.string.title_edit)
		val title = (if (isNew) createTitle else editTitle) + " " + getString(R.string.title_flashcard)

		val dialog = Dialog(view.context)
		dialog.setContentView(R.layout.edit_cardview)
		dialog.setTitle(title)

		val windowParams = WindowManager.LayoutParams()
		windowParams.copyFrom(dialog.window?.attributes)
		windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
		windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT

		val cardFront: EditText = dialog.findViewById(R.id.card_front)
		val cardReverse: EditText = dialog.findViewById(R.id.card_reverse)
		val okButton: Button = dialog.findViewById(R.id.ok_button_prompt)
		val cancelButton: Button = dialog.findViewById(R.id.cancel_button_prompt)

		if (!isNew) {
			cardFront.setText(card.face as String)
			cardReverse.setText(card.reverse as String)
		}

		okButton.setOnClickListener {
			val front:String = cardFront.text.toString().trim()
			val reverse: String = cardReverse.text.toString().trim()
			if (front.isEmpty() && reverse.isEmpty()) {
				Toast.makeText(baseContext,
						getString(R.string.cannot_save_empty_card),
						Toast.LENGTH_SHORT).show()
			} else if (front.isEmpty()) {
				Toast.makeText(baseContext,
						getString(R.string.cannot_save_blank_learning_language),
						Toast.LENGTH_SHORT).show()
			} else if (reverse.isEmpty()) {
				Toast.makeText(baseContext,
						getString(R.string.cannot_save_blank_native_language),
						Toast.LENGTH_SHORT).show()
			} else {
				saveCard(card, front, reverse)
				dialog.dismiss()
			}
		}
		cancelButton.setOnClickListener {
			dialog.cancel()
		}

		dialog.show()
		dialog.window?.attributes = windowParams
	}

	private fun saveCard(card: Card, front: String, reverse: String) {
		val isNew = card.id == 0

		card.face = front
		card.reverse = reverse

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
			messageBox.setTitle(getString(R.string.alert_title_error_saving_card))
			messageBox.setMessage("Error saving $front. Is it a duplicate value?")
			messageBox.setCancelable(false)
			messageBox.setNeutralButton(getString(R.string.OK), null)
			messageBox.show()
		}
	}

	fun onClickButtonDelete(view: View) {
		val card: Card = view.getTag(R.id.TAG_CARD) as Card

		val messageBox = AlertDialog.Builder(this)
		messageBox.setTitle(getString(R.string.alert_title_confirm_delete))
		messageBox.setMessage("Are you certain you want to delete card for '${card.face}'")
		messageBox.setPositiveButton(getString(R.string.DELETE)) { dialog, which ->
			run {
				deleteCard(card)
			}
		}
		messageBox.setNegativeButton(getString(R.string.CANCEL)) { dialog, which -> dialog.cancel() }
		messageBox.show()
	}

	private fun deleteCard(card: Card) {
		val db = DatabaseHelper(this)
		try {
			db.deleteCard(card)
			showCards()
		} catch (ee: Exception) {
			val messageBox = AlertDialog.Builder(this)
			messageBox.setTitle(getString(R.string.alert_title_error_deleting))
			messageBox.setMessage("Error while deleting ${card.face}.")
			messageBox.setCancelable(false)
			messageBox.setNeutralButton(getString(R.string.OK), null)
			messageBox.show()
		}
	}
}