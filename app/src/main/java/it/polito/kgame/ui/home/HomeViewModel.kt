package it.polito.kgame.ui.home

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.toObject
import it.polito.kgame.*
import it.polito.kgame.R
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

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