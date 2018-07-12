package edu.artic.db.models

import com.squareup.moshi.Json

data class ArticMapAnnotation(
        @Json(name = "title") val title: String?,
        @Json(name = "status") val status: String?,
        @Json(name = "nid") val nid: String?,
        @Json(name = "type") val type: String?,
        @Json(name = "translations") val translations: List<Any>,
        @Json(name = "location") val location: String?,
        @Json(name = "latitude") val latitude: String?,
        @Json(name = "longitude") val longitude: String?,
        @Json(name = "floor") val floor: String?,
        @Json(name = "description") val description: String??,
        @Json(name = "label") val label: String??,
        @Json(name = "annotation_type") val annotationType: String??,
        @Json(name = "text_type") val textType: String??,
        @Json(name = "amenity_type") val amenityType: String??,
        @Json(name = "image_filename") val imageFilename: String??,
        @Json(name = "image_url") val imageUrl: String??,
        @Json(name = "image_filemime") val imageFileMime: String??,
        @Json(name = "image_filesize") val imageFileSize: String??,
        @Json(name = "image_width") val imageWidth: String??,
        @Json(name = "image_height") val imageHeight: String??
)