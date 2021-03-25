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

        val selectedDayCal : Calendar = getInstance()
        val todayMillis = System.currentTimeMillis()
        selectedDayCal.timeInMillis = todayMillis

        selectedDayCal.set(selectedDayCal.get(Calendar.YEAR), selectedDayCal.get(Calendar.MONTH), selectedDayCal.get(Calendar.DAY_OF_MONTH), 23, 59)

        data.setText(refactorDate(selectedDayCal))

        datavalid = true

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            selectedDayCal.set(year, month, dayOfMonth, 23, 59)
            data.error = null

            if (todayMillis <= selectedDayCal.timeInMillis){
                data.setText(refactorDate(selectedDayCal))
                data.error = null
                datavalid = true
            }
            else {
                data.setText("")
                data.error = "Inserisci data corretta"
                datavalid = false
            }

        }

        val eventDateCal : Calendar = getInstance()
        impegno.setOnClickListener {
            creare.isVisible = true
            calendarView.isClickable = false
            calendarView.isEnabled = false
            calendarView.isVisible = false
            luogo.setText("")
            descrizione.setText("")
            titolo.setText("")
            orainizio.setText("")
            orainizio.error = null

            orainizio.doAfterTextChanged {

                if (!it.isNullOrBlank() && it.contains(":") && !it.endsWith(":")){

                    val ore = orainizio.text.toString().split(":")[0]
                    val minuti =  orainizio.text.toString().split(":")[1]
                    val todayCal : Calendar = getInstance()
                    todayCal.timeInMillis = todayMillis

                    eventDateCal.set(
                            selectedDayCal.get(Calendar.YEAR),
                            selectedDayCal.get(Calendar.MONTH),
                            selectedDayCal.get(Calendar.DAY_OF_MONTH),
                            ore.toInt(), minuti.toInt())

                    Log.d("millisevento", eventDateCal.timeInMillis.toString())
                    if ((selectedDayCal.get(Calendar.YEAR)>todayCal.get(Calendar.YEAR))
                            || (selectedDayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                                    selectedDayCal.get(Calendar.MONTH) > todayCal.get(Calendar.MONTH))
                            || (selectedDayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                                    selectedDayCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                                    selectedDayCal.get(Calendar.DAY_OF_MONTH) > todayCal.get(Calendar.DAY_OF_MONTH)) ) {
                        //se data dopo oggi
                        //controllare se formato corretto
                        Log.d("tag", "datadopooggi")
                        orainiziovalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59
                        if (!orainiziovalid) {orainizio.error = "Inserisci orario corretto"}
                    } else {
                        //controllare formato corretto
                        Log.d("tag", "dataoggi")
                        orainiziovalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59 && ((ore.toInt() > todayCal.get(Calendar.HOUR_OF_DAY))
                                || (((ore.toInt() == todayCal.get(Calendar.HOUR_OF_DAY)) && minuti.toInt() > todayCal.get(Calendar.MINUTE))))
                        //controllo con ora e minuti correnti

                        if (!orainiziovalid) {orainizio.error = "Inserisci orario corretto"}
                        //orainiziovalid = false
                    }

                } else {
                    orainiziovalid = false
                    orainizio.error = "Inserisci formato hh:mm"
                }
            }

            data.doAfterTextChanged {

                if (!it.isNullOrBlank() && slashesAreRight(it)) {
                    val giorno = data.text.toString().split("/")[0]
                    val mese =  data.text.toString().split("/")[1]
                    val anno =  data.text.toString().split("/")[2]
                    selectedDayCal.set(anno.toInt(), mese.toInt(), giorno.toInt())
                    val todayCal2 : Calendar = getInstance()
                    todayCal2.timeInMillis = todayMillis

                    Log.d("millisevento", eventDateCal.timeInMillis.toString())
                    if ((anno.toInt()>todayCal2.get(Calendar.YEAR)) || (anno.toInt() == todayCal2.get(Calendar.YEAR) && mese.toInt() > todayCal2.get(Calendar.MONTH))
                            || (anno.toInt() == todayCal2.get(Calendar.YEAR) && mese.toInt() == todayCal2.get(Calendar.MONTH)
                                    && giorno.toInt() >= todayCal2.get(Calendar.DAY_OF_MONTH)) ){
                        //se data dopo o uguale a oggi
                        //controllare se formato corretto
                        Log.d("tag", "datadopooggi")
                        ////////////////////////////////////////////////////////////////////
                        datavalid = true
                        //datavalid = giorno.toInt() in 1..31 && mese.toInt() in 1..12
                        ////////////////////////////////////////////////////////////////////
                        if (datavalid == false) {data.error = "Inserisci data corretta"}
                        if (datavalid) {eventDateCal.set(anno.toInt(), mese.toInt(), giorno.toInt())}
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
                println("annulla")
                Log.d("myTag", "annulla")
            }


            evok.setOnClickListener {
                println("ok")
                Log.d("millisevento", eventDateCal.timeInMillis.toString())
                if ((titolo.text.toString().isNotEmpty() && luogo.text.toString().isNotEmpty()
                            && descrizione.text.toString().isNotEmpty()) && orainiziovalid && datavalid
                ) {
                    Log.d("myTag", "campi giusti")
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, titolo.text.toString())
                        putExtra(CalendarContract.Events.EVENT_LOCATION, luogo.text.toString())
                        putExtra(CalendarContract.Events.DESCRIPTION, descrizione.text.toString())
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventDateCal.timeInMillis)
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

    private fun slashesAreRight(chars: CharSequence): Boolean {
        val slash = '/'
        return  chars.count{it == slash} == 2 &&        //ci sono 2 slash
                !chars.startsWith(slash) && !chars.endsWith(slash) &&       //non inizia nè finisce con lo slash
                chars.substring(chars.indexOf(slash), chars.lastIndexOf(slash)).isNotBlank()    //gli slash non sono consecutivi
    }

    private fun refactorDate(cal: Calendar): String {

        val day : String = if(cal.get(Calendar.DAY_OF_MONTH)>=10) cal.get(Calendar.DAY_OF_MONTH).toString()
                else  "0" + cal.get(Calendar.DAY_OF_MONTH)
        val month : String = if (cal.get(Calendar.MONTH)>=10) cal.get((Calendar.MONTH)+1).toString()
                else "0"+ (cal.get(Calendar.MONTH)+1)

        return day + "/" + month + "/" + cal.get(Calendar.YEAR)
    }

}