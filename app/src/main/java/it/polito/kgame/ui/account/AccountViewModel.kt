package it.polito.kgame.ui.account

import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.kgame.R

class AccountViewModel : ViewModel() {
    private val _items= mutableListOf(
            ItemFamily( "Rossi"),
            ItemFamily( "Bianchi"),
            ItemFamily( "Caterini")
    )


    val db : FirebaseFirestore = FirebaseFirestore.getInstance()

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



}