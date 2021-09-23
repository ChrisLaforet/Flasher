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
import com.chrislaforetsoftware.flasher.entities.Card
import java.lang.ClassCastException

class CardEditPopup(private val activity: AppCompatActivity) {

    interface CardEditNoticeListener {
        fun onCardChanged(view: View, card: Card, noticeValue: String)
        fun onCardNotChanged(view: View, card: Card, noticeValue: String)
    }

    private lateinit var listener: CardEditNoticeListener

    init {
        try {
            listener = activity as CardEditNoticeListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity does not implement CardEditNoticeListener")
        }
    }

    fun editCardContent(view: View, card: Card, noticeValue: String = "") {
        val isNew = card.id == 0
        val createTitle = activity.getString(R.string.title_create)
        val editTitle = activity.getString(R.string.title_edit)
        val title = (if (isNew) createTitle else editTitle) + " " + activity.getString(R.string.title_flashcard)

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
            cardFront.setText(card.face)
            cardReverse.setText(card.reverse)
        }

        okButton.setOnClickListener {
            val front:String = cardFront.text.toString().trim()
            val reverse: String = cardReverse.text.toString().trim()
            if (front.isEmpty() && reverse.isEmpty()) {
                Toast.makeText(activity.baseContext,
                    activity.getString(R.string.cannot_save_empty_card),
                    Toast.LENGTH_SHORT).show()
            } else if (front.isEmpty()) {
                Toast.makeText(activity.baseContext,
                    activity.getString(R.string.cannot_save_blank_learning_language),
                    Toast.LENGTH_SHORT).show()
            } else if (reverse.isEmpty()) {
                Toast.makeText(activity.baseContext,
                    activity.getString(R.string.cannot_save_blank_native_language),
                    Toast.LENGTH_SHORT).show()
            } else {
                val changed = saveCard(card, front, reverse)
                dialog.dismiss()
                if (changed) listener.onCardChanged(view, card, noticeValue) else listener.onCardNotChanged(view, card, noticeValue)
            }
        }
        cancelButton.setOnClickListener {
            dialog.cancel()
            listener.onCardNotChanged(view, card, noticeValue)
        }

        dialog.show()
        dialog.window?.attributes = windowParams
    }

    private fun saveCard(card: Card, front: String, reverse: String): Boolean {
        val isNew = card.id == 0

        card.face = front
        card.reverse = reverse

        val db = DatabaseHelper(activity)

        // TODO prevent duplicate face value


        // TODO update deck last use


        try {
            if (isNew) {
                db.createCard(card)
            } else {
                db.updateCard(card)
            }
            return true
        } catch (ee: Exception) {
            val messageBox = AlertDialog.Builder(activity)
            messageBox.setTitle(activity.getString(R.string.alert_title_error_saving_card))
            messageBox.setMessage("Error saving $front. Is it a duplicate value?")
            messageBox.setCancelable(false)
            messageBox.setNeutralButton(activity.getString(R.string.OK), null)
            messageBox.show()
        }
        return false
    }

}