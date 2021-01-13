package it.polito.kgame

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.home_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.charset.Charset

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        //Wifi
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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
                            provawifi.text = str
                        }
                    }

                }
            })
        } catch (e: SecurityException) {
            Log.e("Ciao", e.message!!)
        }




        setSupportActionBar(findViewById(R.id.toolbar))
        val drawerToggle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        drawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

