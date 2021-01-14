package it.polito.kgame.ui.grow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GrowViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is grow Fragment"
    }
    val text: LiveData<String> = _text
}