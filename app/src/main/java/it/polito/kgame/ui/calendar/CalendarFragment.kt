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
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import it.polito.kgame.R
import it.polito.kgame.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.event_form.*
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.impegno_form.*


class CalendarFragment : Fragment(R.layout.fragment_calendar) {


    val calendarViewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_calendar)


        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        /////////////////CREAZIONE IMPEGNO
        creareimp.isVisible = false
        var orainiziovalid : Boolean = false
        var dataIsValid : Boolean = false

        val selectedDayCal : java.util.Calendar = java.util.Calendar.getInstance()
        val todayMillis = System.currentTimeMillis()
        selectedDayCal.timeInMillis = todayMillis

        selectedDayCal.set(
                selectedDayCal.get(Calendar.YEAR),
                selectedDayCal.get(Calendar.MONTH),
                selectedDayCal.get(
                        Calendar.DAY_OF_MONTH
                ),
                23,
                59
        )

        datai.setText(refactorDate(selectedDayCal))

        dataIsValid = true


        //var creazione evento
        creareeve.isVisible = false
        var orainizioevalid : Boolean = false
        var dataeIsValid : Boolean = false

        datae.setText(refactorDate(selectedDayCal))

        dataeIsValid = true


        val mEventDays: MutableList<EventDay> = ArrayList()

        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                selectedDayCal.set(
                        eventDay.calendar.get(Calendar.YEAR),
                        eventDay.calendar.get(Calendar.MONTH),
                        eventDay.calendar.get(
                                Calendar.DAY_OF_MONTH
                        ),
                        23,
                        59
                )
                datai.error = null
                datae.error = null


                if (todayMillis <= selectedDayCal.timeInMillis) {
                    datai.setText(refactorDate(selectedDayCal))
                    datai.error = null
                    dataIsValid = true

                    datae.setText(refactorDate(selectedDayCal))
                    datae.error = null
                    dataeIsValid = true
                } else {
                    datai.setText("")
                    datai.error = "Inserisci data corretta"
                    dataIsValid = false

                    datae.setText("")
                    datae.error = "Inserisci data corretta"
                    dataeIsValid = false
                }
                Log.d("eventi", mEventDays.toString())

            }


        })



        /*calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            selectedDayCal.set(year, month , dayOfMonth, 23, 59)
            datai.error = null
            datae.error = null


            if (todayMillis <= selectedDayCal.timeInMillis) {
                datai.setText(refactorDate(selectedDayCal))
                datai.error = null
                dataIsValid = true

                datae.setText(refactorDate(selectedDayCal))
                datae.error = null
                dataeIsValid = true
            }
            else {
                datai.setText("")
                datai.error = "Inserisci data corretta"
                dataIsValid = false

                datae.setText("")
                datae.error = "Inserisci data corretta"
                dataeIsValid = false
            }


        }*/

        val impegnoDateCal : java.util.Calendar = java.util.Calendar.getInstance()
        impegno.setOnClickListener {
            creareimp.isVisible = true
            calendarView.isClickable = false
            calendarView.isEnabled = false
            calendarView.isVisible = false
            luogoi.setText("")
            descrizionei.setText("")
            titoloi.setText("")
            orainizioi.setText("")
            orainizioi.error = null

            orainizioi.doAfterTextChanged {

                if (!it.isNullOrBlank() && it.contains(":") && !it.endsWith(":") && it.count { x -> x == ':'} ==1) {

                    val ore = orainizioi.text.toString().split(":")[0]
                    val minuti =  orainizioi.text.toString().split(":")[1]
                    val todayCal : Calendar = getInstance()
                    todayCal.timeInMillis = todayMillis

                    impegnoDateCal.set(
                            selectedDayCal.get(Calendar.YEAR),
                            selectedDayCal.get(Calendar.MONTH),
                            selectedDayCal.get(Calendar.DAY_OF_MONTH),
                            ore.toInt(), minuti.toInt()
                    )

                    Log.d("millisevento", impegnoDateCal.timeInMillis.toString())
                    if ((selectedDayCal.get(Calendar.YEAR)>todayCal.get(Calendar.YEAR))
                            || (selectedDayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                                    selectedDayCal.get(Calendar.MONTH) > todayCal.get(Calendar.MONTH))
                            || (selectedDayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                                    selectedDayCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                                    selectedDayCal.get(Calendar.DAY_OF_MONTH) > todayCal.get(
                                    Calendar.DAY_OF_MONTH
                            )) ) {
                        //se data dopo oggi
                        //controllare se formato corretto
                        Log.d("tag", "datadopooggi")
                        orainiziovalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59
                        if (!orainiziovalid) {orainizioi.error = "Inserisci orario corretto"}
                    } else {
                        //controllare formato corretto
                        Log.d("tag", "dataoggi")
                        orainiziovalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59 && ((ore.toInt() > todayCal.get(
                                Calendar.HOUR_OF_DAY
                        ))
                                || (((ore.toInt() == todayCal.get(Calendar.HOUR_OF_DAY)) && minuti.toInt() > todayCal.get(
                                Calendar.MINUTE
                        ))))
                        //controllo con ora e minuti correnti

                        if (!orainiziovalid) {orainizioi.error = "Inserisci orario corretto"}
                        //orainiziovalid = false
                    }

                } else {
                    orainiziovalid = false
                    orainizioi.error = "Inserisci formato hh:mm"
                }
            }

            datai.doAfterTextChanged {

                if (!it.isNullOrBlank() && slashesAreRight(it) && it.length in 8..10) {        //formato corretto. Controllare data
                    val giorno = datai.text.toString().split("/")[0]
                    val mese =  datai.text.toString().split("/")[1]
                    val anno =  datai.text.toString().split("/")[2]
                    selectedDayCal.set(anno.toInt(), mese.toInt() - 1, giorno.toInt(), 23, 59)
                    val todayCal2 : Calendar = getInstance()
                    todayCal2.timeInMillis = todayMillis

                    Log.d("millisevento", impegnoDateCal.timeInMillis.toString())
//                    if ((anno.toInt()>todayCal2.get(Calendar.YEAR))
//                            || (anno.toInt() == todayCal2.get(Calendar.YEAR) && mese.toInt() -1 > todayCal2.get(Calendar.MONTH))
//                            || (anno.toInt() == todayCal2.get(Calendar.YEAR) && mese.toInt() -1 == todayCal2.get(Calendar.MONTH)
//                                    && giorno.toInt() >= todayCal2.get(Calendar.DAY_OF_MONTH)) ){
                    if(selectedDayCal.timeInMillis >= System.currentTimeMillis()) {
                        //se data dopo o uguale a oggi
                        //controllare se formato corretto
                        Log.d("tag", "datadopooggi")
                        //se inserisco 31 febbraio e faccio la conversione dei suoi millis ottengo 02 marzo
                        dataIsValid = ((anno.toInt() == selectedDayCal.get(Calendar.YEAR)) && (mese.toInt() -1 == selectedDayCal.get(
                                Calendar.MONTH
                        )) &&
                                (giorno.toInt() == selectedDayCal.get(Calendar.DAY_OF_MONTH)))


                        //datavalid = data.text.toString() == refactorDate((selectedDayCal))
                        //Log.d("datascritta", data.text.toString())
                        //Log.d("datadaimillis", refactorDate(selectedDayCal) )
                        Log.d("datascritta", datai.text.toString())
                        Log.d("giornomillis", selectedDayCal.get(Calendar.DAY_OF_MONTH).toString())
                        Log.d("mesemillis", selectedDayCal.get(Calendar.MONTH).toString())
                        Log.d("annomillis", selectedDayCal.get(Calendar.YEAR).toString())

                        ////////////////////////////////////////////////////////////////////
                        if (!dataIsValid) {datai.error = "Inserisci data corretta"}
                        if (dataIsValid) {impegnoDateCal.set(
                                anno.toInt(),
                                mese.toInt(),
                                giorno.toInt()
                        )}
                    } else {
                        //data prima di oggi
                        //controllare formato corretto
                        dataIsValid = false
                        if (!dataIsValid) {datai.error = "Inserisci data corretta"}
                        //orainiziovalid = false
                    }

                } else {
                    dataIsValid = false
                    datai.error = "Inserisci formato dd/mm/yyyy"
                }

            }

            annullai.setOnClickListener {
                creareimp.isVisible = false
                calendarView.isClickable = true
                calendarView.isEnabled = true
                calendarView.isVisible = true
                view.hideKeyboard()
                println("annulla")
                Log.d("myTag", "annulla")
                Log.d("datadaimillis", refactorDate(selectedDayCal))
            }


            evoki.setOnClickListener {
                println("ok")
                Log.d("millisevento", impegnoDateCal.timeInMillis.toString())
                if ((titoloi.text.toString().isNotEmpty() && luogoi.text.toString().isNotEmpty()
                            && descrizionei.text.toString().isNotEmpty()) && orainiziovalid && dataIsValid
                ) {
                    Log.d("myTag", "campi giusti")
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, titoloi.text.toString())
                        putExtra(CalendarContract.Events.EVENT_LOCATION, luogoi.text.toString())
                        putExtra(CalendarContract.Events.DESCRIPTION, descrizionei.text.toString())
                        putExtra(
                                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                impegnoDateCal.timeInMillis
                        )
                    }
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        Log.d("myTag", "ok")
                        requireActivity().startActivity(intent)

                        creareimp.isVisible = false
                        calendarView.isVisible = true
                        calendarView.isClickable = true
                        calendarView.isEnabled = true

                        mEventDays.add(EventDay(impegnoDateCal, R.drawable.blackicon))
                        calendarView.setEvents(mEventDays)


                    } else {
                        Toast.makeText(
                                requireContext(), "There is no app that can support this action",
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                            requireContext(), "Please fill all the fields",
                            Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }



        /////////////////////////CREAZIONE EVENTO FAMIGLIA



        val eventDateCal : java.util.Calendar = java.util.Calendar.getInstance()
        evento.setOnClickListener {
            creareeve.isVisible = true
            calendarView.isClickable = false
            calendarView.isEnabled = false
            calendarView.isVisible = false
            luogoe.setText("")
            descrizionee.setText("")
            titoloe.setText("")
            orainizioe.setText("")
            orainizioe.error = null

            orainizioe.doAfterTextChanged {

                if (!it.isNullOrBlank() && it.contains(":") && !it.endsWith(":") && it.count { x -> x == ':'} ==1) {

                    val ore = orainizioe.text.toString().split(":")[0]
                    val minuti =  orainizioe.text.toString().split(":")[1]
                    val todayCal : Calendar = getInstance()
                    todayCal.timeInMillis = todayMillis

                    eventDateCal.set(
                            selectedDayCal.get(Calendar.YEAR),
                            selectedDayCal.get(Calendar.MONTH),
                            selectedDayCal.get(Calendar.DAY_OF_MONTH),
                            ore.toInt(), minuti.toInt()
                    )

                    Log.d("millisevento", eventDateCal.timeInMillis.toString())
                    if ((selectedDayCal.get(Calendar.YEAR)>todayCal.get(Calendar.YEAR))
                            || (selectedDayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                                    selectedDayCal.get(Calendar.MONTH) > todayCal.get(Calendar.MONTH))
                            || (selectedDayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                                    selectedDayCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                                    selectedDayCal.get(Calendar.DAY_OF_MONTH) > todayCal.get(
                                    Calendar.DAY_OF_MONTH
                            )) ) {
                        //se data dopo oggi
                        //controllare se formato corretto
                        Log.d("tag", "datadopooggi")
                        orainizioevalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59
                        if (!orainizioevalid) {orainizioe.error = "Inserisci orario corretto"}
                    } else {
                        //controllare formato corretto
                        Log.d("tag", "dataoggi")
                        orainizioevalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59 && ((ore.toInt() > todayCal.get(
                                Calendar.HOUR_OF_DAY
                        ))
                                || (((ore.toInt() == todayCal.get(Calendar.HOUR_OF_DAY)) && minuti.toInt() > todayCal.get(
                                Calendar.MINUTE
                        ))))
                        //controllo con ora e minuti correnti

                        if (!orainizioevalid) {orainizioe.error = "Inserisci orario corretto"}
                        //orainiziovalid = false
                    }

                } else {
                    orainizioevalid = false
                    orainizioe.error = "Inserisci formato hh:mm"
                }
            }

            datae.doAfterTextChanged {

                if (!it.isNullOrBlank() && slashesAreRight(it) && it.length in 8..10) {        //formato corretto. Controllare data
                    val giorno = datae.text.toString().split("/")[0]
                    val mese =  datae.text.toString().split("/")[1]
                    val anno =  datae.text.toString().split("/")[2]
                    selectedDayCal.set(anno.toInt(), mese.toInt() - 1, giorno.toInt(), 23, 59)
                    val todayCal2 : Calendar = getInstance()
                    todayCal2.timeInMillis = todayMillis

                    Log.d("millisevento", eventDateCal.timeInMillis.toString())
//                    if ((anno.toInt()>todayCal2.get(Calendar.YEAR))
//                            || (anno.toInt() == todayCal2.get(Calendar.YEAR) && mese.toInt() -1 > todayCal2.get(Calendar.MONTH))
//                            || (anno.toInt() == todayCal2.get(Calendar.YEAR) && mese.toInt() -1 == todayCal2.get(Calendar.MONTH)
//                                    && giorno.toInt() >= todayCal2.get(Calendar.DAY_OF_MONTH)) ){
                    if(selectedDayCal.timeInMillis >= System.currentTimeMillis()) {
                        //se data dopo o uguale a oggi
                        //controllare se formato corretto
                        Log.d("tag", "datadopooggi")
                        //se inserisco 31 febbraio e faccio la conversione dei suoi millis ottengo 02 marzo
                        dataeIsValid = ((anno.toInt() == selectedDayCal.get(Calendar.YEAR)) && (mese.toInt() -1 == selectedDayCal.get(
                                Calendar.MONTH
                        )) &&
                                (giorno.toInt() == selectedDayCal.get(Calendar.DAY_OF_MONTH)))


                        //datavalid = data.text.toString() == refactorDate((selectedDayCal))
                        //Log.d("datascritta", data.text.toString())
                        //Log.d("datadaimillis", refactorDate(selectedDayCal) )
                        Log.d("datascritta", datae.text.toString())
                        Log.d("giornomillis", selectedDayCal.get(Calendar.DAY_OF_MONTH).toString())
                        Log.d("mesemillis", selectedDayCal.get(Calendar.MONTH).toString())
                        Log.d("annomillis", selectedDayCal.get(Calendar.YEAR).toString())

                        ////////////////////////////////////////////////////////////////////
                        if (!dataeIsValid) {datae.error = "Inserisci data corretta"}
                        if (dataeIsValid) {eventDateCal.set(
                                anno.toInt(),
                                mese.toInt(),
                                giorno.toInt()
                        )}
                    } else {
                        //data prima di oggi
                        //controllare formato corretto
                        dataeIsValid = false
                        if (!dataeIsValid) {datae.error = "Inserisci data corretta"}
                        //orainiziovalid = false
                    }

                } else {
                    dataeIsValid = false
                    datae.error = "Inserisci formato dd/mm/yyyy"
                }

            }

            annullae.setOnClickListener {
                creareeve.isVisible = false
                calendarView.isClickable = true
                calendarView.isEnabled = true
                calendarView.isVisible = true
                view.hideKeyboard()
                println("annulla")
                Log.d("myTag", "annulla")
                Log.d("datadaimillis", refactorDate(selectedDayCal))
            }


            evoke.setOnClickListener {
                println("ok")
                Log.d("millisevento", eventDateCal.timeInMillis.toString())
                if ((titoloe.text.toString().isNotEmpty() && luogoe.text.toString().isNotEmpty()
                            && descrizionee.text.toString().isNotEmpty()) && orainizioevalid && dataeIsValid
                ) {
                    Log.d("myTag", "campi giusti")
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, titoloe.text.toString())
                        putExtra(CalendarContract.Events.EVENT_LOCATION, luogoe.text.toString())
                        putExtra(CalendarContract.Events.DESCRIPTION, descrizionee.text.toString())
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventDateCal.timeInMillis)
                        //condividere l'evento con tutte le persone della famiglia, si ricava la mail da ogni partecipante
                        //attenzione deve essere mail di google
                        putExtra(Intent.EXTRA_EMAIL, "pippo@coca.it")
                    }
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        Log.d("myTag", "ok")
                        requireActivity().startActivity(intent)
                        creareeve.isVisible = false
                        calendarView.isVisible = true
                        calendarView.isClickable = true
                        calendarView.isEnabled = true
                        mEventDays.add(EventDay(eventDateCal, R.drawable.blackicon))
                        calendarView.setEvents(mEventDays)
                    } else {
                        Toast.makeText(
                                requireContext(), "There is no app that can support this action",
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                            requireContext(), "Please fill all the fields",
                            Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }



    }

    private fun slashesAreRight(chars: CharSequence): Boolean {
        val slash = '/'
        return  chars.count{it == slash} == 2 &&        //ci sono 2 slash
                !chars.startsWith(slash) && !chars.endsWith(slash) &&       //non inizia nè finisce con lo slash
                chars.substring(chars.indexOf(slash) + 1, chars.lastIndexOf(slash)).isNotBlank()    //gli slash non sono consecutivi
    }

    private fun refactorDate(cal: java.util.Calendar): String {

        val day : String = if(cal.get(Calendar.DAY_OF_MONTH)>=10) cal.get(Calendar.DAY_OF_MONTH).toString()
                else  "0" + cal.get(Calendar.DAY_OF_MONTH)
        val month : String = if (cal.get(Calendar.MONTH)>=9) (cal.get(Calendar.MONTH)+1).toString()
                else "0"+ (cal.get(Calendar.MONTH)+1)

        return day + "/" + month + "/" + cal.get(Calendar.YEAR)
    }
}