package it.polito.kgame.ui.calendar

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.Calendar.getInstance
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.polito.kgame.R
import it.polito.kgame.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.event_form.*
import kotlinx.android.synthetic.main.fragment_calendar.*

class CalendarFragment : Fragment(R.layout.fragment_calendar) {


    val calendarViewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_calendar)
        creare.isVisible = false
        var orainiziovalid : Boolean = false
        var datavalid : Boolean = false

        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        val c : Calendar = Calendar.getInstance()
        val today = System.currentTimeMillis()
        c.timeInMillis = today
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 23, 59)
        var dataattuale : String
        dataattuale = if(c.get(Calendar.DAY_OF_MONTH)>=10) (""+c.get(Calendar.DAY_OF_MONTH)+"/"+
                (c.get(Calendar.MONTH)+1)+"/"+(c.get(Calendar.YEAR))) else ("0"+c.get(Calendar.DAY_OF_MONTH)+"/"+
                (c.get(Calendar.MONTH)+1)+"/"+(c.get(Calendar.YEAR)))
        dataattuale = if(c.get(Calendar.MONTH)>=10) (""+c.get(Calendar.DAY_OF_MONTH)+"/"+
        (c.get(Calendar.MONTH)+1)+"/"+(c.get(Calendar.YEAR))) else (""+c.get(Calendar.DAY_OF_MONTH)+"/0"+
        (c.get(Calendar.MONTH)+1)+"/"+(c.get(Calendar.YEAR)))

        data.setText(dataattuale)
        datavalid = true

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            var monthh: Int = month + 1
            dataattuale = if(dayOfMonth>=10) "$dayOfMonth/$monthh/$year" else "0$dayOfMonth/$monthh/$year"
            dataattuale = if(monthh>=10) "$dayOfMonth/$monthh/$year" else "$dayOfMonth/0$monthh/$year"
            c.set(year, month, dayOfMonth, 23, 59)
            Log.d("tagtoday", today.toString())
            Log.d("tagdatascelta", c.timeInMillis.toString())
            data.error = null
            if (today.toString() <= c.timeInMillis.toString()){
                data.setText(dataattuale)
                data.error = null
                datavalid = true
            }
            else {
                data.setText("")
                data.error = "Inserisci data corretta"
                datavalid = false
            }

        }
        var dtstartevento : Calendar = getInstance()
        impegno.setOnClickListener {
            creare.isVisible = true
            calendarView.isClickable = false
            calendarView.isEnabled = false
            calendarView.isVisible = false
            luogo.setText("")
            descrizione.setText("")
            titolo.setText("")
            orainizio.setText("")
            orainizio.error=null

            orainizio.doAfterTextChanged {
                var ore : String
                var minuti : String
                if (!it.isNullOrBlank() && it.contains(":") && it.length >= 4){
                    ore = orainizio.text.toString().split(":")[0]
                    minuti =  orainizio.text.toString().split(":")[1]
                    var o : Calendar = Calendar.getInstance()
                    o.timeInMillis = today
                    dtstartevento.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), ore.toInt(), minuti.toInt())
                    Log.d("millisevento", dtstartevento.timeInMillis.toString())
                    if ((c.get(Calendar.YEAR)>o.get(Calendar.YEAR)) || (c.get(Calendar.YEAR) == o.get(Calendar.YEAR) && c.get(Calendar.MONTH) > o.get(Calendar.MONTH))
                            || (c.get(Calendar.YEAR) == o.get(Calendar.YEAR) && c.get(Calendar.MONTH) == o.get(Calendar.MONTH) && c.get(Calendar.DAY_OF_MONTH) > o.get(Calendar.DAY_OF_MONTH)) ){
                        //se data dopo oggi
                        //controllare se formato corretto
                        Log.d("tag", "datadopooggi")
                        orainiziovalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59
                        if (orainiziovalid == false) {orainizio.error = "Inserisci orario corretto"}
                    } else {
                        //controllare formato corretto
                        Log.d("tag", "dataoggi")
                        orainiziovalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59 && ((ore.toInt() > o.get(Calendar.HOUR_OF_DAY))
                                || (((ore.toInt() == o.get(Calendar.HOUR_OF_DAY)) && minuti.toInt() > o.get(Calendar.MINUTE))))
                        //controllo con ora e minuti correnti

                        if (orainiziovalid == false) {orainizio.error = "Inserisci orario corretto"}
                        //orainiziovalid = false
                    }

                } else {
                    orainiziovalid = false
                    orainizio.error = "Inserisci formato hh:mm"
                }

            }

            data.doAfterTextChanged {
                var giorno : String
                var mese : String
                var anno : String

                if (!it.isNullOrBlank() && it.indexOfAny(charArrayOf('/')) >= 1 && !it.endsWith("/") && it.length ==10){
                    giorno = data.text.toString().split("/")[0]
                    mese =  data.text.toString().split("/")[1]
                    anno =  data.text.toString().split("/")[2]
                    c.set(anno.toInt(), mese.toInt(), giorno.toInt())
                    var o : Calendar = Calendar.getInstance()
                    o.timeInMillis = today

                    Log.d("millisevento", dtstartevento.timeInMillis.toString())
                    if ((anno.toInt()>o.get(Calendar.YEAR)) || (anno.toInt() == o.get(Calendar.YEAR) && mese.toInt() > o.get(Calendar.MONTH))
                            || (anno.toInt() == o.get(Calendar.YEAR) && mese.toInt() == o.get(Calendar.MONTH)
                                    && giorno.toInt() >= o.get(Calendar.DAY_OF_MONTH)) ){
                        //se data dopo o uguale a oggi
                        //controllare se formato corretto
                        Log.d("tag", "datadopooggi")
                        ////////////////////////////////////////////////////////////////////
                        datavalid = true
                        //datavalid = giorno.toInt() in 1..31 && mese.toInt() in 1..12
                        ////////////////////////////////////////////////////////////////////
                        if (datavalid == false) {data.error = "Inserisci data corretta"}
                        if (datavalid) {dtstartevento.set(anno.toInt(), mese.toInt(), giorno.toInt())}
                    } else {
                        //data prima di oggi
                        //controllare formato corretto
                        datavalid = false
                        if (datavalid == false) {data.error = "Inserisci data corretta"}
                        //orainiziovalid = false
                    }

                } else {
                    datavalid = false
                    data.error = "Inserisci formato dd/mm/yyyy"
                }

            }

            annulla.setOnClickListener {
                creare.isVisible = false
                calendarView.isClickable = true
                calendarView.isEnabled = true
                calendarView.isVisible = true
                view.hideKeyboard()
                print("annulla")
                Log.d("myTag", "annulla")
            }


            evok.setOnClickListener {
                print("ok")
                Log.d("millisevento", dtstartevento.timeInMillis.toString())
                if ((titolo.text.toString().isNotEmpty() && luogo.text.toString().isNotEmpty()
                            && descrizione.text.toString().isNotEmpty()) && orainiziovalid && datavalid
                ) {
                    Log.d("myTag", "campi giusti")
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, titolo.text.toString())
                        putExtra(CalendarContract.Events.EVENT_LOCATION, luogo.text.toString())
                        putExtra(CalendarContract.Events.DESCRIPTION, descrizione.text.toString())
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dtstartevento.timeInMillis)
                    }
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        Log.d("myTag", "ok")
                        requireActivity().startActivity(intent)
                        creare.isVisible = false
                        calendarView.isVisible = true
                        calendarView.isClickable = true
                        calendarView.isEnabled = true
                    } else {
                        Toast.makeText(requireContext(), "There is no app that can support this action",
                                Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(requireContext(), "Please fill all the fields",
                            Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

}