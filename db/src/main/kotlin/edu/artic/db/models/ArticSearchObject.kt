package edu.artic.db.models

import com.squareup.moshi.Json

data class ArticSearchObject(
        @Json(name = "search_strings") val searchStrings: Map<String, String>,
        @Json(name = "search_objects") val searchObjects: List<Int>
)