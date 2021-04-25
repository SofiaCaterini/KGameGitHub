package it.polito.kgame

import com.applandeo.materialcalendarview.EventDay
import java.util.*

data class EventoInfo(
        var titolo: String? = null,
        var cal: Calendar? = null,
        var descrizione: String? = null,
        var luogo: String? = null,
)
