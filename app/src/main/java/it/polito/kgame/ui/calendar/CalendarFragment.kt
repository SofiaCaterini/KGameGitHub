package it.polito.kgame.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import it.polito.kgame.R
import it.polito.kgame.ui.home.HomeViewModel

class CalendarFragment : Fragment(R.layout.fragment_calendar) {


    val calendarViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {



    }
}