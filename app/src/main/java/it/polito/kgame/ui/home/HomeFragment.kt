package it.polito.kgame.ui.home

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.polito.kgame.R
import it.polito.kgame.ui.grow.noClicked
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.charset.Charset

class HomeFragment : Fragment(R.layout.fragment_home) {

    val adapter = ItemAdapterUsers()
    val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_home)

        homeViewModel.data.observe(viewLifecycleOwner, Observer { data-> adapter.setData(data) })
        rvhome.layoutManager= LinearLayoutManager(requireContext())
        rvhome.adapter = adapter




        //Inizio parte di movimento
        var position = 0
        var playersNum = 2

        homeAddWeight.setOnClickListener {

            var messaggiosalvato : String = getString(R.string.question_message_weight)
            var message : String = "$messaggiosalvato"

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.question_title_weight)
                    .setMessage(message)
                    .setPositiveButton(R.string.yes) { _, _ ->
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


                                            var messaggiosalvato2 : String = getString(R.string.question_message_obj_ok)
                                            var kg : String = getString(R.string.kg)
                                            var peso : String = str
                                            var message2 : String = "$messaggiosalvato2 $peso $kg"

                                            MaterialAlertDialogBuilder(requireContext())
                                                    .setTitle(R.string.question_title_weight_ok)
                                                    .setMessage(message2)
                                                    .setPositiveButton(R.string.ok) { _, _ ->

                                                    }
                                                    .show()



                                        }
                                    }


                                }
                            })
                        } catch (e: SecurityException) {
                            Log.e("Ciao", e.message!!)

                        }




                    }
                    .setNegativeButton(R.string.no) { _, _ -> noClicked() }
                    .show()
        }
        /*homeAddWeight.setOnClickListener {
            //refresh position
            position++
            //move player
            println("right2= " + view.gameBoard.width)
            when (playersNum) {
                1 -> {
                    player1.translationX =
                        view.gameBoard.left + (view.gameBoard.width.toFloat()) * getXYmodsFromPosition(
                            position
                        ).first - player1.width.toFloat() / 2
                    player1.translationY =
                        view.gameBoard.top + (view.gameBoard.height.toFloat()) * getXYmodsFromPosition(
                            position
                        ).second - player1.height.toFloat() / 2
                }
                2 -> {
                    player1.translationX =
                        view.gameBoard.left + (view.gameBoard.width.toFloat()) * getXYmodsFromPosition(
                            position
                        ).first - player1.width.toFloat()*3/4
                    player1.translationY =
                        view.gameBoard.top + (view.gameBoard.height.toFloat()) * getXYmodsFromPosition(
                            position
                        ).second - player1.height.toFloat()*3/4

                    player2.translationX =
                        view.gameBoard.left + (view.gameBoard.width.toFloat()) * getXYmodsFromPosition(
                            position
                        ).first - player2.width.toFloat()/4
                    player2.translationY =
                        view.gameBoard.top + (view.gameBoard.height.toFloat()) * getXYmodsFromPosition(
                            position
                        ).second - player2.height.toFloat()/4

                }
            }
        }*/

    }

    //Prende in input il numero della casella e ritorna i moltiplicatori x ed y relativi a quella casella
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
                y = 5/100F
            }
            3 -> {
                println("position == 3")
                x = 51/100F
                y = 5/100F
            }
            4 -> {
                println("position == 4")
                x = 41/100F
                y = 5/100F
            }
            5 -> {
                println("position == 5")
                x = 30/100F
                y = 7/100F
            }
            6 -> {
                println("position == 6")
                x = 22/100F
                y = 11/100F
            }
            7 -> {
                println("position == 7")
                x = 22/100F
                y = 17/100F
            }
            8 -> {
                println("position == 8")
                x = 27/100F
                y = 22/100F
            }
            9 -> {
                println("position == 9")
                x = 36/100F
                y = 22/100F
            }
            10 -> {
                println("position == 10")
                x = 46/100F
                y = 20/100F
            }
            11 -> {
                println("position == 11")
                x = 56/100F
                y = 21/100F
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
                x = 79/100F
                y = 35/100F
            }
            15 -> {
                println("position == 15")
                x = 70/100F
                y = 40/100F
            }
            16 -> {
                println("position == 16")
                x = 59/100F
                y = 40/100F
            }
            17 -> {
                println("position == 17")
                x = 49/100F
                y = 40/100F
            }
            18 -> {
                println("position == 18")
                x = 39/100F
                y = 39/100F
            }
            19 -> {
                println("position == 19")
                x = 29/100F
                y = 38/100F
            }
            20 -> {
                println("position == 20")
                x = 23/100F
                y = 44/100F
            }
            21 -> {
                println("position == 21")
                x = 22/100F
                y = 52/100F
            }
            22 -> {
                println("position == 22")
                x = 26/100F
                y = 59/100F
            }
            23 -> {
                println("position == 23")
                x = 34/100F
                y = 62/100F
            }
            24 -> {
                println("position == 24")
                x = 44/100F
                y = 63/100F
            }
            25 -> {
                println("position == 25")
                x = 54/100F
                y = 63/100F
            }
            26 -> {
                println("position == 26")
                x = 64/100F
                y = 64/100F
            }
            27 -> {
                println("position == 27")
                x = 72/100F
                y = 68/100F
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
                x = 78/100F
                y = 93/100F
            }
            31 -> {
                println("position == 31")
                x = 68/100F
                y = 95/100F
            }
            32 -> {
                println("position == 32")
                x = 56/100F
                y = 95/100F
            }
            33 -> {
                println("position == 33")
                x = 46/100F
                y = 95/100F
            }
            34 -> {
                println("position == 34")
                x = 35/100F
                y = 95/100F
            }
            35 -> {
                println("position == 35")
                x = 24/100F
                y = 95/100F
            }

            else -> { // Note the block
                print("position is neither 1 nor 2")
            }
        }
        return Pair(x, y)
    }
}