package it.polito.kgame.ui.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import it.polito.kgame.R
import it.polito.kgame.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.event_form.*
import kotlinx.android.synthetic.main.fragment_calendar.*

class CalendarFragment : Fragment(R.layout.fragment_calendar) {


    val calendarViewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_calendar)
        creare.isVisible = false
        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        impegno.setOnClickListener {
            creare.isVisible = true
            annulla.setOnClickListener {
                creare.isVisible = false
                view.hideKeyboard()
                print("annulla")
                Log.d("myTag", "annulla")
            }
            evok.setOnClickListener {
                print("ok")
                if (titolo.text.toString().isNotEmpty() && luogo.text.toString().isNotEmpty()
                        && descrizione.text.toString().isNotEmpty()) {
                    print("Campi giusti")
                    Log.d("myTag", "campi giusti")
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, titolo.text.toString())
                        putExtra(CalendarContract.Events.EVENT_LOCATION, luogo.text.toString())
                        putExtra(CalendarContract.Events.DESCRIPTION, descrizione.text.toString())
                    }
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        print("ok")
                        Log.d("myTag", "ok")
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "There is no app that can support this action",
                                Toast.LENGTH_SHORT).show()
                        print("No app")
                    }


                } else {
                    Toast.makeText(requireContext(), "Please fill all the fields",
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}