package edu.artic.map

import com.google.android.gms.maps.model.LatLng
import edu.artic.db.models.ArticGallery
import edu.artic.db.models.ArticMapAnnotation
import edu.artic.db.models.ArticObject
import edu.artic.db.models.ArticTour
import edu.artic.map.helpers.toLatLng

/**
 * Simple wrapper around DAO entities, intended to simplify the logic within [MapViewModel].
 *
 * Each instance of this class provides data for one marker on the
 * [map][com.google.android.gms.maps.GoogleMap].
 *
 * @param floor the floor of the map where said marker should appear
 */
sealed class MapItem<T>(val item: T, val floor: Int) {
    /**
     * Rich-format label for an area of the map.
     */
    class Annotation(item: ArticMapAnnotation, floor: Int) : MapItem<ArticMapAnnotation>(item, floor)

    /**
     * This acts as a sort of group for multiple [MapItem.Object]s.
     */
    class Gallery(gallery: ArticGallery, floor: Int) : MapItem<ArticGallery>(gallery, floor)

    /**
     * This provides the greatest level of detail - it can represent individual art pieces.
     *
     * There are usually an order of magnitude more [Object]s than [Gallery]s.
     */
    class Object(item: ArticObject, floor: Int) : MapItem<ArticObject>(item, floor)

    /**
     * This provides detail about the tour object.
     */
    class TourIntro(item: ArticTour, floor: Int) : MapItem<ArticTour>(item, floor)

    /**
     * TODO: This function is currently unused. Perhaps convert to abstract method?
     */
    fun toLatLng() : LatLng {
        return when {
            this is Annotation -> item.toLatLng()
            this is Gallery -> item.toLatLng()
            this is Object -> item.toLatLng()
            else -> throw IllegalStateException()
        }
    }
}