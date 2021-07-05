package com.chrislaforetsoftware.flasher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.chrislaforetsoftware.flasher.entities.Deck

class ExportActivity() : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_export)

		val actionBar = supportActionBar
		actionBar!!.title = "Export deck"
		actionBar.setDisplayHomeAsUpEnabled(true)


	}

	fun selectDeckClick(view: View) {

	}

	fun selectFileClick(view: View) {

	}
}