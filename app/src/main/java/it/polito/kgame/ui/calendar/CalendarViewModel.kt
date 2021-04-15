package it.polito.kgame.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.applandeo.materialcalendarview.EventDay
import it.polito.kgame.R
import it.polito.kgame.ui.home.ItemUsers
import java.util.*

class CalendarViewModel : ViewModel() {

    private val _items= mutableListOf(
            EventDay( Calendar.getInstance()),


    )
    //chi conosce il viewmodel può vedere i dati
    private val _data: MutableLiveData<List<EventDay>> = MutableLiveData<List<EventDay>>().also {
        it.value = _items
    }

    val data: LiveData<List<EventDay>> = _data

}