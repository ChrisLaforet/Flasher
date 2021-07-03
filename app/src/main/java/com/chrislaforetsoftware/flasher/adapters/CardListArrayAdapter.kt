package com.chrislaforetsoftware.flasher.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.chrislaforetsoftware.flasher.R
import com.chrislaforetsoftware.flasher.entities.Card


class CardListArrayAdapter(
    private val activity: Activity,
    private val layoutResource: Int,
    private val cardsList: List<Card>
) : ArrayAdapter<Card>(activity, layoutResource, cardsList) {

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var rowView = convertView
        if (rowView == null) {
            val layoutInflater: LayoutInflater = activity.getLayoutInflater()
            rowView = layoutInflater.inflate(layoutResource, null, true)

            viewHolder = ViewHolder()
            viewHolder.cardFace = rowView!!.findViewById<View>(R.id.card_face) as TextView
            rowView.setTag(viewHolder)
        } else {
            viewHolder = rowView.tag as ViewHolder
        }
        val card: Card = cardsList.get(position)
        viewHolder.cardFace?.text = card.face
        return rowView
    }

    internal class ViewHolder {
        var cardFace: TextView? = null
    }
}