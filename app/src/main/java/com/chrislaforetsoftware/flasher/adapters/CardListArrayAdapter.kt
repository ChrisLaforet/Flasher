package com.chrislaforetsoftware.flasher.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.chrislaforetsoftware.flasher.R
import com.chrislaforetsoftware.flasher.entities.Card


class CardListArrayAdapter(
        private val activity: Activity,
        private val layoutResource: Int,
        private val showFace: Boolean,
        private val cardsList: List<Card>) : ArrayAdapter<Card>(activity, layoutResource, cardsList) {

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Card? {
        return cardsList.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var rowView = convertView
        if (rowView == null) {
            val layoutInflater: LayoutInflater = activity.getLayoutInflater()
            rowView = layoutInflater.inflate(layoutResource, null, true)

            viewHolder = ViewHolder()
            viewHolder.cardFace = rowView!!.findViewById<View>(R.id.card_face) as TextView
            viewHolder.flagged = rowView.findViewById<View>(R.id.card_flagged) as ImageView
            viewHolder.editButton = rowView.findViewById<View>(R.id.card_edit) as ImageView
            viewHolder.deleteButton = rowView.findViewById<View>(R.id.card_delete) as ImageView

            viewHolder.clearFlagImage = rowView.context.getDrawable(R.drawable.clearflag) as Drawable
            viewHolder.redFlagImage = rowView.context.getDrawable(R.drawable.redflag) as Drawable

            rowView.setTag(viewHolder)
        } else {
            viewHolder = rowView.tag as ViewHolder
        }
        val card: Card = cardsList.get(position)
        val textForCard: String = if (showFace) card.face else card.reverse
        viewHolder.cardFace?.text = textForCard

        viewHolder.flagged?.setImageDrawable(if (card.flagged) viewHolder.redFlagImage else viewHolder.clearFlagImage)
        viewHolder.flagged?.setTag(R.id.TAG_POSITION, position)
        viewHolder.flagged?.setTag(R.id.TAG_CARD, card)

        viewHolder.editButton?.setTag(R.id.TAG_POSITION, position)
        viewHolder.editButton?.setTag(R.id.TAG_CARD, card)

        viewHolder.deleteButton?.setTag(R.id.TAG_POSITION, position)
        viewHolder.deleteButton?.setTag(R.id.TAG_CARD, card)

        return rowView
    }

    internal class ViewHolder {
        var cardFace: TextView? = null
        var flagged: ImageView? = null
        var editButton: ImageView? = null
        var deleteButton: ImageView? = null
        var clearFlagImage: Drawable? = null
        var redFlagImage: Drawable? = null
    }
}