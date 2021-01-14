package it.polito.kgame.ui.grow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import it.polito.kgame.R

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
        val textView: TextView = root.findViewById(R.id.text_grow)
        growViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}