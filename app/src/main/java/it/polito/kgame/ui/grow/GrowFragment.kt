package it.polito.kgame.ui.grow

import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import it.polito.kgame.R
import kotlinx.android.synthetic.main.fragment_grow.*
import kotlinx.android.synthetic.main.fragment_grow.view.*


class GrowFragment : Fragment() {

    private lateinit var growViewModel: GrowViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        growViewModel =
                ViewModelProvider(this).get(GrowViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_grow, container, false)
        growViewModel.text.observe(viewLifecycleOwner, Observer {
        })

        //Graph
        val graph = root.findViewById(R.id.graph) as GraphView
        val series = LineGraphSeries(arrayOf(DataPoint(0.toDouble(), 1.toDouble()), DataPoint(1.toDouble(), 5.toDouble()), DataPoint(2.toDouble(), 3.toDouble())));
        series.color = R.color.black
        graph.addSeries(series)
        //Sveglia

        root.sveglia.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                var orarioscelto : String = SimpleDateFormat("HH:mm").format(cal.time)
                var messaggiosalvato : String = getString(R.string.question_message)
                var message : String = "$messaggiosalvato $orarioscelto ?"
                AlertDialog.Builder(root.context)
                        .setTitle(R.string.question_title)
                        .setMessage(message)
                        .setPositiveButton(R.string.yes) { _, _ -> yesClicked() }
                        .setNegativeButton(R.string.no) { _, _ -> noClicked() }
                        .show()
            }
            TimePickerDialog(root.context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }



        return root
    }
    }
    fun yesClicked(){

    }
    fun noClicked(){

    }