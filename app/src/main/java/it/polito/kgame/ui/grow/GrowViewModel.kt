package it.polito.kgame.ui.grow

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import it.polito.kgame.DbManager
import it.polito.kgame.EventoInfo
import it.polito.kgame.PesoInfo
import it.polito.kgame.User
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class GrowViewModel : ViewModel() {

    val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var weightListener: ListenerRegistration? = null

    private val _thisUser = MutableLiveData<User>()
    val thisUser: LiveData<User>
        get() = _thisUser

    private val _fbUser = MutableLiveData<FirebaseUser>()
    val fbUser: LiveData<FirebaseUser>
        get() = _fbUser

    private val _Weights = MutableLiveData<MutableList<PesoInfo>>()
    val Weights : MutableLiveData<MutableList<PesoInfo>>
        get() = _Weights


    init {

        _fbUser.value = FirebaseAuth.getInstance().currentUser

        setUserWeight()

        viewModelScope.launch {
            DbManager.getUserDoc()?.addSnapshotListener { value, error ->
                if (error != null) {
                    println("ERROR retrieving user's doc")
                }
                if (value != null && value.exists()) {
                    _thisUser.value = value.toObject<User>()!!
                    println("FAMMI SAPERE 1 " +_thisUser.value)
                }
            }
        }
    }

    private fun setUserWeight() {
        _fbUser.value = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            weightListener = DbManager.getUserWeightColl()?.addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(ContentValues.TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val pesi = ArrayList<String>()
                val datee = ArrayList<Long>()
                val sessioni = ArrayList<PesoInfo>()

                println("Sono nella collection")
                sessioni.clear()
                for (doc in value!!) {
                    if (doc!= null && doc.exists()) {
                        println("sono dentro al doc")

                        doc.getString("peso")?.let {
                            pesi.add(it)
                        }

                        doc.getLong("data")?.let {
                            datee.add(it)
                        }
                        sessioni.clear()

                        for (i in 0 until pesi.size) {
                            sessioni.add(PesoInfo(datee[i], pesi[i].toFloat()))
                        }

                    }
                    _Weights.value = sessioni

                }
            }
        }
    }

    fun changeObjective( obj: Double){
        _thisUser.value?.objective = obj
        _thisUser.value?.let { DbManager.updateUser(null, it) }
    }

}