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
            root.player1.translationX = root.gameBoard.left + (root.gameBoard.width.toFloat() ) * getXYmodsFromPosition(position).first - root.player1.width.toFloat()/2
            root.player1.translationY = root.gameBoard.top + (root.gameBoard.height.toFloat() ) * getXYmodsFromPosition(position).second - root.player1.height.toFloat()/2
        }

        return root
    }

    fun getXYmodsFromPosition (position: Int): Pair<Float, Float> {
        var(x, y) = Pair(1F, 1F)
        when (position) {
            1 -> {
                println("position == 1")
                x = 75/100F
                y = 6/100F
            }
            2 -> {
                println("position == 2")
                x = 62/100F
                y = 6/100F
            }
            3 -> {
                println("position == 3")
                x = 53/100F
                y = 6/100F
            }
            4 -> {
                println("position == 4")
                x = 41/100F
                y = 6/100F
            }
            5 -> {
                println("position == 5")
                x = 30/100F
                y = 7/100F
            }
            6 -> {
                println("position == 6")
                x = 23/100F
                y = 10/100F
            }
            7 -> {
                println("position == 7")
                x = 23/100F
                y = 18/100F
            }
            8 -> {
                println("position == 8")
                x = 27/100F
                y = 22/100F
            }
            9 -> {
                println("position == 9")
                x = 35/100F
                y = 22/100F
            }
            10 -> {
                println("position == 10")
                x = 45/100F
                y = 20/100F
            }
            11 -> {
                println("position == 1")
                x = 55/100F
                y = 20/100F
            }
            12 -> {
                println("position == 12")
                x = 65/100F
                y = 22/100F
            }
            13 -> {
                println("position == 13")
                x = 75/100F
                y = 25/100F
            }
            14 -> {
                println("position == 14")
                x = 78/100F
                y = 35/100F
            }
            15 -> {
                println("position == 15")
                x = 70/100F
                y = 40/100F
            }
            16 -> {
                println("position == 16")
                x = 60/100F
                y = 40/100F
            }
            17 -> {
                println("position == 17")
                x = 50/100F
                y = 40/100F
            }
            18 -> {
                println("position == 18")
                x = 39/100F
                y = 39/100F
            }
            19 -> {
                println("position == 19")
                x = 30/100F
                y = 38/100F
            }
            20 -> {
                println("position == 20")
                x = 22/100F
                y = 43/100F
            }
            21 -> {
                println("position == 21")
                x = 21/100F
                y = 51/100F
            }
            22 -> {
                println("position == 22")
                x = 25/100F
                y = 58/100F
            }
            23 -> {
                println("position == 23")
                x = 34/100F
                y = 62/100F
            }
            24 -> {
                println("position == 24")
                x = 43/100F
                y = 63/100F
            }
            25 -> {
                println("position == 25")
                x = 52/100F
                y = 63/100F
            }
            26 -> {
                println("position == 26")
                x = 61/100F
                y = 63/100F
            }
            27 -> {
                println("position == 27")
                x = 71/100F
                y = 67/100F
            }
            28 -> {
                println("position == 28")
                x = 79/100F
                y = 75/100F
            }
            29 -> {
                println("position == 29")
                x = 81/100F
                y = 84/100F
            }
            30 -> {
                println("position == 30")
                x = 77/100F
                y = 92/100F
            }
            31 -> {
                println("position == 31")
                x = 66/100F
                y = 95/100F
            }
            32 -> {
                println("position == 32")
                x = 57/100F
                y = 95/100F
            }
            33 -> {
                println("position == 33")
                x = 46/100F
                y = 95/100F
            }
            34 -> {
                println("position == 31")
                x = 35/100F
                y = 95/100F
            }
            35 -> {
                println("position == 35")
                x = 22/100F
                y = 95/100F
            }

            else -> { // Note the block
                print("position is neither 1 nor 2")
            }
        }
        return Pair(x, y)
    }
}