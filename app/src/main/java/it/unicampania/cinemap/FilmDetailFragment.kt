package it.unicampania.cinemap

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.TmdbMovies
import info.movito.themoviedbapi.model.MovieDb
import it.unicampania.imdbapi.Cinema
import it.unicampania.imdbapi.IMDBApi
import it.unicampania.util.onDefault
import it.unicampania.util.onMain
import it.unicampania.util.setAnimateOnClickListener
import it.unicampania.util.snackBarMessage
import kotlinx.android.synthetic.main.fragment_film_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FilmDetailFragment : Fragment() {
    private var movie: MovieDb? = null
    private lateinit var tmdbApi: TmdbApi
    var waitForInitJob: Job = Job()

    var tramaOpened = false
    var progOpened = false
    var infoOpened = false
    var castOpened = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).let {
            waitForInitJob = GlobalScope.launch {
                it.job.join()
                tmdbApi = it.tmdbApiDeferred.await()
                arguments?.let {
                    val movieId = it.getInt("movieId")
                    movie = tmdbApi.movies.getMovie(movieId, "it", TmdbMovies.MovieMethod.credits)
                }
            }
        }
        return inflater.inflate(R.layout.fragment_film_detail, container, false)
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch {
            onMain {
                spinKit.visibility = View.VISIBLE
                scrollView2.visibility = View.GONE
            }
            waitForInitJob.join()
            movie?.let {
                var orari: ArrayList<Cinema>? = null
                it.imdbID?.let { imdbID ->
                    orari = IMDBApi.getMovieShowtimes(
                        imdbID, (activity as MainActivity).getSharedPreferences(
                            "CAP",
                            Context.MODE_PRIVATE
                        ).getString("CAP", "81030")!!
                    )
                }
                onMain {
                    (activity as MainActivity).imageLoader.let { imageLoader ->
                        imageLoader.displayImage(
                            "https://image.tmdb.org/t/p/w500" + it.posterPath,
                            filmImage
                        )
                        imageLoader.displayImage(
                            "https://image.tmdb.org/t/p/original" + it.backdropPath,
                            backgroundImage
                        )
                    }

                    (activity as MainActivity).supportActionBar!!.title = it.title

                    tramaCardView.setAnimateOnClickListener(tramaView, tramaChevron, { tramaOpened }) {
                        tramaOpened = !tramaOpened
                    }

                    progLayout.setAnimateOnClickListener(timesList, progChevron, { progOpened }) {
                        progOpened = !progOpened
                    }

                    infoLayout.setAnimateOnClickListener(infoView, infoChevron, { infoOpened }) {
                        infoOpened = !infoOpened
                    }

                    castLayout.setAnimateOnClickListener(castList, castChevron, { castOpened }) {
                        castOpened = !castOpened
                    }

                    spinKit.visibility = View.GONE
                    scrollView2.visibility = View.VISIBLE
                    dataView.text =
                        onDefault {
                            DateTimeFormatter.ofPattern("dd-MM-yyyy").format(
                                LocalDate.parse(it.releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            )
                        }
                    tramaView.text = it.overview
                    durationView.text = it.runtime.toString()
                    votationView.text = if (it.voteAverage != 0.0f) it.voteAverage.toString() else "n/a"
                    orari?.let { _ ->
                        if (LocalDate.parse(it.releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).isBefore(
                                LocalDate.now()
                            )
                        ) {
                            timesList.layoutManager =
                                LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
                            timesList.adapter = CinemaAdapter(orari!!, this@FilmDetailFragment, view.context)
                        } else {
                            progCardView.visibility = View.GONE
                        }
                    } ?: run {
                        progCardView.visibility = View.GONE
                    }
                    it.cast?.let { cast ->
                        castList.layoutManager =
                            LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
                        castList.adapter = CastAdapter(
                            cast.subList(0, if (cast.size < 7) cast.size else 7),
                            (activity as MainActivity).imageLoader,
                            view.context
                        )
                    } ?: run {
                        castCardView.visibility = View.GONE
                    }
                }
            } ?: snackBarMessage { "Film non trovato" }
        }
    }
}