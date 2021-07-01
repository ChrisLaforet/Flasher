package com.chrislaforetsoftware.flasher.types

import org.junit.Test

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

class StringDateTest {

	@Test
	fun givenNoDateAsParameter_whenCreatingStringData_thenReturnsString() {
		val result = SimpleDateFormat("yyyy-MM-dd").format(Date())
		val stringDate = StringDate()
		assertEquals(result, stringDate.representation)
	}

	@Test
	fun givenDateAsParameter_whenCreatingStringData_thenReturnsString() {
		val date = Date()
		val result = SimpleDateFormat("yyyy-MM-dd").format(date)
		val stringDate = StringDate(date)
		assertEquals(result, stringDate.representation)
	}

	@Test
	fun givenDateStringAsParameter_whenCreatingStringData_thenReturnsString() {
		val result = "2021-01-31"
		val stringDate = StringDate(result)
		assertEquals(result, stringDate.representation)
	}

	@Test
	fun givenMalformedDateStringAsParameter_whenCreatingStringData_thenReturnsTodayAsString() {
		val date = Date()
		val result = SimpleDateFormat("yyyy-MM-dd").format(date)
		val stringDate = StringDate("202-1-31A")
		assertEquals(result, stringDate.representation)
	}
}