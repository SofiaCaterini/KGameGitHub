package it.polito.kgame.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.kgame.Item

class AccountViewModel : ViewModel() {
    private val _items= mutableListOf(
            Item( "Rossi"),
            Item( "Bianchi"),
            Item( "Caterini")

    )
    //chi conosce il viewmodel può vedere i dati
    private val _data: MutableLiveData<List<Item>> = MutableLiveData<List<Item>>().also {
        it.value = _items
    }

    val data: LiveData<List<Item>> = _data

    fun addItem(nome: String){
        val item = Item(nome)
        _items.add(item)
        _data.value= _items
    }
}