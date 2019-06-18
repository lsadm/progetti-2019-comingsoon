package it.unicampania.cinemap

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import it.unicampania.imdbapi.Cinema
import kotlinx.android.synthetic.main.cinema_item.view.*
import java.util.*

class CinemaAdapter(private val cinemas: ArrayList<Cinema>, private val origin: Fragment, val context: Context) :
    RecyclerView.Adapter<CinemaAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.cinema_item, parent, false))
    }

    override fun getItemCount(): Int {
        return cinemas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cinemaView.text = cinemas[position].name
        when (origin) {
            is FilmDetailFragment -> {
                holder.nMoviesView.visibility = View.GONE
                holder.filmView.visibility = View.GONE
                cinemas[position].showtimes?.let {
                    var string = ""
                    it.forEach {
                        string += "${it.get(Calendar.HOUR_OF_DAY).toString().padStart(
                            2,
                            '0'
                        )}:${it.get(Calendar.MINUTE).toString().padStart(2, '0')} "
                    }
                    holder.timesView.text = string
                }
            }
            is CinemaFragment -> {
                holder.nMoviesView.text = cinemas[position].movies!!.size.toString()
                holder.itemView.setOnClickListener {
                    Navigation.findNavController(holder.itemView)
                        .navigate(R.id.action_cinemaFragment_to_cinemaDetailFragment,
                            Bundle().apply { putParcelable("cinema", cinemas[position]) }
                        )
                }
                holder.orariView.visibility = View.GONE
                holder.timesView.visibility = View.GONE
            }
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cinemaView: TextView = view.cinemaView
        val nMoviesView: TextView = view.nMoviesView
        val orariView: TextView = view.orariView
        val timesView: TextView = view.timesView
        val filmView: TextView = view.filmView
    }
}