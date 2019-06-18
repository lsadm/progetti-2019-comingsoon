package it.unicampania.imdbapi

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import it.unicampania.util.error
import it.unicampania.util.info
import it.unicampania.util.onDefault
import it.unicampania.util.onIO
import org.jsoup.Jsoup
import java.text.SimpleDateFormat


object IMDBApi {
    private const val BASE_URL = "https://www.imdb.com"
    private const val SHOWTIMES = "showtimes"


    suspend fun getMovieShowtimes(imdbId: String, cap: String, date: String = ""): ArrayList<Cinema>? {
        if (!imdbId.matches(Regex("^tt[0-9]{5,9}"))) {
            Log.e("IMDBAPI", "imdbID not valid")
            return null
        }
        if (date != "" && !date.matches(Regex("^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]"))) {
            Log.e("IMDBAPI", "date not valid")
            return null
        }

        val uri = Uri.parse(BASE_URL).buildUpon()
            .appendEncodedPath(SHOWTIMES)
            .appendEncodedPath("title")
            .appendEncodedPath(imdbId)
            .appendEncodedPath("IT")
            .appendEncodedPath(cap)

        if (date != "") {
            uri.appendEncodedPath(date)
        }
        val doc = onIO {
            Jsoup.connect(uri.build().toString()).get()
        }

        val cinemas = ArrayList<Cinema>()

        onDefault {
            doc.select(".list_item").forEach {
                var showtimesArray: ArrayList<Calendar>? = null

                var showtimes3DArray: ArrayList<Calendar>? = null

                var address = ""

                val loc = it.select(".address").first().getElementsByAttributeValue("itemprop", "location").first()
                address += loc.getElementsByAttributeValue("itemprop", "streetAddress").text() + ", "
                address += loc.getElementsByAttributeValue("itemprop", "addressLocality").text()

                it.select(".showtimes").let { showtimes ->
                    if (showtimes.size > 0) {
                        showtimesArray = ArrayList()
                        showtimes.first().children().filter { it.hasAttr("content") }.forEach { times ->
                            showtimesArray!!.add(Calendar.getInstance().also {
                                it.time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(times.attr("content"))
                            })
                        }
                        if (showtimes.size > 1) {
                            showtimes3DArray = ArrayList()
                            showtimes.last().children().filter { it.hasAttr("content") }.forEach { times ->
                                showtimes3DArray?.add(Calendar.getInstance().also {
                                    it.time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(times.attr("content"))
                                })
                            }
                        }
                    }
                }

                cinemas.add(
                    Cinema(
                        it.getElementsByAttributeValue("itemprop", "name").text(),
                        address,
                        showtimesArray,
                        showtimes3DArray
                    )
                )
            }
        }

        return if (cinemas.size > 0) cinemas else null
    }

    suspend fun getCinemas(cap: String): ArrayList<Cinema> {
        val uri = Uri.parse(BASE_URL).buildUpon()
            .appendEncodedPath(SHOWTIMES)
            .appendEncodedPath("IT")
            .appendEncodedPath(cap)
        val doc = onIO {
            info { uri.build().toString() }
            Jsoup.connect(uri.build().toString()).get()
        }
        val cinemas = ArrayList<Cinema>()
        try {
            onDefault {
                doc.select("#cinemas-at-list").first().children()
                    .filter { it.`is`(".list_item") }
                    .forEach {
                        val movies = ArrayList<Pair<String, ArrayList<Calendar>>>()
                        val name = it.children().first().select("a[href]").text()
                        var address = ""

                        val loc = it.select(".address").first().children().first { it.hasAttr("itemscope") }
                        address += loc.getElementsByAttributeValue("itemprop", "streetAddress").text() + ", "
                        address += loc.getElementsByAttributeValue("itemprop", "addressLocality").text()

                        it.select(".list_item").forEach { movie ->
                            val link = movie.select(".info").select("a[href]").attr("href")
                            val id = Regex("tt[0-9]{5,9}").find(link)!!.value
                            val showtimesArray = ArrayList<Calendar>()


                            val text = movie.select(".info").first().select(".showtimes").text()

                            text.split("|").forEach {

                                showtimesArray.add(Calendar.getInstance().also { calendar ->
                                    calendar.time =
                                        SimpleDateFormat("HH:mm").parse(it.replace("pm", "").trim())
                                })
                            }


                            movies.add(Pair(id, showtimesArray))
                        }

                        movies.removeAt(0)

                        cinemas.add(
                            Cinema(
                                name,
                                address,
                                movies = movies
                            )
                        )
                    }
            }
        } catch (e: Exception) {
            error { e.stackTrace.toString() }
        }

        return cinemas
    }
}