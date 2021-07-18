package com.chrislaforetsoftware.flasher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import com.chrislaforetsoftware.flasher.adapters.CardListArrayAdapter
import com.chrislaforetsoftware.flasher.entities.Card

class FileSelection : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_file_selection)

		val actionBar = supportActionBar
		actionBar!!.title = "Select file"
		actionBar.setDisplayHomeAsUpEnabled(true)
	}

	override fun onResume() {
		super.onResume()
		showFiles()
	}

	private fun showFiles() {
		// TODO someday permit selection of files from anywhere

		val files = mutableListOf<String>()
		for (file in fileList()) {
			if (file.endsWith(".json")) {
				files.add(file)
			}
		}

		val listView: ListView? = this.findViewById(R.id.card_list)
		if (listView != null) {
			val adapter: ArrayAdapter<String> = ArrayAdapter(
					this.applicationContext,
					R.layout.deck_listview,
					files
			)

			listView.adapter = adapter
		}
	}
}