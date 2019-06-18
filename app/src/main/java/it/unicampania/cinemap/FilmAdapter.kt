package it.unicampania.cinemap

import android.content.Context
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import com.github.ybq.android.spinkit.SpinKitView
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import info.movito.themoviedbapi.model.MovieDb
import it.unicampania.util.error
import it.unicampania.util.onMain
import kotlinx.android.synthetic.main.film_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FilmAdapter(
    private val films: MutableList<MovieDb>,
    private val origin: Fragment,
    private val times: HashMap<Int, ArrayList<Calendar>>? = null,
    val context: Context
) :
    RecyclerView.Adapter<FilmAdapter.ViewHolder>() {
    private val BASE_URL = "https://image.tmdb.org/t/p/"
    private val W500 = "w500"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.film_item, parent, false))
    }

    override fun getItemCount(): Int {
        return films.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            Navigation.findNavController(it).navigate(
                when (origin) {
                    is FilmFragment -> {
                        R.id.action_filmFragment_to_filmDetailFragment
                    }
                    is SearchFragment -> {
                        R.id.action_searchFragment_to_filmDetailFragment
                    }
                    else -> {
                        R.id.filmDetailFragment
                    }
                }, Bundle().apply {
                    putInt("movieId", films[position].id)
                }
            )
        }
        holder.filmTitle.text = films[position].title
        GlobalScope.launch {
            onMain {
                (origin.activity as MainActivity).imageLoader.displayImage(
                    BASE_URL + W500 + films[position].posterPath,
                    holder.filmImage,
                    object : SimpleImageLoadingListener() {
                        override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                            super.onLoadingComplete(imageUri, view, loadedImage)
                            holder.spinKit.visibility = View.GONE
                        }

                        override fun onLoadingStarted(imageUri: String?, view: View?) {
                            super.onLoadingStarted(imageUri, view)
                            holder.spinKit.visibility = View.VISIBLE
                        }
                    }
                )
                when (origin) {
                    is FilmFragment -> {
                        holder.orariFilmView.visibility = View.GONE
                        holder.timesFilmView.visibility = View.GONE
                    }
                    is CinemaDetailFragment -> {
                        var string = ""
                        times!![films[position].id]?.let {
                            it.forEach {
                                string += "${(it.get(java.util.Calendar.HOUR_OF_DAY) + 12).toString().padStart(
                                    2,
                                    '0'
                                )}:${it.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')} "
                            }
                            holder.timesFilmView.text = string
                        }
                    }
                    is SearchFragment -> {
                        holder.orariFilmView.visibility = View.GONE
                        holder.timesFilmView.visibility = View.GONE
                    }
                    else -> {
                        error { "Nope." }
                    }

                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val filmTitle: TextView = view.filmTitle
        val filmImage: ImageView = view.filmImage
        val spinKit: SpinKitView = view.spinKit
        val orariFilmView: TextView = view.orariFilmView
        val timesFilmView: TextView = view.timesFilmView
    }
}