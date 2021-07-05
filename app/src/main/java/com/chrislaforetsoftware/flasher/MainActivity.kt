package com.chrislaforetsoftware.flasher

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.types.StringDate
import java.util.*


class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val ab = supportActionBar
		ab!!.subtitle = "Card Decks"

		val listView: ListView? = this.findViewById(R.id.deck_list)
		listView?.setOnItemClickListener { parent, view, position, id ->
			val element = parent.getItemAtPosition(position) 	// The item that was clicked
			val deck = getDatabase().getDeckByName(element.toString())
			val intent = Intent(this, CardsActivity::class.java)
			intent.putExtra("deck", deck)
			startActivity(intent)
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater: MenuInflater = menuInflater
		inflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onResume() {
		super.onResume()
		showDecks()
	}

	private fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}

	private fun showDecks() {
		val deckTitleList: List<String> = this.getDatabase().getDecks()
			.asSequence()
			.map{ d -> d.name}
			.toList()
		val listItems = arrayOfNulls<String>(deckTitleList.size)
		for (index in deckTitleList.indices) {
			listItems[index] = deckTitleList[index]
		}

		val listView: ListView? = this.findViewById(R.id.deck_list)
		if (listView != null) {
			val adapter: ArrayAdapter<String> = ArrayAdapter(
				this.applicationContext,
				R.layout.deck_listview,
				listItems
			)
			listView.adapter = adapter
		}
	}

	fun onClickCreateDeckActionButton(view: View) {
		val namePromptBox = AlertDialog.Builder(view.context)
		namePromptBox.setTitle("Name for Card Deck")

		val editText = EditText(namePromptBox.context)
		editText.inputType = InputType.TYPE_CLASS_TEXT
		editText.setSingleLine()
		editText.setPadding(10, 20, 10, 10)

		namePromptBox.setView(editText)

		namePromptBox.setPositiveButton("OK") { dialog, which ->
			run {
				val title = editText.getText().toString()
				if (title.isNotEmpty()) {
					val db = DatabaseHelper(this)
					val deck = Deck()
					deck.name = title
					deck.created = StringDate(Date()).representation
					deck.lastUse = deck.created

					try {
						db.createDeck(deck)
						showDecks()
					} catch (ee: Exception) {
						val messageBox = AlertDialog.Builder(this)
						messageBox.setTitle("Error while saving deck")
						messageBox.setMessage("Error saving $title. Is it a duplicate name?")
						messageBox.setCancelable(false)
						messageBox.setNeutralButton("OK", null)
						messageBox.show()
					}
				}
			}
		}

		namePromptBox.setNegativeButton("Cancel") { dialog, which -> dialog.cancel()
		}

		namePromptBox.show()
	}
}