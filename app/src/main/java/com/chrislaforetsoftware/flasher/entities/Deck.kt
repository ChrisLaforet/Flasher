package com.chrislaforetsoftware.flasher.entities

import java.io.Serializable

class Deck: Serializable {
    var id: Int = 0
    var name: String = ""
    var created: String = ""
    var lastUse: String? = null
}