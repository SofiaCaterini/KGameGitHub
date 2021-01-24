package it.polito.kgame.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import it.polito.kgame.R
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.textView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.charset.Charset
import kotlin.math.floor
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //Wifi
        val manager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val builder = NetworkRequest.Builder()
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        builder.setNetworkSpecifier(
            WifiNetworkSpecifier.Builder().apply {
                //Qui inserite il nome del vostro WIFI e la password
                setSsid("KGame")
                setWpa2Passphrase("123123123")
            }.build()
        )
        Log.d("esp", "Builder built")
        try {
            manager.requestNetwork(builder.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    manager.bindProcessToNetwork(network)
                    Log.d("esp","network connected")
                    lifecycleScope.launch(Dispatchers.IO) {
                        val str= URL("http://192.168.4.1/").readText(Charset.forName("UTF-8"))
                        withContext(Dispatchers.Main) {
                            //qui mi faccio mostrare il peso in una textView
                            provawifi.text = str;
                        }
                    }

                }
            })
        } catch (e: SecurityException) {
            Log.e("Ciao", e.message!!)
        }
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        //Inizio parte di movimento
        //set position 0
        println("left= " + root.gameBoard.left)
        println("right= " + root.gameBoard.width)

        //root.player1.translationX = root.gameBoard.maxWidth.toFloat()
        //root.player1.translationX = ((root.gameBoard.left + root.gameBoard.maxWidth) * 4/5).toFloat()
        root.player1.translationY = 50F

        //refresh position
        var position = 0
        root.homeAddWeight.setOnClickListener {
            position++
            println("right2= " + root.gameBoard.width)
            root.player1.translationX = root.gameBoard.left + (root.gameBoard.width.toFloat() - root.player1.width.toFloat()) * 31/40
            root.player1.translationY = (root.gameBoard.width.toFloat() - root.player1.width.toFloat()) * 3/20
        }

        return root
    }
}