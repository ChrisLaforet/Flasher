package com.chrislaforetsoftware.flasher.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.chrislaforetsoftware.flasher.R
import com.chrislaforetsoftware.flasher.entities.Deck


class DeckListArrayAdapter(
        private val activity: Activity,
        private val layoutResource: Int,
        private val decksList: List<Deck>) : ArrayAdapter<Deck>(activity, layoutResource, decksList) {

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Deck {
        return decksList.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var rowView = convertView
        if (rowView == null) {
            val layoutInflater: LayoutInflater = activity.layoutInflater
            rowView = layoutInflater.inflate(layoutResource, null, true)

            viewHolder = ViewHolder()
            viewHolder.label = rowView!!.findViewById<View>(R.id.label) as TextView
            viewHolder.editButton = rowView.findViewById<View>(R.id.deck_edit) as ImageView

            rowView.tag = viewHolder
        } else {
            viewHolder = rowView.tag as ViewHolder
        }
        val deck: Deck = decksList[position]
        viewHolder.label?.text = deck.name
        viewHolder.label?.setTag(R.id.TAG_POSITION, position)
        viewHolder.label?.setTag(R.id.TAG_DECK, deck)

        viewHolder.editButton?.setTag(R.id.TAG_POSITION, position)
        viewHolder.editButton?.setTag(R.id.TAG_DECK, deck)

        return rowView
    }

    internal class ViewHolder {
        var label: TextView? = null
        var editButton: ImageView? = null
    }
}