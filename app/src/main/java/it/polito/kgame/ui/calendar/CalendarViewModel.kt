package it.polito.kgame.ui.calendar

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.EventDay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import it.polito.kgame.DbManager
import it.polito.kgame.EventoInfo
import it.polito.kgame.R
import it.polito.kgame.User
import it.polito.kgame.ui.home.ItemUsers
import kotlinx.coroutines.launch
import java.util.*

class CalendarViewModel : ViewModel() {

    private val _items= mutableListOf(
            EventDay( Calendar.getInstance()),
    )

    //////////////////////////////////////
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var engagementListener: ListenerRegistration? = null
    private var areUpdatesBeenMade : Boolean = false

    private val _fbUser = MutableLiveData<FirebaseUser>()
    val fbUser: LiveData<FirebaseUser>
        get() = _fbUser

    private val _thisEngagement = MutableLiveData<EventoInfo>()
    val thisEngagement: MutableLiveData<EventoInfo>
        get() = _thisEngagement


    init {
        setUserEngagement()
    }

    private fun setUserEngagement() {
        areUpdatesBeenMade = false
        _fbUser.value = FirebaseAuth.getInstance().currentUser

        viewModelScope.launch {
            engagementListener = DbManager.getUserEngagementDoc()?.addSnapshotListener { value, error ->
                if (error != null) {
                    println("ERRORREEEEEE")
                }
                if (value != null && value.exists()) {
                    _thisEngagement.value = value.toObject<EventoInfo>()!!
                    println("FAMMI SAPERE" +_thisEngagement.value)
                }
            }
        }
    }



    //chi conosce il viewmodel può vedere i dati
    private val _data: MutableLiveData<List<EventDay>> = MutableLiveData<List<EventDay>>().also {
        it.value = _items
    }

    val data: LiveData<List<EventDay>> = _data


    fun changeTitle( titolo : String) {
        _thisEngagement.value?.titolo = titolo
        areUpdatesBeenMade = true
    }

    fun changeDescription(descr: String?) {
        _thisEngagement.value?.descrizione = descr
        areUpdatesBeenMade = true
    }

    fun saveUpdates(context: Context) {
        if (areUpdatesBeenMade) {
            _thisEngagement.value?.let { DbManager.updateEngagement(context, it) }
        }
        else {
            println("There were no updates to save")
        }
    }

    fun addEngagement(context: Context, eng:EventoInfo, calendar: Calendar){
        _thisEngagement.value?.cal = eng.cal
        _thisEngagement.value?.descrizione = eng.descrizione
        _thisEngagement.value?.luogo = eng.luogo
        _thisEngagement.value?.titolo = eng.titolo
        _thisEngagement.value?.let {
            DbManager.createEngagement(context, it, calendar.timeInMillis)
        }
    }

    fun discardUpdates() {
        setUserEngagement()
    }

}