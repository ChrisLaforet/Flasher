package com.chrislaforetsoftware.flasher.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.chrislaforetsoftware.flasher.entities.Card
import com.chrislaforetsoftware.flasher.entities.Deck
import com.chrislaforetsoftware.flasher.exception.DatabaseException

class DatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    companion object {
        const val DATABASE_NAME = "FlasherCard.db"

        const val DECK_TABLE = "decks"
        const val DECK_ID = "ID"
        const val DECK_NAME = "NAME"
        const val DECK_CREATED = "Created"
        const val DECK_LASTUSE = "LastUsed"

        const val CARD_TABLE = "cards"
        const val CARD_ID = "ID"
        const val CARD_DECK_ID = "DeckID"
        const val CARD_FACE = "Face"
        const val CARD_REVERSE = "Reverse"
        const val CARD_CREATED = "Created"
        const val CARD_QUIZZES = "TotalQuizzes"
        const val CARD_CORRECT = "TotalCorrect"
        const val CARD_MISSES = "TotalMisses"
        const val CARD_FLAGGED = "Flagged"
    }

    val db: SQLiteDatabase by lazy {
        this.writableDatabase
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE " + DECK_TABLE + " (" +
                DECK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DECK_NAME + " TEXT NOT NULL UNIQUE, " +
                DECK_CREATED + " TEXT NOT NULL, " +
                DECK_LASTUSE + " TEXT," +
                " UNIQUE (" + DECK_NAME + " COLLATE NOCASE)" +
                ")")

        db?.execSQL("CREATE TABLE " + CARD_TABLE + " (" +
                CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CARD_DECK_ID + " INTEGER NOT NULL, " +
                CARD_FACE + " TEXT NOT NULL, " +
                CARD_REVERSE + " TEXT NOT NULL, " +
                CARD_CREATED + " TEXT NOT NULL, " +
                CARD_QUIZZES + " INTEGER, " +
                CARD_CORRECT + " INTEGER, " +
                CARD_MISSES + " INTEGER, " +
                CARD_FLAGGED + " INTEGER, " +
                "FOREIGN KEY(" + CARD_DECK_ID + ") REFERENCES " + DECK_TABLE + "(" + DECK_ID + ") ON DELETE CASCADE " +
                ")")
    }

    override fun onUpgrade(db: SQLiteDatabase?, fromVersion: Int, toVersion: Int) {
        TODO("Not yet implemented - do nothing to the existing table until there is the need to move versions")
        // load data into something, execute drop table if exists on tables, call onCreate, then retrofit the data into new tables
    }

    private fun checkRowId(rowId: Long, tableName: String) {
        if (rowId == -1L) {
            throw DatabaseException("Error writing to $tableName")
        }
    }

    fun createDeck(deck: Deck) {
        val db: SQLiteDatabase = this.writableDatabase
        db.use {
            val values = ContentValues()
            values.put(DECK_NAME, deck.name)
            values.put(DECK_CREATED, deck.created)
            values.put(DECK_LASTUSE, deck.lastUse)
            val rowId = db.insert(DECK_TABLE,null, values)
            checkRowId(rowId, DECK_TABLE)
            deck.id = rowId.toInt()
        }
    }

    fun createCard(card: Card) {
        val db: SQLiteDatabase = this.writableDatabase
        db.use {
            val values = ContentValues()
            values.put(CARD_DECK_ID, card.deckId)
            values.put(CARD_FACE, card.face)
            values.put(CARD_REVERSE, card.reverse)
            values.put(CARD_CREATED, card.created)
            values.put(CARD_QUIZZES, card.quizzes)
            values.put(CARD_CORRECT, card.correct)
            values.put(CARD_MISSES, card.misses)
            values.put(CARD_FLAGGED, if (card.flagged) 1 else 0)
            val rowId = db.insert(CARD_TABLE,null, values)
            checkRowId(rowId, CARD_TABLE)
            card.id = rowId.toInt()
        }
    }

    fun updateDeck(deck: Deck) {
        val db: SQLiteDatabase = this.writableDatabase
        db.use {
            val values = ContentValues()
            values.put(DECK_NAME, deck.name)
            values.put(DECK_CREATED, deck.created)
            values.put(DECK_LASTUSE, deck.lastUse)
            val success = db.update(DECK_TABLE, values,DECK_ID + "=" + deck.id, null)
            if (success == 0) {
                throw DatabaseException("Error updating $DECK_TABLE")
            }
        }
    }

    fun updateCard(card: Card) {
        val db: SQLiteDatabase = this.writableDatabase
        db.use {
            val values = ContentValues()
            values.put(CARD_DECK_ID, card.deckId)
            values.put(CARD_FACE, card.face)
            values.put(CARD_REVERSE, card.reverse)
            values.put(CARD_CREATED, card.created)
            values.put(CARD_QUIZZES, card.quizzes)
            values.put(CARD_CORRECT, card.correct)
            values.put(CARD_MISSES, card.misses)
            values.put(CARD_FLAGGED, if (card.flagged) 1 else 0)
            val success = db.update(CARD_TABLE, values,CARD_ID + "=" + card.id, null)
            if (success == 0) {
                throw DatabaseException("Error updating $CARD_TABLE")
            }
        }
    }

    fun getDecks(): List<Deck> {
        val decks: MutableList<Deck> = mutableListOf()

        val db: SQLiteDatabase = this.readableDatabase
        db.use {
            val selectQuery = "SELECT * FROM $DECK_TABLE"
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    decks.add(extractDeckFromCursor(cursor))
                }
            }
        }
        return decks
    }

    fun getDeckNames(): List<String> {
        val deckNames: MutableList<String> = mutableListOf()
        for (deck in getDecks()) {
            deckNames.add(deck.name)
        }
        return deckNames
    }

    private fun extractDeckFromCursor(cursor: Cursor): Deck {
        val deck = Deck()
        deck.id = cursor.getInt(cursor.getColumnIndex(DECK_ID))
        deck.name = cursor.getString(cursor.getColumnIndex(DECK_NAME))
        deck.created = cursor.getString(cursor.getColumnIndex(DECK_CREATED))
        deck.lastUse = cursor.getString(cursor.getColumnIndex(DECK_LASTUSE))
        return deck
    }

    fun getDeckById(id: Int): Deck? {
        val db: SQLiteDatabase = this.readableDatabase
        db.use {
            val selectQuery = "SELECT * FROM $DECK_TABLE WHERE $DECK_ID = $id"
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor != null && cursor.moveToNext()) {
                return extractDeckFromCursor(cursor)
            }
        }
        return null
    }

    fun getDeckByName(name: String): Deck? {
        val db: SQLiteDatabase = this.readableDatabase
        db.use {
            val selectQuery = "SELECT * FROM $DECK_TABLE WHERE $DECK_NAME = '$name'"
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor != null && cursor.moveToNext()) {
                return extractDeckFromCursor(cursor)
            }
        }
        return null
    }

    private fun extractCardFromCursor(cursor: Cursor): Card {
        val card = Card()
        card.id = cursor.getInt(cursor.getColumnIndex(CARD_ID))
        card.deckId = cursor.getInt(cursor.getColumnIndex(CARD_DECK_ID))
        card.face = cursor.getString(cursor.getColumnIndex(CARD_FACE))
        card.reverse = cursor.getString(cursor.getColumnIndex(CARD_REVERSE))
        card.created = cursor.getString(cursor.getColumnIndex(CARD_CREATED))
        card.quizzes = cursor.getInt(cursor.getColumnIndex(CARD_QUIZZES))
        card.correct = cursor.getInt(cursor.getColumnIndex(CARD_CORRECT))
        card.misses = cursor.getInt(cursor.getColumnIndex(CARD_MISSES))
        card.flagged = cursor.getInt(cursor.getColumnIndex(CARD_FLAGGED)) != 0
        return card
    }

    fun getCardsByDeckId(deckId: Int): List<Card> {
        val cards: MutableList<Card> = mutableListOf()
        val db: SQLiteDatabase = this.readableDatabase
        db.use {
            val selectQuery = "SELECT * FROM $CARD_TABLE WHERE $CARD_DECK_ID = $deckId"
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    cards.add(extractCardFromCursor(cursor))
                }
            }
        }
        return cards
    }

    fun getCardById(id: Int): Card? {
        var card: Card? = null
        val db: SQLiteDatabase = this.readableDatabase
        db.use {
            val selectQuery = "SELECT * FROM $CARD_TABLE WHERE $CARD_ID = $id"
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    card = extractCardFromCursor(cursor)
                }
            }
        }
        return card
    }

    fun deleteCard(card: Card) {
        val db: SQLiteDatabase = this.writableDatabase
        db.use {
            db.delete(CARD_TABLE, "$CARD_ID=${card.id}", null)
        }
    }
}