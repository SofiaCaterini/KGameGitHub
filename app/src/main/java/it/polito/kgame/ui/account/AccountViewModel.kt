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
import it.polito.kgame.R
import it.polito.kgame.User
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {
    private val _items= mutableListOf(
            ItemFamily( "Rossi"),
            ItemFamily( "Bianchi"),
            ItemFamily( "Caterini")
    )
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userListener: ListenerRegistration? = null
    private var areUpdatesBeenMade : Boolean = false

    private val _fbUser = MutableLiveData<FirebaseUser>()
    val fbUser: LiveData<FirebaseUser>
        get() = _fbUser

    private val _thisUser = MutableLiveData<User>()
    val thisUser: LiveData<User>
        get() = _thisUser


    init {
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
    private val _data: MutableLiveData<List<ItemFamily>> = MutableLiveData<List<ItemFamily>>().also {
        it.value = _items
    }


    val data: LiveData<List<ItemFamily>> = _data

    fun addItem(nome: String){
        val item = ItemFamily(nome)
        _items.add(item)
        _data.value= _items
    }

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

}