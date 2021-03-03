package it.polito.kgame.ui.grow

import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.provider.AlarmClock
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import it.polito.kgame.R
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_grow.*


class GrowFragment : Fragment(R.layout.fragment_grow) {

    val growViewModel by activityViewModels<GrowViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_grow)

        //Graph
        var graph : GraphView = view.findViewById(R.id.graph) as GraphView
        val series = LineGraphSeries(arrayOf(DataPoint(0.toDouble(), 1.toDouble()), DataPoint(1.toDouble(), 5.toDouble()), DataPoint(2.toDouble(), 3.toDouble())));
        series.color = R.color.black
        graph.addSeries(series)


        //Sveglia
        sveglia.setOnClickListener {

            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
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
                        .setNegativeButton(R.string.no) { _, _ -> noClicked() }
                        .show()
            }
            TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()

        }

    }

    }


    fun noClicked(){
    //Do nothing
    }