package com.chrislaforetsoftware.flasher.serializers

import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.types.StringDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

class DeckSerializer {
	companion object {
		fun serializeDeck(deck: Deck, cards: List<Card>): String {
			val cardList = mutableListOf<SerializedCard>()
			for (card in cards) {
				cardList.add(
					SerializedCard(
						card.face,
						card.reverse,
						card.quizzes,
						card.correct,
						card.misses,
						card.flagged
					)
				)
			}
			val serializedDeck = SerializedDeck(deck.name, cardList.toTypedArray())
			return Json.encodeToString(SerializedDeck.serializer(), serializedDeck)
		}

		fun deserializeDeck(serialized: String): DeckWithCards {
			val reconstituted = Json.decodeFromString(SerializedDeck.serializer(), serialized)

			val now = StringDate(Date())
			val deck = Deck()
			deck.name = reconstituted.name
			deck.created = now.representation

			val cards = mutableListOf<Card>()
			for (reconstitutedCard in reconstituted.cards) {
				val card = Card()
				card.face = reconstitutedCard.face
				card.reverse = reconstitutedCard.reverse
				card.created = now.representation
				card.correct = reconstitutedCard.correct
				card.flagged = reconstitutedCard.flagged
				card.misses = reconstitutedCard.misses
				card.quizzes = reconstitutedCard.quizzes
				cards.add(card)
			}

			return DeckWithCards(deck, cards)
		}
	}
}


@Serializable
private data class SerializedDeck(val name: String,
						  val cards: Array<SerializedCard>) {
}


@Serializable
private data class SerializedCard(
	var face: String,
	var reverse: String,
	var quizzes: Int,
	var correct: Int,
	var misses: Int,
	var flagged: Boolean) {
}