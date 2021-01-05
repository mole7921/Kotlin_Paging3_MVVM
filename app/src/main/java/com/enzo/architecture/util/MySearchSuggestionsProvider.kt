package com.enzo.architecture.util

import android.content.SearchRecentSuggestionsProvider

class MySearchSuggestionsProvider: SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {

        private val LOG_TAG = MySearchSuggestionsProvider::class.java.simpleName

        const val AUTHORITY = "com.enzo.util.MySearchSuggestionsProvider"
        const val MODE = DATABASE_MODE_QUERIES
    }



}