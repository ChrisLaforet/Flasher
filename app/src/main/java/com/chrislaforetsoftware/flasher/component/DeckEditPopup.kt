package com.chrislaforetsoftware.flasher.component

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.chrislaforetsoftware.flasher.R
import com.chrislaforetsoftware.flasher.db.DatabaseHelper
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.entities.Fragment
import java.lang.ClassCastException

class DeckEditPopup(private val activity: AppCompatActivity) {

    interface DeckEditNoticeListener {
        fun onDeckNameChanged(view: View, deck: Deck)
        fun onDeckNameNotChanged(view: View, deck: Deck)
    }

    private var listener: DeckEditNoticeListener

    init {
        try {
            listener = activity as DeckEditNoticeListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity does not implement DeckEditNoticeListener")
        }
    }

    fun editDeck(view: View, deck: Deck) {
        val title = activity.getString(R.string.edit_deck)

        val dialog = Dialog(view.context)
        dialog.setContentView(R.layout.edit_deckview)
        dialog.setTitle(title)

        val windowParams = WindowManager.LayoutParams()
        windowParams.copyFrom(dialog.window?.attributes)
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        val deckName: EditText = dialog.findViewById(R.id.deck_name)
        deckName.setText(deck.name)

        val articles: EditText = dialog.findViewById(R.id.list_of_articles)
        val fragments: List<Fragment> = loadArticlesFor(deck)
        articles.setText(fragments.map { it.fragment }.toList().sorted().joinToString(", "))

        val okButton: Button = dialog.findViewById(R.id.ok_button_prompt)
        val cancelButton: Button = dialog.findViewById(R.id.cancel_button_prompt)

        okButton.setOnClickListener {
            var canSave = false
            var nameChanged = false
            val newDeckName = deckName.text.toString().trim()
            if (newDeckName == deck.name) {
                canSave = true
            } else if (isDeckNameAvailable(newDeckName)) {
                canSave = true
                nameChanged = true
            } else {
                Toast.makeText(activity.baseContext,
                        activity.getString(R.string.cannot_rename_deck),
                        Toast.LENGTH_SHORT).show()
            }

            if (canSave) {
                updateArticlesForDeck(deck, fragments, articles.text.toString().trim())
                if (nameChanged) {
                    renameDeckTo(deck, newDeckName)
                    listener.onDeckNameChanged(view, deck)
                } else {
                    listener.onDeckNameNotChanged(view, deck)
                }
                dialog.dismiss()
            }
        }
        cancelButton.setOnClickListener {
            dialog.cancel()
            listener.onDeckNameNotChanged(view, deck)
        }

        dialog.show()
        dialog.window?.attributes = windowParams
    }

    private fun loadArticlesFor(deck: Deck): List<Fragment> {
        val db = DatabaseHelper(activity)
        return db.getFragmentsByDeckId(deck.id)
    }

    private fun isDeckNameAvailable(newDeckName: String): Boolean {
        val db = DatabaseHelper(activity)
        return db.getDeckByName(newDeckName) == null
    }

    private fun renameDeckTo(deck: Deck, newDeckName: String) {
        val db = DatabaseHelper(activity)
        val originalDeckName = deck.name
        try {
            deck.name = newDeckName
            db.updateDeck(deck)
        } catch (ee: Exception) {
            deck.name = originalDeckName
            val messageBox = AlertDialog.Builder(activity)
            messageBox.setTitle(activity.getString(R.string.error_changing_deck_name))
            messageBox.setMessage("Error changing name of deck to $newDeckName from $originalDeckName.")
            messageBox.setCancelable(false)
            messageBox.setNeutralButton(activity.getString(R.string.OK), null)
            messageBox.show()
        }
    }

    private fun updateArticlesForDeck(deck: Deck, fragments: List<Fragment>, articleString: String) {
        val articles = mutableListOf<String>()
        articleString.split(",", ";", ":", " ").forEach {
            val article = it.trim()
            if (article.isNotEmpty() && !articles.contains(article)) {
                articles.add(article)
            }
        }

        val (matchedArticles, newArticles) = articles.partition { article ->
            fragments.any { it.fragment == article }
        }

        try {
            val db = DatabaseHelper(activity)
            fragments.forEach {
                if (!matchedArticles.contains(it.fragment)) {
                    db.deleteFragment(it)
                }
            }

            newArticles.forEach {
                val fragment = Fragment()
                fragment.deckId = deck.id
                fragment.fragment = it
                db.createFragment(fragment)
            }
        } catch (ee: Exception) {
            val messageBox = AlertDialog.Builder(activity)
            messageBox.setTitle(activity.getString(R.string.error_saving_articles))
            messageBox.setMessage(activity.getString(R.string.error_encountered_updating_articles))
            messageBox.setCancelable(false)
            messageBox.setNeutralButton(activity.getString(R.string.OK), null)
            messageBox.show()
        }
    }
}