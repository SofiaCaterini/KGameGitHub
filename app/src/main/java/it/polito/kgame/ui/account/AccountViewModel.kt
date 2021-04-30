package it.polito.kgame.ui.account

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.Toast
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
import it.polito.kgame.Family
import it.polito.kgame.R
import it.polito.kgame.User
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {
    private val _items= mutableListOf(
        User("22", username = "pippo"),
    )
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userListener: ListenerRegistration? = null
    private var areUpdatesBeenMade : Boolean = false

    private val _fbUser = MutableLiveData<FirebaseUser>()
    val fbUser: LiveData<FirebaseUser>
        get() = _fbUser

    private val _thisUsersFam = MutableLiveData<Family>()
    val thisUsersFam: LiveData<Family>
        get() = _thisUsersFam

    private val _thisUser = MutableLiveData<User>()
    val thisUser: LiveData<User>
        get() = _thisUser


    init {
        setUser()

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

    private fun setUser() {
        areUpdatesBeenMade = false
        _fbUser.value = FirebaseAuth.getInstance().currentUser

        viewModelScope.launch {
//            _thisUser.value = DbManager.getUser()
//            println("FAMMI SAPERE" +_thisUser.value)
            userListener = DbManager.getUserDoc()?.addSnapshotListener { value, error ->
                if (error != null) {
                    println("ERRORREEEEEE")
                }
                if (value != null && value.exists()) {
                    _thisUser.value = value.toObject<User>()!!
                    println("FAMMI SAPERE" +_thisUser.value)
                }
            }
        }
    }

    //chi conosce il viewmodel può vedere i dati
    private val _data: MutableLiveData<List<User>> = MutableLiveData<List<User>>().also {
        it.value = _items
    }


    val data: LiveData<List<User>> = _data


    fun changePawn(pawnCode: Int) {
        _thisUser.value?.pawnCode = pawnCode
        areUpdatesBeenMade = true
    }

    fun changeUsername( username : String) {
        _thisUser.value?.username = username
        areUpdatesBeenMade = true
    }

    fun changeImg(uri: String?) {
        _thisUser.value?.profileImg = uri
        areUpdatesBeenMade = true
    }

    fun saveUpdates(context: Context) {
        if (areUpdatesBeenMade) {
            _thisUser.value?.let { DbManager.updateUser(context, it) }
        }
        else {
            println("There were no updates to save")
        }
    }

    fun discardUpdates() {
        setUser()
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


}