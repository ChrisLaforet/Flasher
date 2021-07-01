package com.chrislaforetsoftware.flasher

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.chrislaforetsoftware.flasher.db.DatabaseHelper

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
	}

	fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}
}