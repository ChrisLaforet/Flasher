package com.chrislaforetsoftware.flasher.types

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class StringDate(date: Date) {

	lateinit var representation: String

	init {
		val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
		representation = dateFormat.format(date)
	}

	constructor(): this(Date()) {}

	constructor(field: String): this() {
		val regex = Regex("""\d\d\d\d-\d\d-\d\d""")
		if (regex.matches(field)) {
			this.representation = field
		}
	}
}