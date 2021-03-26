package it.polito.kgame.ui.grow

import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.provider.AlarmClock
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import it.polito.kgame.R
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_grow.*


class GrowFragment : Fragment(R.layout.fragment_grow){

    val growViewModel by activityViewModels<GrowViewModel>()

    val oneDayInMillis : Long = 86400000

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val cal = Calendar.getInstance()
        val todayMillis = cal.timeInMillis

        println("UNo " + todayMillis)
        println("dueee " + oneDayInMillis)
        println("dueee * 31 " + 31*oneDayInMillis)
        println("sotttea " + (todayMillis - oneDayInMillis))
        println("sotttea 30 " + (todayMillis - 31*oneDayInMillis))




        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_grow)

        //Graph
        val sim30Pesate : DoubleArray = doubleArrayOf(69.9, 70.1, 70.7, 71.0, 70.5, 68.8, 69.5, 67.9, 66.0, 67.2, 67.2, 68.5, 70.1, 70.7, 71.0, 70.5, 68.8, 69.5, 67.9, 66.0, 67.2, 67.2, 68.5, 70.1, 70.7, 71.0, 70.5, 68.8, 69.5, 67.9)
        val sim30Dates : LongArray = longArrayOf(1612300000000, 1613300000000, 1614300000000, 1615300000000, 1616300000000, 1617300000000, 1618300000000, 1619300000000, 1620300000000, 1621300000000, 1622300000000, 1623300000000, 1624300000000, 1625300000000, 1626300000000, 1627300000000, 1628300000000, 1629300000000, 1630300000000, 1631300000000, 1632300000000, 1633300000000, 1634300000000, 1635300000000, 1636300000000, 1637300000000, 1638300000000, 1639300000000, 1640300000000, 1641300000000)

        var graph : GraphView = view.findViewById(R.id.graph) as GraphView

        graph.gridLabelRenderer.numHorizontalLabels = 5
        graph.gridLabelRenderer.setHumanRounding(false,true)
        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    val c : Calendar = Calendar.getInstance()
                    c.setTimeInMillis(value.toLong())
                    val day = c.get(Calendar.DAY_OF_MONTH).toDouble()
                    val month = monthNumToLetter(c.get(Calendar.MONTH))

                    super.formatLabel(day, isValueX) + month
                } else {
                    super.formatLabel(value, isValueX) + "kg"
                }
            }
        }

        var series = LineGraphSeries(arrayOf(DataPoint(0.toDouble(), 1.toDouble()), DataPoint(1.toDouble(), 5.toDouble()), DataPoint(2.toDouble(), 3.toDouble())))

        if(sim30Dates.size==sim30Pesate.size){
            var arr : MutableList<DataPoint> = mutableListOf()
            var i = 0
            var c : Calendar = Calendar.getInstance()
            while (i < sim30Dates.size) {
                c.setTimeInMillis(sim30Dates[i])
                arr.add(DataPoint(
                        c.timeInMillis
                                //(c.get(Calendar.YEAR).toString() + dueCifre(c.get(Calendar.MONTH).toString()) + dueCifre(c.get(Calendar.DAY_OF_MONTH).toString()))
                                .toDouble(),
                        sim30Pesate[i]))
                i++
            }
            println(arr)
            series = LineGraphSeries(arr.toTypedArray())

        } else {
            android.app.AlertDialog.Builder(requireContext())
                    .setTitle("I dati non sono corretti")
                    .show()
        }

