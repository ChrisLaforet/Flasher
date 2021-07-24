package com.chrislaforetsoftware.flasher.entities

import java.io.Serializable

class Deck: Serializable {
    var id: Int = 0
    var name: String = ""
    var created: String = ""
    var lastUse: String? = null

    fun getDeckNameAsFilename() : String {
        var filename: String = name.replace(' ', '_')
        filename = name.replace('/','_')
        filename = name.replace('\\','_')
        return filename
    }
}