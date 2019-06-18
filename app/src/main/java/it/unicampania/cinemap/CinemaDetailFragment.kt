package it.unicampania.cinemap


import android.content.ActivityNotFoundException
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.TmdbFind
import info.movito.themoviedbapi.model.MovieDb
import it.unicampania.imdbapi.Cinema
import it.unicampania.util.onIO
import it.unicampania.util.onMain
import kotlinx.android.synthetic.main.fragment_cinema_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URLEncoder


class CinemaDetailFragment : Fragment() {

    val films = ArrayList<MovieDb>()
    val times = HashMap<Int, ArrayList<Calendar>>()
    private lateinit var tmdbApi: TmdbApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cinema_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val cinema = it.getParcelable<Cinema>("cinema")
            GlobalScope.launch {
                tmdbApi = (activity as MainActivity).tmdbApiDeferred.await()
                cinema!!.movies?.let {
                    films.clear()
                    onMain {
                        (activity as MainActivity).supportActionBar?.title = cinema.name
                        spinKit.visibility = View.VISIBLE
                        progView.visibility = View.GONE
                        addressCinema.visibility = View.GONE
                        addressCinema.text = cinema.address
                    }
                    it.forEach { pair ->
                        onIO {
                            tmdbApi.find.find(
                                pair.first,
                                TmdbFind.ExternalSource.imdb_id,
                                "it"
                            ).movieResults.firstOrNull()?.let {
                                times[it.id] = pair.second
                                films.add(it)
                            }

                        }
                    }
                }

                onMain {
                    mapsLayout.setOnClickListener {
                        var intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                "https://maps.google.com/maps?q=${URLEncoder.encode(
                                    cinema.name,
                                    "utf-8"
                                )}"
                            )
                        )
                        intent.`package` = "com.google.android.apps.maps"
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://maps.google.com/maps?q=${URLEncoder.encode(
                                        cinema.name,
                                        "utf-8"
                                    )}"
                                )
                            )

                            startActivity(intent)
                        }
                    }
                    listCinema.layoutManager = GridLayoutManager(view.context, 2)
                    listCinema.adapter = FilmAdapter(
                        films,
                        this@CinemaDetailFragment,
                        times,
                        view.context
                    )
                    spinKit.visibility = View.GONE
                    progView.visibility = View.VISIBLE
                    addressCinema.visibility = View.VISIBLE
                }
            }
        }

    }
}