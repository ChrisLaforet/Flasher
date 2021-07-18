package com.chrislaforetsoftware.flasher.serializers

import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck

data class DeckWithCards(val deck: Deck, val cards: List<Card>) {

}
