package it.polito.kgame.ui.home

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import it.polito.kgame.*
import it.polito.kgame.R
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.ceil

class HomeViewModel : ViewModel() {
        private var weightListener: ListenerRegistration? = null

        private val _fbUser = MutableLiveData<FirebaseUser>()

        private val _thisUser = MutableLiveData<User>()
        val thisUser: LiveData<User>
                get() = _thisUser

        private val _thisUsersFam = MutableLiveData<Family>()
        val thisUsersFam: LiveData<Family>
                get() = _thisUsersFam

        private var _data: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
        val data : LiveData<List<User>>
                get() = _data

        private val _weights = MutableLiveData<MutableList<PesoInfo>>()
        val weights : MutableLiveData<MutableList<PesoInfo>>
                get() = _weights


        init {
                _fbUser.value = FirebaseAuth.getInstance().currentUser

                viewModelScope.launch {
                        DbManager.getUserDoc()?.addSnapshotListener { value, error ->
                                if (error != null) {
                                        println("ERROR retrieving user's doc")
                                }
                                if (value != null && value.exists()) {
                                        _thisUser.value = value.toObject<User>()!!
                                        //_thisUser.value!!.inGame = value[DbManager.INGAME] as Boolean
                                        println("FAMMI SAPERE 5 " +_thisUser.value + " / " + value[DbManager.INGAME])
                                        fillFamily()
                                }
                        }
                }


                setUserWeight()

        }

        private fun fillFamily() {
                viewModelScope.launch {
                        _thisUser.value?.familyCode?.let {
                                DbManager.getFamilyDoc(it)?.addSnapshotListener { value, error ->
                                        if (error != null) {
                                                println("ERROR retrieving family's doc")
                                        }
                                        if (value != null && value.exists()) {
                                                _thisUsersFam.value = Family(
                                                        it,
                                                        value[DbManager.FAM_NAME] as String?
                                                )
                                                addComps()
                                                _thisUsersFam.value?.lastMatchMillis = value[DbManager.MATCH_START_DATE] as Long?
                                                if(value[DbManager.MATCH_STATE]!=null) _thisUsersFam.value?.matchState = value[DbManager.MATCH_STATE] as String
                                                if(value[DbManager.PLAYERS_IN_GAME]!=null) _thisUsersFam.value?.playersInGame = value[DbManager.PLAYERS_IN_GAME].toString().toInt()
                                                println("value[players in game]: " + value[DbManager.PLAYERS_IN_GAME])
                                                _thisUsersFam.value?.lastWinnerMail = value[DbManager.LAST_WINNER] as String?
                                                println("capiamoci " + value)
                                                println("FAMMI SAPERE 2 " +_thisUsersFam.value)
                                        }
                                }
                        }
                }
        }

        private fun addComps() {
                viewModelScope.launch {
                        _thisUsersFam.value = Family(
                                _thisUsersFam.value?.code,
                                _thisUsersFam.value?.name,
                                _thisUsersFam.value?.code?.let { DbManager.getFamilyComps(it)},
                                _thisUsersFam.value?.lastMatchMillis,
                                _thisUsersFam.value?.matchState!!,
                                _thisUsersFam.value?.playersInGame!!,
                                _thisUsersFam.value?.lastWinnerMail
                        )

                       // _thisUsersFam.value?.code?.let {_thisUsersFam.value?.components = DbManager.getFamilyComps(it)}

//
//                        _thisUsersFam.value?.components = _thisUsersFam.value?.code?.let {
//                                println("vediamo un pochetto: " + DbManager.getFamilyComps(it))
//                                DbManager.getFamilyComps(it)
//                        }
//                }.invokeOnCompletion {println("vediamo un pochetto22: " + _thisUsersFam.value?.components)
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
                                        _weights.value = sessioni

                                }
                        }
                }
        }

        fun changePosition(context:Context, actualWeight: Float) {
                val bonusStreaks = arrayListOf(5,10,15,20,30) //if you do a streak of these length you will a bonus step  //5.1 10.1 15.2 20.2 30.3
                var x = 0
                if((_thisUser.value?.objective!! - actualWeight).absoluteValue <= (_thisUser.value?.objective!! - _weights.value?.get(_weights.value?.size!! - 2)?.peso!!).absoluteValue) {
                        x++
                        _thisUser.value?.goodWeightStreak = _thisUser.value?.goodWeightStreak!! + 1
                }
                else {
                        x--
                        _thisUser.value?.goodWeightStreak = 0
                }
                if(bonusStreaks.contains(_thisUser.value?.goodWeightStreak)) {
                        x += (_thisUser.value?.goodWeightStreak!!-1/ 10) + 1    //this is      __x += goodWeightStreak/10__       but the value is rounded by excess (5->1 / 10->1 / 15->2 / 20->2 / 30->3)
                }
                _thisUser.value?.position = _thisUser.value?.position!! + x
                
                println("this user " +_thisUser.value + " fattarelli " +_thisUser.value?.position!! +" / "+ x)
                DbManager.updateUser(context,_thisUser.value!!)
        }

        fun updateMatchState(context: Context) {
                _thisUsersFam.value?.let { DbManager.updateFamily(context, it) }
        }
        fun updatePlayerState(context: Context) {
                _thisUser.value?.let { DbManager.updateUser(context, it) }
        }

        private val _items= mutableListOf(
                ItemUsers( 1, "Raff", R.drawable.dog),
                ItemUsers( 2,"Liuk", R.drawable.lion),
                ItemUsers( 3,"Caterini", R.drawable.owl)

        )
//        //chi conosce il viewmodel può vedere i dati
//        private val _data: MutableLiveData<List<User>> = MutableLiveData<List<User>>().also {
//                println("non mi ricordo se funzionava: " + _thisUsersFam.value?.components)
//            it.value = _thisUsersFam.value?.components
//        }
//
//        val data: LiveData<List<User>> = _data

        /*fun addItem(nome: String){
            val item = ItemUsers(nome)
            _items.add(item)
            _data.value= _items
        }*/
    }