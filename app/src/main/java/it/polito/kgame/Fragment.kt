package it.polito.kgame

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.charset.Charset



class Fragment : Fragment(R.layout.fragment_) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_, container, false)

    }


}