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

    //////////////////////////////////////
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var appointmentListener: ListenerRegistration? = null
    private var areUpdatesBeenMade : Boolean = false

    private val _fbUser = MutableLiveData<FirebaseUser>()
    val fbUser: LiveData<FirebaseUser>
        get() = _fbUser

    private val _Appointments = MutableLiveData<List<EventoInfo>>()
    val Appointments : MutableLiveData<List<EventoInfo>>
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
                val dat = ArrayList<Long>()
                val luoghi = ArrayList<String>()
                val apps = ArrayList<EventoInfo>()
                val cals = ArrayList<Calendar>()
                apps.clear()
                for (doc in value!!) {
                    if (doc!= null && doc.exists()) {
                        doc.getString("titolo")?.let {
                            titoli.add(it)
                        }
                        doc.getString("descrizione")?.let {
                            descrizioni.add(it)
                        }
                        doc.getString("luogo")?.let {
                            luoghi.add(it)
                        }

                        doc.getLong("calendar")?.let {
                            dat.add(it)
                        }


                        cals.clear()
                        apps.clear()
                        for (i in 0 until titoli.size) {
                            cals.add(Calendar.getInstance())
                            cals[i].timeInMillis = dat[i].toLong()
                            apps.add(EventoInfo(titoli[i], cals[i], descrizioni[i], luoghi[i]))
                        }

                    }
                    _Appointments.value = apps as MutableList<EventoInfo>

                }
            }
        }
    }

}