package it.unicampania.cinemap


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.MovieDb
import it.unicampania.util.info
import it.unicampania.util.onDefault
import it.unicampania.util.onIO
import it.unicampania.util.onMain
import kotlinx.android.synthetic.main.fragment_film.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class FilmFragment : Fragment() {
    private var films = mutableListOf<MovieDb>()
    private lateinit var tmdbApi: TmdbApi
    private var currentPage = 1
    private var downloadJob: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_film, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).let { mainActivity ->
            mainActivity.supportActionBar!!.title = "Film"
            downloadJob = GlobalScope.launch {
                mainActivity.job.join()
                tmdbApi = mainActivity.tmdbApiDeferred.await()
                onMain {
                    spinKit.visibility = View.VISIBLE
                }
                films =
                    onIO {
                        tmdbApi.movies.getNowPlayingMovies("it", currentPage, "IT")
                    }.results
                        .apply {
                            onDefault {
                                removeIf { it.posterPath == null }
                                sortByDescending { it.releaseDate }
                            }
                        }

                onMain {
                    filmList.layoutManager = GridLayoutManager(view.context, 2)
                    filmList.adapter = FilmAdapter(films, this@FilmFragment, context = view.context)
                    spinKit.visibility = View.GONE
                }
            }
        }

        filmList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val gridLayoutManager = recyclerView.layoutManager!! as GridLayoutManager
                if (gridLayoutManager.findLastCompletelyVisibleItemPosition() == gridLayoutManager.itemCount - 5
                    && downloadJob.isCompleted
                ) {
                    info { "Starting update." }
                    spinKit.visibility = View.VISIBLE
                    downloadJob = GlobalScope.launch {
                        films.addAll(onIO {
                            tmdbApi.movies.getNowPlayingMovies("it", ++currentPage, "IT")
                        }.results.apply { removeIf { it.posterPath == null } })
                        onMain {
                            filmList.adapter!!.notifyDataSetChanged()
                            spinKit.visibility = View.GONE
                        }
                    }


                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        downloadJob.cancel()
        (activity as MainActivity).imageLoader.clearMemoryCache()
    }
}
