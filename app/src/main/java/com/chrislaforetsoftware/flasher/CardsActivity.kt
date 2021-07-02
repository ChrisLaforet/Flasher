package com.chrislaforetsoftware.flasher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Deck

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

	fun createNewCard(view: View) {
		// TODO: handle new card here
	}
}