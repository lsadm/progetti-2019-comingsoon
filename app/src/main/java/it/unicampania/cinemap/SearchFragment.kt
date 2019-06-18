package it.unicampania.cinemap


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.MovieDb
import it.unicampania.util.onDefault
import it.unicampania.util.onIO
import it.unicampania.util.onMain
import it.unicampania.util.snackBarMessageIf
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private var films = mutableListOf<MovieDb>()
    lateinit var tmdbApi: TmdbApi
    var currentQueryLenght = 0
    var searchJob: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).let {

            it.supportActionBar!!.title = "Search"
            GlobalScope.launch {
                it.job.join()
                tmdbApi = it.tmdbApiDeferred.await()
                onMain {
                    searchList.layoutManager = GridLayoutManager(view.context, 2)
                    searchList.adapter = FilmAdapter(films, this@SearchFragment, context = view.context)
                }
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                snackBarMessageIf(query!!.length < 3) {
                    "Inserisci almeno tre caratteri."
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.length > 2) {
                    films.clear()
                    currentQueryLenght = newText.length
                    searchJob.cancel()
                    searchJob = GlobalScope.launch {
                        onMain {
                            spinKit.visibility = View.VISIBLE
                        }

                        delay(200)

                        if (currentQueryLenght == newText.length) {
                            films.addAll(
                                onIO {
                                    tmdbApi.search.searchMovie(newText, 0, "it", false, 1)
                                }.results
                                    .filter { it.posterPath != null }
                                    .let {
                                        onDefault {
                                            it.sortedByDescending { it.popularity }
                                        }
                                    })

                            onMain {
                                if (films.size > 0) {
                                    searchList.adapter!!.notifyDataSetChanged()
                                }
                                spinKit.visibility = View.GONE
                            }
                        }
                    }

                }
                return true
            }
        })
    }

    override fun onStop() {
        super.onStop()
        searchJob.cancel()
    }
}