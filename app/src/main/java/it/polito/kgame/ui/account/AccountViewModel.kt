package it.polito.kgame.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountViewModel : ViewModel() {
    private val _items= mutableListOf(
            ItemFamily( "Rossi"),
            ItemFamily( "Bianchi"),
            ItemFamily( "Caterini")

    )
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