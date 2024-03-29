package com.chrislaforetsoftware.flasher.serializers

import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.entities.Fragment
import com.chrislaforetsoftware.flasher.types.StringDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

class DeckSerializer {
	companion object {
		fun serializeDeck(deck: Deck, cards: List<Card>, fragments: List<Fragment>, includeFlaggings: Boolean, includeStatistics: Boolean): String {
			val cardList = mutableListOf<SerializedCard>()
			for (card in cards) {
				cardList.add(
					SerializedCard(
						card.face,
						card.reverse,
						if (includeStatistics) card.quizzes else 0,
						if (includeStatistics) card.correct else 0,
						if (includeStatistics) card.misses else 0,
						if (includeFlaggings) card.flagged else false
					)
				)
			}
			val fragmentList = mutableListOf<String>()
			for (fragment in fragments) {
				fragmentList.add(fragment.fragment)
			}

			val serializedDeck = SerializedDeck(deck.name, includeFlaggings, includeStatistics, cardList.toTypedArray(), fragmentList.toTypedArray())
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

			val fragments = mutableListOf<String>()
			for (reconsitutedFragment in reconstituted.fragments) {
				fragments.add(reconsitutedFragment)
			}

			return DeckWithCards(deck, cards, fragments)
		}
	}
}


@Serializable
private data class SerializedDeck(val name: String,
								  val includesFlaggings: Boolean,
								  val includesStatistics: Boolean,
								  val cards: Array<SerializedCard>,
								  val fragments: Array<String>) {
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