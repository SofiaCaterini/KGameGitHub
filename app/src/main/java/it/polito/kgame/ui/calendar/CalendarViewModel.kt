package it.polito.kgame.ui.calendar

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
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
import kotlin.collections.ArrayList

class CalendarViewModel : ViewModel() {

    private val _items= mutableListOf(
            EventDay( Calendar.getInstance()),
    )

    //////////////////////////////////////
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var appointmentListener: ListenerRegistration? = null
    private var areUpdatesBeenMade : Boolean = false

    private val _fbUser = MutableLiveData<FirebaseUser>()
    val fbUser: LiveData<FirebaseUser>
        get() = _fbUser

    private val _Appointments = MutableLiveData<MutableList<EventoInfo>>()
    val Appointments: MutableLiveData<MutableList<EventoInfo>>
        get() = _Appointments


    init {
        setUserAppointment()
    }

   private fun setUserAppointment() {
        areUpdatesBeenMade = false
        _fbUser.value = FirebaseAuth.getInstance().currentUser

        viewModelScope.launch {
            appointmentListener = DbManager.getUserAppointmentColl()?.addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val titoli = ArrayList<String>()
                val descrizioni = ArrayList<String>()
                val dat = ArrayList<String>()
                val luoghi = ArrayList<String>()
                val apps = ArrayList<EventoInfo>()
                val cals = ArrayList<Calendar>()

                for (doc in value!!) {

                    doc.getString("titolo")?.let {
                        titoli.add(it)
                    }
                    doc.getString("descrizione")?.let {
                        descrizioni.add(it)
                    }
                    doc.getString("luogo")?.let {
                        luoghi.add(it)
                    }

                    doc.getString("cal")?.let {
                        dat.add(it)
                    }

                    cals.clear()
                    for (i in 0 until titoli.size) {
                        cals.add(Calendar.getInstance())
                        cals[i].timeInMillis =dat[i].toLong()
                    }

                    apps.clear()
                    for (j in 0 until titoli.size) {
                        apps.add(EventoInfo(titoli[j],cals[j],descrizioni[j],luoghi[j]))
                    }


                }
                Log.d("date", dat.toString())
                Log.d("cals", cals.toString())
                Log.d("FAMMI SAPERE Appp", apps.toString())
                _Appointments.value = apps
                /*
                var apps = mutableMapOf<String, Any?>()
                for (doc in value!!) {
                    doc.getData()?.let{
                        apps = it
                    }

                    apps.add(doc.toObject<EventoInfo>())
                    Log.d("FAMMI SAPERE Appp", _Appointments.value.toString())
                }
                _Appointments.value?.forEach {
                    it.cal =
                } */
            }
        }
    }


    //chi conosce il viewmodel può vedere i dati
    private val _data: MutableLiveData<List<EventDay>> = MutableLiveData<List<EventDay>>().also {
        it.value = _items
    }

    val data: LiveData<List<EventDay>> = _data

/*
    fun changeTitle( titolo : String) {
        _thisAppointment.value?.titolo = titolo
        areUpdatesBeenMade = true
    }

    fun changeDescription(descr: String?) {
        _thisAppointment.value?.descrizione = descr
        areUpdatesBeenMade = true
    }

    fun saveUpdates(context: Context) {
        if (areUpdatesBeenMade) {
            _thisAppointment.value?.let { DbManager.updateAppointment(context, it) }
        }
        else {
            println("There were no updates to save")
        }
    }

    fun addEngagement(context: Context, app:EventoInfo, calendar: Calendar){
        _thisAppointment.value?.cal = app.cal
        _thisAppointment.value?.descrizione = app.descrizione
        _thisAppointment.value?.luogo = app.luogo
        _thisAppointment.value?.titolo = app.titolo
        _thisAppointment.value?.let {
            DbManager.createAppointment(context, it, calendar.timeInMillis)
        }
    }

    fun discardUpdates() {
        setUserAppointment()
    }
*/
}