package it.polito.kgame.ui.home

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import it.polito.kgame.*
import it.polito.kgame.R
import kotlinx.coroutines.launch

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

        private val _We = MutableLiveData<MutableList<PesoInfo>>()
        val We : MutableLiveData<MutableList<PesoInfo>>
                get() = _We


        init {
                _fbUser.value = FirebaseAuth.getInstance().currentUser

                viewModelScope.launch {
                        DbManager.getUserDoc()?.addSnapshotListener { value, error ->
                                if (error != null) {
                                        println("ERROR retrieving user's doc")
                                }
                                if (value != null && value.exists()) {
                                        _thisUser.value = value.toObject<User>()!!
                                        println("FAMMI SAPERE 1 " +_thisUser.value)
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
                                _thisUsersFam.value?.code?.let { DbManager.getFamilyComps(it) }
                        )

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
                                        _We.value = sessioni

                                }
                        }
                }
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