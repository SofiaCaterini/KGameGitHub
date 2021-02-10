package it.polito.kgame.ui.grow

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import it.polito.kgame.AlarmReceiver
import it.polito.kgame.MyAlarmManager
import it.polito.kgame.R
import kotlinx.android.synthetic.main.fragment_grow.*
import kotlinx.android.synthetic.main.fragment_grow.view.*
import java.security.AccessController.getContext
import java.sql.Types.NULL


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
                var message : String = "$messaggiosalvato $orarioscelto?"

                AlertDialog.Builder(root.context)
                        .setTitle(R.string.question_title)
                        .setMessage(message)
                        .setPositiveButton(R.string.yes) { _, _ -> yesClicked(requireActivity().applicationContext, cal) }
                        .setNegativeButton(R.string.no) { _, _ -> noClicked() }
                        .show()
            }
            TimePickerDialog(root.context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }


        return root
    }

    }
    fun yesClicked(context: Context, calendar: Calendar){
    //Intent
        /*val intent = Intent(AlarmClock.ACTION_SET_ALARM)
        intent.putExtra(AlarmClock.EXTRA_MESSAGE,"Pesati!")
        intent.putExtra(AlarmClock.EXTRA_HOUR, 19)
        intent.putExtra(AlarmClock.EXTRA_MINUTES, 19)
        startActivity(context, intent)*/
        /*
        //prova 2
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(AlarmClock.EXTRA_MESSAGE,"Pesati!")
        intent.putExtra(AlarmClock.EXTRA_HOUR, 19)
        intent.putExtra(AlarmClock.EXTRA_MINUTES, 19)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        val interval = (60 * 1000).toLong()
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent)*/

        //prova 3

        MyAlarmManager.setAlarm(context, calendar.timeInMillis, "Test Message!")
    }
    fun noClicked(){
    //Do nothing
    }