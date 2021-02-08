package it.polito.kgame.ui.grow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        return root
    }
    }