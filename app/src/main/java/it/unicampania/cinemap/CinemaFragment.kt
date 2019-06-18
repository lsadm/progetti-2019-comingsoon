package it.unicampania.cinemap


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.unicampania.imdbapi.Cinema
import it.unicampania.imdbapi.IMDBApi
import it.unicampania.util.onIO
import it.unicampania.util.onMain
import it.unicampania.util.snackBarMessage
import it.unicampania.util.snackBarMessageIf
import kotlinx.android.synthetic.main.fragment_cinema.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class CinemaFragment : Fragment() {
    var cinemaArrayList = ArrayList<Cinema>()
    private lateinit var sharedPref: SharedPreferences
    var mainJob: Job = Job()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cinema, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar!!.title = "Cinema"
        mainJob = GlobalScope.launch {
            sharedPref = activity!!.getSharedPreferences("CAP", Context.MODE_PRIVATE)
            capEditText.setText(sharedPref.getString("CAP", ""))
            capEditText.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    capEditText.clearFocus()
                    if (capEditText.text.toString().isEmpty()) {
                        snackBarMessage { "Il Cap è Obbligatorio" }
                    } else if (!capEditText.text.toString().matches(Regex("^[0-9]{5}"))) {
                        snackBarMessage { "Inserire un numero di 5 cifre" }
                    } else {
                        sharedPref.edit().putString("CAP", capEditText.text.toString()).apply()
                        spinKit.visibility = View.VISIBLE
                        capEditText.isEnabled = false
                        cinemaArrayList.clear()
                        GlobalScope.launch {
                            cinemaArrayList.addAll(
                                onIO {
                                    IMDBApi.getCinemas(
                                        sharedPref.getString(
                                            "CAP",
                                            "81030"
                                        )!!
                                    )
                                })
                            onMain {
                                cinemaList.adapter!!.notifyDataSetChanged()
                                spinKit.visibility = View.GONE
                                capEditText.isEnabled = true
                            }
                        }
                    }
                }
                true
            }
            onMain {
                spinKit.visibility = View.VISIBLE
                capEditText.isEnabled = false
            }

            cinemaArrayList = onIO {
                IMDBApi.getCinemas(
                    sharedPref.getString(
                        "CAP",
                        "81030"
                    )!!
                )
            }


            snackBarMessageIf(cinemaArrayList.isEmpty()) {
                "Nessun cinema trovato vicino a ${activity!!.getSharedPreferences(
                    "CAP",
                    Context.MODE_PRIVATE
                ).getString("CAP", "81030")!!}"
            }
            onMain {
                cinemaList.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
                cinemaList.adapter = CinemaAdapter(cinemaArrayList, this@CinemaFragment, view.context)
                spinKit.visibility = View.GONE
                capEditText.isEnabled = true
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mainJob.cancel()
    }
}
