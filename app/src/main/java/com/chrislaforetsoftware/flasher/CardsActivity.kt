package com.chrislaforetsoftware.flasher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.chrislaforetsoftware.flasher.db.DatabaseHelper

class CardsActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val ab = supportActionBar
		ab!!.title = "Name of card deck here"
		ab.subtitle = "Flashcard List"
		ab.setDisplayHomeAsUpEnabled(true)

		// TODO: add sort and search icons on action bar
	}

	fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}
}