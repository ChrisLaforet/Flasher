package com.chrislaforetsoftware.flasher

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import com.chrislaforetsoftware.flasher.adapters.CardListArrayAdapter
import com.chrislaforetsoftware.flasher.adapters.DeckListArrayAdapter
import com.chrislaforetsoftware.flasher.component.CardEditPopup
import com.chrislaforetsoftware.flasher.component.DeckEditPopup
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.types.StringDate
import java.util.*


class MainActivity : AppCompatActivity(), DeckEditPopup.DeckEditNoticeListener {

	private val manifestStoragePermissions = arrayOf(
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val actionBar = supportActionBar
		actionBar!!.subtitle = getString(R.string.activity_title_card_decks)

		requestExternalStorageManagementPermissions()
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

	private fun requestExternalStorageManagementPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
				!Environment.isExternalStorageManager()) {
			val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

			startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
			if (permission != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, manifestStoragePermissions, REQUEST_EXTERNAL_STORAGE)
			}
		}
	}

	private fun getDatabase(): DatabaseHelper {
		return DatabaseHelper(this)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// Handle item selection
		return when (item.itemId) {
			R.id.export_deck -> {
				val intent = Intent(this, ExportActivity::class.java)
				startActivity(intent)
				true
			}
			R.id.import_deck -> {
				val intent = Intent(this, ImportActivity::class.java)
				startActivity(intent)
				true
			}
			R.id.settings -> {
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun showDecks() {
		val decksList: List<Deck> = this.getDatabase().getDecks()

		val listView: ListView? = this.findViewById(R.id.deck_list)
		if (listView != null) {
			val adapter = DeckListArrayAdapter(
					this,
					R.layout.deck_listview,
					decksList
			)
			listView.adapter = adapter
		}
	}

	fun onClickSelect(view: View) {
		val deck: Deck = view.getTag(R.id.TAG_DECK) as Deck
		val intent = Intent(this, CardsActivity::class.java)
		intent.putExtra(DECK_EXTRA, deck)
		startActivity(intent)
	}

	fun onClickButtonEdit(view: View) {
		val deck: Deck = view.getTag(R.id.TAG_DECK) as Deck
		val editPopup = DeckEditPopup(this)
		editPopup.editDeck(view, deck)
	}

	fun onClickCreateDeckActionButton(view: View) {
		val namePromptBox = AlertDialog.Builder(view.context)
		namePromptBox.setTitle(getString(R.string.title_card_deck_name))

		val editText = EditText(namePromptBox.context)
		editText.inputType = InputType.TYPE_CLASS_TEXT
		editText.setSingleLine()
		editText.setPadding(10, 20, 10, 10)

		namePromptBox.setView(editText)

		namePromptBox.setPositiveButton(getString(R.string.OK)) { dialog, which ->
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
						messageBox.setTitle(getString(R.string.alert_title_error_saving_deck))
						messageBox.setMessage("Error saving $title. Is it a duplicate name?")
						messageBox.setCancelable(false)
						messageBox.setNeutralButton(getString(R.string.OK), null)
						messageBox.show()
					}
				}
			}
		}

		namePromptBox.setNegativeButton(getString(R.string.CANCEL)) { dialog, which -> dialog.cancel()
		}

		namePromptBox.show()
	}

	override fun onDeckNameChanged(view: View, deck: Deck) {
		showDecks()
	}

	override fun onDeckNameNotChanged(view: View, deck: Deck) {
		// does nothing
	}
}