package it.unicampania.cinemap

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import info.movito.themoviedbapi.model.people.PersonCast
import kotlinx.android.synthetic.main.cast_item.view.*

class CastAdapter(
    private val crew: MutableList<PersonCast>,
    private val imageLoader: ImageLoader,
    val context: Context
) :
    RecyclerView.Adapter<CastAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.cast_item,
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int {
        return crew.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            actorView.text = crew[position].name
            characterView.text = crew[position].character
            imageLoader.displayImage("https://image.tmdb.org/t/p/w185${crew[position].profilePath}", actorImage)
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val actorView = view.actorView
        val actorImage = view.actorImage
        val characterView = view.characterView
    }
}