//        graph.viewport.setMinY( getMin(simPesate) - 7.0)
//        graph.viewport.setMaxY( getMax(simPesate) + 1.0)
//        graph.viewport.isYAxisBoundsManual = true

        series.color = R.color.black
        graph.addSeries(series)

        var graphWidthIndex = 0;
        left_arrow.setOnClickListener {
           if(graphWidthIndex == 0)  graphWidthIndex=2 else graphWidthIndex--
            resizeGraph(graphWidthIndex, series, todayMillis)

        }
        right_arrow.setOnClickListener {
            if(graphWidthIndex == 2)  graphWidthIndex=0 else graphWidthIndex++
            resizeGraph(graphWidthIndex, series, todayMillis)
        }

        //set objective
        var obj : Int = 0
        var objIsActive = false
        val np: NumberPicker = view.findViewById(R.id.numberPicker)
        np.isVisible = false
        cancel.isVisible = false
        ok.isVisible = false
        numPick_bg.isVisible = false

        var objLine = LineGraphSeries(
                arrayOf(
                    DataPoint(
                        (todayMillis - 31*oneDayInMillis)
                            .toDouble(),
                        obj.toDouble()
                    ),
                    DataPoint(
                        todayMillis
                            .toDouble(),
                        obj.toDouble()
                    )
                )
        )

        refreshgrow2.setOnClickListener {
            if(objIsActive) {
                graph.removeSeries(objLine)

                objLine = LineGraphSeries(
                        arrayOf(
                            DataPoint(
                                (todayMillis - 31*oneDayInMillis)
                                .toDouble(),
                                obj.toDouble()
                            ),
                            DataPoint(
                                todayMillis
                                    .toDouble(),
                                obj.toDouble()
                            )
                        )
                )
                objLine.color = R.color.white
                graph.addSeries(objLine)
            }
        }


        val kgValues = arrayOfNulls<String>(200)

        for (i in 0..199) {
            kgValues[i] = i.toString() + " Kg"
        }
        np.minValue = 0
        np.maxValue = 199
        np.value = 50 //mettere obiettivo precedente
        np.displayedValues = kgValues

        obiettivo.setOnClickListener {
            np.isVisible = true
            cancel.isVisible = true
            ok.isVisible = true
            numPick_bg.isVisible = true

            ok.setOnClickListener {
                np.isVisible = false
                cancel.isVisible = false
                ok.isVisible = false
                numPick_bg.isVisible = false

                var messag : String = getString(R.string.question_message_obj)
                var kg : String = getString(R.string.kgq)
                var peso : String = np.value.toString()
                var message2 : String = "$messag $peso $kg"

                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.question_title_obj_ok)
                        .setMessage(message2)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            obj = np.value
                            objIsActive = true
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
            }
        }
        cancel.setOnClickListener {
            np.isVisible = false
            cancel.isVisible = false
            ok.isVisible = false
            numPick_bg.isVisible = false

        }

        //Sveglia
        sveglia.setOnClickListener {

            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                var orarioscelto : String = SimpleDateFormat("HH:mm").format(cal.time)
                var messaggiosalvato : String = getString(R.string.question_message)
                var message : String = "$messaggiosalvato $orarioscelto?"

                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.question_title)
                        .setMessage(message)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Pesati!")
                            intent.putExtra(AlarmClock.EXTRA_HOUR, cal.get(Calendar.HOUR_OF_DAY))
                            intent.putExtra(AlarmClock.EXTRA_MINUTES, cal.get(Calendar.MINUTE))
                            requireActivity().startActivity(intent)

                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> noClicked() }
                        .show()
            }
            TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()

        }

    }

    private fun monthNumToLetter(monthNum: Int): String? {
        when(monthNum) {
            0 -> {
                return requireContext().resources.getString(R.string.gen)
            }
            1 -> {
                return requireContext().resources.getString(R.string.feb)
            }
            2 -> {
                return requireContext().resources.getString(R.string.mar)
            }
            3 -> {
                return requireContext().resources.getString(R.string.apr)
            }
            4 -> {
                return requireContext().resources.getString(R.string.mag)
            }
            5 -> {
                return requireContext().resources.getString(R.string.giu)
            }
            6 -> {
                return requireContext().resources.getString(R.string.lug)
            }
            7 -> {
                return requireContext().resources.getString(R.string.ago)
            }
            8 -> {
                return requireContext().resources.getString(R.string.set)
            }
            9 -> {
                return requireContext().resources.getString(R.string.ott)
            }
            10 -> {
                return requireContext().resources.getString(R.string.nov)
            }
            11 -> {
                return requireContext().resources.getString(R.string.dic)
            }
        }
        return null
    }

    private fun dueCifre(x: String): String {return if (x.length==1) ("0" + x) else x}

    private fun resizeGraph(graphWidthIndex: Int, series: LineGraphSeries<DataPoint>, today: Long) {

        //println("giorno del mese: " + today)

        graph.removeSeries(series)

            when(graphWidthIndex) {
                0 -> {
                    last_x_days.text = "Ultimi 7 giorni"
                    // set manual x bounds to have nice steps
                    graph.viewport.setMinX(today - (7*oneDayInMillis).toDouble())
                    graph.viewport.setMaxX(today + (oneDayInMillis).toDouble())
                    graph.viewport.isXAxisBoundsManual = true
                }
                1 -> {
                    last_x_days.text = "Ultimi 15 giorni"
                    // set manual x bounds to have nice steps
                    graph.viewport.setMinX(today - (15*oneDayInMillis).toDouble())
                    graph.viewport.setMaxX(today + (oneDayInMillis).toDouble())
                    graph.viewport.isXAxisBoundsManual = true
                }
                2 -> {
                    last_x_days.text = "Ultimo mese"
                    // set manual x bounds to have nice steps
                    graph.viewport.setMinX(today - (31*oneDayInMillis).toDouble())
                    graph.viewport.setMaxX(today + (oneDayInMillis).toDouble())
                    graph.viewport.isXAxisBoundsManual = true
                }
            }
        graph.addSeries(series)
    }

}


    fun noClicked(){
    //Do nothing
    }

    fun getMin(x: DoubleArray): Double {
        var firstIteration = true
        var res : Double = 6.9
        for (d in x) {
            if(firstIteration) {
                res = d
                firstIteration = false
            }
            else {
                if(d<res) {
                    res=d
                }
            }
        }
        return res
    }

    fun getMax(x: DoubleArray): Double {
        var firstIteration = true
        var res : Double = 6.9
        for (d in x) {
            if(firstIteration) {
                res = d
                firstIteration = false
            }
            else {
                if(d>res) {
                    res=d
                }
            }
        }
        return res
    }

