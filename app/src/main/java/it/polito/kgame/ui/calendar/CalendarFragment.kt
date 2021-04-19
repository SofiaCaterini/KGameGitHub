package it.polito.kgame.ui.calendar

import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.android.material.button.MaterialButton
import it.polito.kgame.DbManager
import it.polito.kgame.EventoInfo
import it.polito.kgame.R
import kotlinx.android.synthetic.main.anteprima_evento.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.event_form.*
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.impegno_form.*
import java.util.*
import java.util.Calendar.getInstance
import kotlin.collections.ArrayList


class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    //private val adapter = EventAdapter()
    private val calendarViewModel by activityViewModels<CalendarViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_calendar)

        val mImpegniDays: MutableList<EventDay> = ArrayList()
        val listaimpegni : MutableList<EventoInfo> = ArrayList()

        rvdettagli.isVisible = false
        ante.isVisible=false
        ante.y = 0F

        println("top: $ante.y")
        calendarViewModel.Appointments.observe(viewLifecycleOwner, Observer { appointment ->
            println("APPOINTMENTS: $appointment")
            println("napp: ${appointment.size}")
            mImpegniDays.clear()
            listaimpegni.clear()
            appointment.forEach {
                println("itInizio: $it")
                var x = java.util.Calendar.getInstance()
                x.timeInMillis = it.cal?.timeInMillis!!
                mImpegniDays.add(EventDay(x!!, R.drawable.blackicon))
                calendarView.setEvents(mImpegniDays)
                listaimpegni.add(it)
                println("listaimpegni: $listaimpegni")
                println("eventday: $mImpegniDays")
            }

            println("imp: $mImpegniDays")
            mImpegniDays.forEach {
                println("mimpdays: ${it.calendar.timeInMillis.toString()}")
            }
            println("numimp: ${mImpegniDays.size}")
            appointment.forEach {
                println("appore: ${it.cal?.get(java.util.Calendar.HOUR_OF_DAY)}")
            }


        })

        val dati = listaimpegni
        val adapter = EventAdapter(dati)
        rvdettagli.layoutManager= LinearLayoutManager(requireContext())
        rvdettagli.adapter = adapter



        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        /////////////////CREAZIONE IMPEGNO
        creareimp.isVisible = false
        ante.isVisible = false
        var orainiziovalid : Boolean = false
        var dataIsValid : Boolean = false
        val impegnoDateCal : java.util.Calendar = java.util.Calendar.getInstance()
        val selectedDayCal : java.util.Calendar = java.util.Calendar.getInstance()
        val todayMillis = System.currentTimeMillis()
        selectedDayCal.timeInMillis = todayMillis

        selectedDayCal.set(
                selectedDayCal.get(java.util.Calendar.YEAR),
                selectedDayCal.get(java.util.Calendar.MONTH),
                selectedDayCal.get(
                        java.util.Calendar.DAY_OF_MONTH
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
        val mImpEvDays: MutableList<EventDay> = ArrayList()

        val listaeventi : MutableList<EventoInfo> = ArrayList()


        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                rvdettagli.isVisible = false
                ante.y = 150F
                println("impact: $mImpegniDays")
                println("listaime: $listaimpegni")
                listaimpegni.forEach {
                    Log.d("ore",it.cal?.timeInMillis.toString())
                }
                mImpegniDays.forEach {
                    Log.d("oreimpdays",it.calendar?.timeInMillis.toString())
                }
                ante.isVisible = false
                buttonevent.text = "Visualizza dettagli"


                selectedDayCal.set(
                        eventDay.calendar.get(java.util.Calendar.YEAR),
                        eventDay.calendar.get(java.util.Calendar.MONTH),
                        eventDay.calendar.get(
                                java.util.Calendar.DAY_OF_MONTH
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

                listaeventi.forEach {
                    Log.d("listaev", it.cal?.timeInMillis.toString())
                }
                //se la data cliccata ha un impegno
                val ximp : EventDay? = mImpegniDays.find {
                    (it.calendar.get(java.util.Calendar.DAY_OF_MONTH) == selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH)
                            && it.calendar.get(java.util.Calendar.MONTH) == selectedDayCal.get(java.util.Calendar.MONTH)
                            && it.calendar.get(java.util.Calendar.YEAR) == selectedDayCal.get(java.util.Calendar.YEAR))
                }
                if (ximp != null) {
                    Log.d("tag", "Ha impegno")

                    val I : EventoInfo? = listaimpegni.find{
                        (it.cal?.get(java.util.Calendar.DAY_OF_MONTH)  == selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH)
                                && (it.cal?.get(java.util.Calendar.MONTH)!!) == selectedDayCal.get(java.util.Calendar.MONTH)
                                && it.cal?.get(java.util.Calendar.YEAR) == selectedDayCal.get(java.util.Calendar.YEAR))
                    }

                    val impegnidatacorrente : List<EventoInfo> = listaimpegni.filter{
                        (it.cal?.get(java.util.Calendar.DAY_OF_MONTH)  == selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH)
                                && (it.cal?.get(java.util.Calendar.MONTH)!!) == selectedDayCal.get(java.util.Calendar.MONTH)
                                && it.cal?.get(java.util.Calendar.YEAR) == selectedDayCal.get(java.util.Calendar.YEAR))
                    }


                    if (impegnidatacorrente.size > 1) {
                        buttonevent.text = getString(R.string.msganteprimanuovieventi)
                    }

                    if (I != null) {
                        lista_titolo.text = I.titolo
                        lista_ora.text = refactorTime(I.cal)
                    }
                    Log.d("seldatecal", selectedDayCal.get(java.util.Calendar.WEEK_OF_MONTH).toString())

                    when (selectedDayCal.get(java.util.Calendar.WEEK_OF_MONTH)) {

                        1 -> {
                            ante.translationY = 0F
                        }
                        2 -> {
                            ante.translationY = 150F
                        }
                        3 -> {
                            ante.translationY = 300F
                        }
                        4 -> {
                            ante.translationY = 450F
                        }
                        5 -> {
                            ante.translationY = 600F
                        }



                    }

                    ante.isVisible = true
                    close.setOnClickListener {
                        ante.isVisible = false
                    }

                    ante.setOnClickListener {
                        ante.isVisible = false
                        println("Dovrei visualizzare rv")
                        //val dati = listaimpegni
                        val adapter = EventAdapter(impegnidatacorrente)
                        rvdettagli.adapter = adapter
                        rvdettagli.isVisible = true
                        close.setOnClickListener {
                            ante.isVisible = false
                        }

                    }
                }
                else {
                    Log.d("tag", "Non ha impegno")
                }

                //se la data cliccata ha un evento
                val xeve : EventDay? = mEventDays.find {
                    (it.calendar.get(java.util.Calendar.DAY_OF_MONTH) == selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH)
                            && it.calendar.get(java.util.Calendar.MONTH) == selectedDayCal.get(java.util.Calendar.MONTH)
                            && it.calendar.get(java.util.Calendar.YEAR) == selectedDayCal.get(java.util.Calendar.YEAR))
                }
                if (xeve != null) {
                    Log.d("tag", "Ha evento")
                    ante.isVisible = true
                    val E: EventoInfo? = listaeventi.find {
                        (it.cal?.get(java.util.Calendar.DAY_OF_MONTH) == selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH)
                                && (it.cal?.get(java.util.Calendar.MONTH)!! - 1) == selectedDayCal.get(java.util.Calendar.MONTH)
                                && it.cal?.get(java.util.Calendar.YEAR) == selectedDayCal.get(java.util.Calendar.YEAR))
                    }
                    val Ep: Map<Boolean, Int> = (listaeventi.plus(listaimpegni)).groupingBy {
                        it.cal?.get(java.util.Calendar.DAY_OF_MONTH) == selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH)
                                && (it.cal?.get(java.util.Calendar.MONTH)!! - 1) == selectedDayCal.get(java.util.Calendar.MONTH)
                                && it.cal?.get(java.util.Calendar.YEAR) == selectedDayCal.get(java.util.Calendar.YEAR)
                    }
                            .eachCount().filter { it.value > 1 }
                    if (Ep.isNotEmpty()) {
                        buttonevent.text = getString(R.string.msganteprimanuovieventi)
                        buttonevent.setIconResource(R.drawable.ic_arrow_right)
                        buttonevent.iconGravity = MaterialButton.ICON_GRAVITY_END
                    }

                    Log.d("E", E?.cal?.timeInMillis.toString())
                    if (E != null) {
                        lista_titolo.text = E.titolo
                        lista_ora.text = refactorTime(E.cal)

                    }
                    close.setOnClickListener {
                        ante.isVisible = false
                    }
                }
                else {
                    Log.d("tag", "Non ha evento")
                }

            }


        })


        impegno.setOnClickListener {
            impegno.isClickable = false
            evento.isClickable = false
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
                    val todayCal : java.util.Calendar = java.util.Calendar.getInstance()
                    todayCal.timeInMillis = todayMillis

                    impegnoDateCal.set(
                            selectedDayCal.get(java.util.Calendar.YEAR),
                            selectedDayCal.get(java.util.Calendar.MONTH),
                            selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH),
                            ore.toInt(), minuti.toInt()
                    )

                    if ((selectedDayCal.get(java.util.Calendar.YEAR)>todayCal.get(java.util.Calendar.YEAR))
                            || (selectedDayCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                                    selectedDayCal.get(java.util.Calendar.MONTH) > todayCal.get(java.util.Calendar.MONTH))
                            || (selectedDayCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                                    selectedDayCal.get(java.util.Calendar.MONTH) == todayCal.get(java.util.Calendar.MONTH) &&
                                    selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH) > todayCal.get(
                                    java.util.Calendar.DAY_OF_MONTH
                            )) ) {
                        //se data dopo oggi
                        //controllare se formato corretto

                        orainiziovalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59
                        if (!orainiziovalid) {orainizioi.error = "Inserisci orario corretto"}
                    } else {
                        //controllare formato corretto

                        orainiziovalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59 && ((ore.toInt() > todayCal.get(
                                java.util.Calendar.HOUR_OF_DAY
                        ))
                                || (((ore.toInt() == todayCal.get(java.util.Calendar.HOUR_OF_DAY)) && minuti.toInt() > todayCal.get(
                                java.util.Calendar.MINUTE
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
                    val todayCal2 : java.util.Calendar = java.util.Calendar.getInstance()
                    todayCal2.timeInMillis = todayMillis

                    if(selectedDayCal.timeInMillis >= System.currentTimeMillis()) {
                        //se data dopo o uguale a oggi
                        //controllare se formato corretto

                        //se inserisco 31 febbraio e faccio la conversione dei suoi millis ottengo 02 marzo
                        dataIsValid = ((anno.toInt() == selectedDayCal.get(java.util.Calendar.YEAR)) && (mese.toInt() -1 == selectedDayCal.get(
                                java.util.Calendar.MONTH
                        )) &&
                                (giorno.toInt() == selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH)))

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
                impegno.isClickable = true
                evento.isClickable = true
                creareimp.isVisible = false
                calendarView.isClickable = true
                calendarView.isEnabled = true
                calendarView.isVisible = true
                view.hideKeyboard()

            }


            evoki.setOnClickListener {
                println("ok")
                impegno.isClickable = true
                evento.isClickable = true
                if ((titoloi.text.toString().isNotEmpty() && luogoi.text.toString().isNotEmpty()
                            && descrizionei.text.toString().isNotEmpty()) && orainiziovalid && dataIsValid
                ) {

                    //listaimpegni.add(nuovoimpegno)
                    //calendarViewModel.addEngagement(requireContext(),nuovoimpegno)
                    val dataprova2 : java.util.Calendar = java.util.Calendar.getInstance()

                    dataprova2.set(impegnoDateCal.get(java.util.Calendar.YEAR), impegnoDateCal.get(java.util.Calendar.MONTH),
                            impegnoDateCal.get(java.util.Calendar.DAY_OF_MONTH), impegnoDateCal.get(java.util.Calendar.HOUR),
                            impegnoDateCal.get(java.util.Calendar.MINUTE))

                    if (mEventDays.find {
                                (it.calendar.get(java.util.Calendar.DAY_OF_MONTH) == dataprova2.get(java.util.Calendar.DAY_OF_MONTH)
                                        && it.calendar.get(java.util.Calendar.MONTH) == dataprova2.get(java.util.Calendar.MONTH)
                                        && it.calendar.get(java.util.Calendar.YEAR) == dataprova2.get(java.util.Calendar.YEAR))
                            }!= null) {
                        mImpEvDays.add(EventDay(dataprova2, R.drawable.blackandgreenicon))
                    }
                    else {
                        val nuovoimpegno = EventoInfo(titoloi.text.toString(), impegnoDateCal , descrizionei.text.toString(),luogoi.text.toString())
                        DbManager.createAppointment(requireContext(), nuovoimpegno, System.currentTimeMillis())

                    }

                    //calendarView.setEvents(mImpEvDays.plus(mImpegniDays.plus(mEventDays)))


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

                        requireActivity().startActivity(intent)

                        creareimp.isVisible = false
                        calendarView.isVisible = true
                        calendarView.isClickable = true
                        calendarView.isEnabled = true

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
            impegno.isClickable = false
            evento.isClickable = false
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
                            selectedDayCal.get(java.util.Calendar.YEAR),
                            selectedDayCal.get(java.util.Calendar.MONTH),
                            selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH),
                            ore.toInt(), minuti.toInt()
                    )

                    if ((selectedDayCal.get(java.util.Calendar.YEAR)>todayCal.get(java.util.Calendar.YEAR))
                            || (selectedDayCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                                    selectedDayCal.get(java.util.Calendar.MONTH) > todayCal.get(java.util.Calendar.MONTH))
                            || (selectedDayCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                                    selectedDayCal.get(java.util.Calendar.MONTH) == todayCal.get(java.util.Calendar.MONTH) &&
                                    selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH) > todayCal.get(
                                    java.util.Calendar.DAY_OF_MONTH
                            )) ) {
                        //se data dopo oggi
                        //controllare se formato corretto
                        orainizioevalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59
                        if (!orainizioevalid) {orainizioe.error = "Inserisci orario corretto"}
                    } else {
                        //controllare formato corretto
                        orainizioevalid = ore.toInt() in 0..23 && minuti.toInt() in 0..59 && ((ore.toInt() > todayCal.get(
                                java.util.Calendar.HOUR_OF_DAY
                        ))
                                || (((ore.toInt() == todayCal.get(java.util.Calendar.HOUR_OF_DAY)) && minuti.toInt() > todayCal.get(
                                java.util.Calendar.MINUTE
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
                    val todayCal2 : java.util.Calendar = java.util.Calendar.getInstance()
                    todayCal2.timeInMillis = todayMillis

                    if(selectedDayCal.timeInMillis >= System.currentTimeMillis()) {
                        //se data dopo o uguale a oggi
                        //controllare se formato corretto

                        //se inserisco 31 febbraio e faccio la conversione dei suoi millis ottengo 02 marzo
                        dataeIsValid = ((anno.toInt() == selectedDayCal.get(java.util.Calendar.YEAR)) && (mese.toInt() -1 == selectedDayCal.get(
                                java.util.Calendar.MONTH
                        )) &&
                                (giorno.toInt() == selectedDayCal.get(java.util.Calendar.DAY_OF_MONTH)))

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
                evento.isClickable = true
                impegno.isClickable = true
                creareeve.isVisible = false
                calendarView.isClickable = true
                calendarView.isEnabled = true
                calendarView.isVisible = true
                view.hideKeyboard()
                println("annulla")

            }


            evoke.setOnClickListener {
                evento.isClickable = true
                impegno.isClickable = true
                if ((titoloe.text.toString().isNotEmpty() && luogoe.text.toString().isNotEmpty()
                            && descrizionee.text.toString().isNotEmpty()) && orainizioevalid && dataeIsValid
                ) {
                    val nuovoevento = EventoInfo(titoloe.text.toString(), eventDateCal , descrizionee.text.toString(),luogoe.text.toString())
                    listaeventi.add(nuovoevento)
                    Log.d("Nuovoevento", nuovoevento.cal?.timeInMillis.toString())
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
                        requireActivity().startActivity(intent)
                        creareeve.isVisible = false
                        calendarView.isVisible = true
                        calendarView.isClickable = true
                        calendarView.isEnabled = true

                        val dataprova1 : java.util.Calendar = java.util.Calendar.getInstance()

                        dataprova1.set(eventDateCal.get(java.util.Calendar.YEAR), eventDateCal.get(java.util.Calendar.MONTH),
                                eventDateCal.get(java.util.Calendar.DAY_OF_MONTH), eventDateCal.get(java.util.Calendar.HOUR),
                                eventDateCal.get(java.util.Calendar.MINUTE))
                        Log.d("dataprova1==mimp", dataprova1.timeInMillis.toString())

                        if (mImpegniDays.find {
                                    (it.calendar.get(java.util.Calendar.DAY_OF_MONTH) == dataprova1.get(java.util.Calendar.DAY_OF_MONTH)
                                            && it.calendar.get(java.util.Calendar.MONTH) == dataprova1.get(java.util.Calendar.MONTH)
                                            && it.calendar.get(java.util.Calendar.YEAR) == dataprova1.get(java.util.Calendar.YEAR))
                                }!= null) {
                            mImpEvDays.add(EventDay(dataprova1, R.drawable.blackandgreenicon))


                        }
                        else {
                            mEventDays.add(EventDay(dataprova1, R.drawable.greenicon))
                        }


                        //calendarView.setEvents(mImpEvDays.plus(mImpegniDays.plus(mEventDays)))


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

        val day : String = if(cal.get(java.util.Calendar.DAY_OF_MONTH)>=10) cal.get(java.util.Calendar.DAY_OF_MONTH).toString()
                else  "0" + cal.get(java.util.Calendar.DAY_OF_MONTH)
        val month : String = if (cal.get(java.util.Calendar.MONTH)>=9) (cal.get(java.util.Calendar.MONTH)+1).toString()
                else "0"+ (cal.get(java.util.Calendar.MONTH)+1)

        return day + "/" + month + "/" + cal.get(java.util.Calendar.YEAR)
    }
    private fun refactorTime(cal: java.util.Calendar?): String {

        val hour : String = if(cal?.get(java.util.Calendar.HOUR_OF_DAY)!! >= 10) cal?.get(java.util.Calendar.HOUR_OF_DAY).toString()
        else  "0" + cal?.get(java.util.Calendar.HOUR_OF_DAY)
        val minute : String = if (cal.get(java.util.Calendar.MINUTE)>=10) (cal.get(java.util.Calendar.MINUTE)).toString()
        else "0"+ (cal.get(java.util.Calendar.MINUTE))

        return hour + ":" + minute
    }

}