package com.chrislaforetsoftware.flasher.entities

import java.io.Serializable

class Card: Serializable {
    var id: Int = 0
    var deckId: Int = 0
    var face: String = ""
    var reverse: String = ""
    var created: String = ""
    var quizzes: Int = 0
    var correct: Int = 0
    var misses: Int = 0
    var flagged: Boolean = false

    fun getFaceForSort(): String {
        return face.toLowerCase()
    }

    fun getReverseForSort(): String {
        return reverse.toLowerCase()
    }
}