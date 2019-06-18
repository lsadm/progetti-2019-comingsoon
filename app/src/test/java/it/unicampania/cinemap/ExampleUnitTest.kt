package it.unicampania.cinemap

import info.movito.themoviedbapi.TmdbApi
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun apiKey_isValid(){
        val apikey = BuildConfig.THE_MOVIE_DB_API_TOKEN
        assert {
            try {
                TmdbApi(apikey)
                true
            }catch (e:Exception){
                e.printStackTrace()
                false
            }
        }
    }
}


fun assert(block :()->Boolean){
    assert(block())
}