package it.polito.kgame.ui.grow

import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
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
import androidx.lifecycle.Observer
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import it.polito.kgame.PesoInfo
import it.polito.kgame.R
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_grow.*
import kotlinx.android.synthetic.main.fragment_grow.obiettivo
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.obj_form.*


class GrowFragment : Fragment(R.layout.fragment_grow){

    val growViewModel by activityViewModels<GrowViewModel>()

    val oneDayInMillis : Long = 86400000

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val cal = Calendar.getInstance()
        val todayMillis = cal.timeInMillis
        val listapesate : MutableList<PesoInfo> = ArrayList()
        var ser : LineGraphSeries<DataPoint>? = null
        val graph : GraphView = view.findViewById(R.id.graph) as GraphView
        var appBar = requireActivity().findViewById<AppBarLayout>(R.id.appBarLayout)

        val bundle = this.arguments
        if (bundle != null) {
            obb.isVisible = true
            appBar.foreground = ColorDrawable(requireContext().getColor(R.color.addBlack))
            obscure2.visibility = View.VISIBLE
        } else {
            obb.isVisible = false
            appBar.foreground = ColorDrawable(requireContext().getColor(R.color.empty))
            obscure2.visibility = View.INVISIBLE
        }

        growViewModel.Weights.observe(viewLifecycleOwner, Observer { weight ->
            right_arrow.isVisible = true
            left_arrow.isVisible = true
            last_x_days.isVisible = true
            println("WEIGHTS: $weight")
            println("nWeight: ${weight.size}")
            listapesate.clear()
            val dati: MutableMap<Long, Double> = mutableMapOf()
            weight.forEach {
                listapesate.add(it)
                dati[it.data!!] = it.peso?.toDouble()!!
            }

            println("DATI: $dati")
            val dataultimapesata: java.util.Calendar = java.util.Calendar.getInstance()
            dataultimapesata.timeInMillis = listapesate[listapesate.size - 1].data!!
            materialTextView4.text = refactorDate(dataultimapesata)

            //Graph
            ser = updateGraph(dati)

            graph.addSeries(ser)
            resizeGraph(0, ser!!, todayMillis)


        })
        var obj : Int = 0
        var objIsActive = false
        val np: NumberPicker = view.findViewById(R.id.numberPicker)
        var objLine = LineGraphSeries(
                arrayOf(
                        DataPoint(
                                (todayMillis - 31 * oneDayInMillis)
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

        fun refresh() {
            if(objIsActive) {

                if (graph.series.contains(objLine)){
                graph.removeSeries(objLine)
                }

                objLine = LineGraphSeries(
                        arrayOf(
                                DataPoint(
                                        (todayMillis - 31 * oneDayInMillis)
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


        growViewModel.thisUser.observe(viewLifecycleOwner, Observer {
            println("OBJ: ${it.objective}")
            //set objective
            if (it.objective != null) {
                obj = it.objective!!.toInt()
                objIsActive = true
                if (ser != null) {
                    resizeGraph(0, ser!!, todayMillis)
                }
            }
            refresh()
            //obb.isVisible = false


        })

        /*val dataultimapesata: java.util.Calendar = java.util.Calendar.getInstance()
        dataultimapesata.timeInMillis = listapesate[listapesate.size].data!!
        materialTextView4.text = refactorDate(dataultimapesata)*/

        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_grow)

        //Graph

        graph.gridLabelRenderer.setHorizontalLabelsAngle(45)
        //graph.gridLabelRenderer.numHorizontalLabels = 6
        graph.gridLabelRenderer.setHumanRounding(true, true)
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



        var graphWidthIndex = 0
        left_arrow.setOnClickListener {
           if(graphWidthIndex == 0)  graphWidthIndex=2 else graphWidthIndex--
            if(ser!= null) {
                resizeGraph(graphWidthIndex, ser!!, todayMillis)
            }

        }
        right_arrow.setOnClickListener {
            if(graphWidthIndex == 2)  graphWidthIndex=0 else graphWidthIndex++
            if(ser!= null) {
                resizeGraph(graphWidthIndex, ser!!, todayMillis)
            }
        }




        val kgValues = arrayOfNulls<String>(200)

        for (i in 0..199) {
            kgValues[i] = i.toString() + " Kg"
        }
        np.minValue = 0
        np.maxValue = 199
        np.value = growViewModel.thisUser.value?.objective?.toInt()?:50
        np.displayedValues = kgValues

        obiettivo.setOnClickListener {
            np.value = growViewModel.thisUser.value?.objective?.toInt()?:50

            obb.isVisible = true
            appBar.foreground = ColorDrawable(requireContext().getColor(R.color.addBlack))
            obscure2.visibility = View.VISIBLE

        }

       ok.setOnClickListener {
            obb.isVisible = false
            appBar.foreground = ColorDrawable(requireContext().getColor(R.color.empty))
            obscure2.visibility = View.INVISIBLE


            var messag : String = getString(R.string.question_message_obj)
            var kg : String = getString(R.string.kgq)
            var peso : String = np.value.toString()
            var message2 : String = "$messag $peso $kg"



            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.question_title_obj_ok)
                    .setMessage(message2)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        obj = np.value
                        objIsActive = true
                        refresh()
                        growViewModel.changeObjective(obj.toDouble())
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
        }

        cancel.setOnClickListener {
            obb.isVisible = false
            appBar.foreground = ColorDrawable(requireContext().getColor(R.color.empty))
            obscure2.visibility = View.INVISIBLE
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
                        .setTitle(R.string.question_title_weight)
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

        println("giorno del mese: " + graphWidthIndex)

        graph.removeSeries(series)

            when(graphWidthIndex) {
                0 -> {
                    last_x_days.text = "Ultimi 7 giorni"
                    // set manual x bounds to have nice steps
                    graph.viewport.setMinX(today - (7 * oneDayInMillis).toDouble())
                    graph.viewport.setMaxX(today + (oneDayInMillis).toDouble())
                    graph.viewport.isXAxisBoundsManual = true
                }
                1 -> {
                    last_x_days.text = "Ultimi 15 giorni"
                    // set manual x bounds to have nice steps
                    graph.viewport.setMinX(today - (15 * oneDayInMillis).toDouble())
                    graph.viewport.setMaxX(today + (oneDayInMillis).toDouble())
                    graph.viewport.isXAxisBoundsManual = true
                }
                2 -> {
                    last_x_days.text = "Ultimo mese"
                    // set manual x bounds to have nice steps
                    graph.viewport.setMinX(today - (31 * oneDayInMillis).toDouble())
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

private fun refactorDate(cal: java.util.Calendar): String {

    val day : String = if(cal.get(java.util.Calendar.DAY_OF_MONTH)>=10) cal.get(java.util.Calendar.DAY_OF_MONTH).toString()
    else  "0" + cal.get(java.util.Calendar.DAY_OF_MONTH)
    val month : String = if (cal.get(java.util.Calendar.MONTH)>=9) (cal.get(java.util.Calendar.MONTH)+1).toString()
    else "0"+ (cal.get(java.util.Calendar.MONTH)+1)

    return day + "/" + month + "/" + cal.get(java.util.Calendar.YEAR)
}

private fun updateGraph(dati: MutableMap<Long, Double>) : LineGraphSeries<DataPoint> {
    var series: LineGraphSeries<DataPoint>
    println("dentro updatecalendar")
    println("dates: ${dati.keys}")
    println("pesate: ${dati.values}")

    var arr : MutableList<DataPoint> = mutableListOf()
    var i = 0
    var c : Calendar = Calendar.getInstance()
    while (i < dati.size) {
        c.setTimeInMillis(dati.keys.elementAt(i))
        arr.add(DataPoint(
                c.timeInMillis
                        //(c.get(Calendar.YEAR).toString() + dueCifre(c.get(Calendar.MONTH).toString()) + dueCifre(c.get(Calendar.DAY_OF_MONTH).toString()))
                        .toDouble(),
                dati.values.elementAt(i)))
        i++
    }
    println(arr)
    series = LineGraphSeries(arr.toTypedArray())
//        graph.viewport.setMinY( getMin(simPesate) - 7.0)
//        graph.viewport.setMaxY( getMax(simPesate) + 1.0)
//        graph.viewport.isYAxisBoundsManual = true
    series.color = R.color.black
    return series

}

