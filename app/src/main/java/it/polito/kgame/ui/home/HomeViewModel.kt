package it.polito.kgame.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.kgame.R

class HomeViewModel : ViewModel() {
        private val _items= mutableListOf(
                ItemUsers( 1, "Raff", R.drawable.dog),
                ItemUsers( 2,"Liuk", R.drawable.lion),
                ItemUsers( 3,"Caterini", R.drawable.owl)

        )
        //chi conosce il viewmodel può vedere i dati
        private val _data: MutableLiveData<List<ItemUsers>> = MutableLiveData<List<ItemUsers>>().also {
            it.value = _items
        }

        val data: LiveData<List<ItemUsers>> = _data

        /*fun addItem(nome: String){
            val item = ItemUsers(nome)
            _items.add(item)
            _data.value= _items
        }*/
    }