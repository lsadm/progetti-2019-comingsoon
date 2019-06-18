package it.unicampania.imdbapi

import android.icu.util.Calendar
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Cinema(
    var name: String,
    var address: String,
    var showtimes: ArrayList<Calendar>? = null,
    var showtimes3D: ArrayList<Calendar>? = null,
    var movies: ArrayList<Pair<String, ArrayList<Calendar>>>? = null
) : Parcelable