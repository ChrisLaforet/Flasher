package com.chrislaforetsoftware.flasher

import android.graphics.drawable.Drawable
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {

	private lateinit var deck: Deck;

	private lateinit var cardFront: TextView
	private lateinit var cardReverse: TextView

	private lateinit var cardLanguagePrompt: TextView
	private lateinit var cardGoodPrompt: TextView
	private lateinit var cardFailedPrompt: TextView
	private lateinit var cardFlaggingButton: ImageButton

	private lateinit var quizLanguage: String
	private lateinit var learningLanguagePrompt: String
	private lateinit var nativeLanguagePrompt: String

	private lateinit var clearFlagImage: Drawable
	private lateinit var redFlagImage: Drawable

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_quiz)

		val actionBar = supportActionBar
		actionBar!!.title = getString(R.string.flashcard_quiz_title)
		// actionBar.setDisplayHomeAsUpEnabled(true) 		// TODO - cannot just go back to Cards without setting the DECK extra!!

		val extras = intent.extras
		this.deck = extras!!.getSerializable(DECK_EXTRA) as Deck
		this.quizLanguage = extras.getString(QUIZ_LANGUAGES_EXTRA).toString()
		val quizLimit = extras.getString(QUIZ_LIMIT_EXTRA)

		cardFront = this.findViewById(R.id.card_front)
		cardReverse = this.findViewById(R.id.card_reverse)

		cardLanguagePrompt = this.findViewById(R.id.card_language_prompt)
		cardGoodPrompt = this.findViewById(R.id.card_good_prompt)
		cardFailedPrompt = this.findViewById(R.id.card_failed_prompt)

		learningLanguagePrompt = getString(R.string.learning_language_prompt)
		nativeLanguagePrompt = getString(R.string.native_language_prompt)

		val cards: List<Card> = loadCards(quizLimit)
		if (cards.isEmpty()) {
			Toast.makeText(baseContext,
					getString(R.string.no_cards_for_quiz_message),
					Toast.LENGTH_SHORT).show()
			finish()
			return
		}
		var nextCardIndex = 0
		var card = cards[nextCardIndex++]
		showCard(card)

		val endQuizButton: Button = this.findViewById(R.id.end_quiz_button)
		endQuizButton.setOnClickListener {
			finish()
		}

		clearFlagImage = getDrawable(R.drawable.clearflag) as Drawable
		redFlagImage = getDrawable(R.drawable.redflag) as Drawable
		cardFlaggingButton = this.findViewById(R.id.card_flagging_button)
		cardFlaggingButton.setOnClickListener {
			setFlagging(card)
		}

		val peekButton: ImageButton = this.findViewById(R.id.button_peek)
		peekButton.setOnClickListener {
			cardReverse.visibility = if (cardReverse.visibility == View.INVISIBLE) View.VISIBLE else View.INVISIBLE
		}

		val goodButton: ImageButton = this.findViewById(R.id.button_good)
		goodButton.setOnClickListener {
			markGood(card)
			if (nextCardIndex >= cards.size) {
				finish()
			} else {
				card = cards[nextCardIndex++]
				showCard(card)
			}
		}

		val failButton: ImageButton = this.findViewById(R.id.button_fail)
		failButton.setOnClickListener {
			markFail(card)
			if (nextCardIndex >= cards.size) {
				finish()
			} else {
				card = cards[nextCardIndex++]
				showCard(card)
			}
		}

		val editButton: ImageButton = this.findViewById(R.id.button_edit)
		editButton.setOnClickListener {

		}
	}

	private fun showCard(card: Card) {
		var showLanguage = quizLanguage
		if (quizLanguage == IS_BOTH_LANGUAGES) {
			showLanguage = if (Random.nextBoolean()) IS_LEARNING_LANGUAGE else IS_NATIVE_LANGUAGE
		}
		cardReverse.visibility = View.INVISIBLE
		if (showLanguage == IS_LEARNING_LANGUAGE) {
			cardFront.text = card.face
			cardReverse.text = card.reverse
			cardLanguagePrompt.text = learningLanguagePrompt
		} else {
			cardFront.text = card.reverse
			cardReverse.text = card.face
			cardLanguagePrompt.text = nativeLanguagePrompt
		}
		cardGoodPrompt.text = card.correct.toString()
		cardFailedPrompt.text = card.misses.toString()

		cardFlaggingButton.setImageDrawable(if (card.flagged) redFlagImage else clearFlagImage)
	}

	private fun markGood(card: Card) {
		card.correct++
		getDatabase().updateCard(card)
	}

	private fun setFlagging(card: Card) {
		card.flagged = !card.flagged
		getDatabase().updateCard(card)
		cardFlaggingButton.setImageDrawable(if (card.flagged) redFlagImage else clearFlagImage)
	}

	private fun markFail(card: Card) {
		card.misses++
		getDatabase().updateCard(card)
	}

	private fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}

	private fun loadCards(quizLimit: String?): List<Card> {
		val filteredCards = mutableListOf<Card>()
		getDatabase().getCardsByDeckId(deck.id).forEach {
			if (quizLimit != null) {
				when (quizLimit) {
					IS_FLAGGED_ONLY -> if (it.flagged) filteredCards.add(it)
					IS_NEW_ONLY -> if (it.misses == 0 && it.correct == 0) filteredCards.add(it)
					IS_FAILED_ONLY -> if (it.misses != 0) filteredCards.add(it)
					else -> filteredCards.add(it)
				}
			} else {
				filteredCards.add(it)
			}
		}

		return shuffleCards(filteredCards)
	}

	private fun shuffleCards(cards: List<Card>): List<Card> {
		if (cards.isEmpty()) {
			return cards
		}

		var total: Int = 0
		val shuffler = IntArray(cards.size) { -1 }
		while (total < shuffler.size) {
			val nextValue = Random.nextInt(0, shuffler.size)
			if (shuffler[nextValue] < 0) {
				shuffler[nextValue] = total++;
			}
		}

		val shuffledCards = mutableListOf<Card>()
		shuffler.forEach { shuffledCards.add(cards[it]) }
		return shuffledCards
	}
}