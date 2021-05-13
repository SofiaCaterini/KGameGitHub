package it.polito.kgame.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import it.polito.kgame.*
import it.polito.kgame.ui.grow.noClicked
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.carousel_layout.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.join_match.*
import kotlinx.android.synthetic.main.join_match.txt_title
import kotlinx.android.synthetic.main.wait_for_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.charset.Charset
import java.util.*


class HomeFragment : Fragment(R.layout.fragment_home) {

    val adapter = ItemAdapterUsers()
    val homeViewModel by activityViewModels<HomeViewModel>()

    var pawnWidth : Int? = null
    private var pawnHeight : Int? = null
    val sampleImages = arrayListOf<Int>(R.drawable.dog, R.drawable.lion)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val layout = requireActivity().findViewById<ConstraintLayout>(R.id.homeLayout)

        println("COPIARE: " + layout.gameBoard.width + "/" + layout.gameBoard.height)// = ViewGroup.LayoutParams()

        pawnHeight=player1.layoutParams.height
        pawnWidth=player1.layoutParams.width
        //println("misure della pedina: w - " + pawnWidth + "; h - " +pawnHeight)

        val listapesate : MutableList<PesoInfo> = ArrayList()
        val today : Calendar = Calendar.getInstance()
        //today.timeInMillis = System.currentTimeMillis()
        var dataultima : Calendar = Calendar.getInstance()
        var datacontroller : Boolean = true

        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_home)

        homeViewModel.thisUser.observe(viewLifecycleOwner,
            { userValue ->
                println("tizio: " + userValue)
//                val endTimeCal = Calendar.getInstance()
//                if (homeViewModel.thisUsersFam.userValue?.lastMatchMillis != null) {
//                    endTimeCal.timeInMillis = homeViewModel.thisUsersFam.userValue?.lastMatchMillis!!
//                    endTimeCal.add(Calendar.DAY_OF_MONTH,30)
//                    if (today.after(endTimeCal)) homeViewModel.thisUsersFam.userValue?.matchState = "ENDED"
//                    endTimeCal.add(Calendar.DAY_OF_MONTH, 1)
//                    if(today.after(endTimeCal) && homeViewModel.thisUsersFam.userValue?.playersInGame == 0) homeViewModel.thisUsersFam.userValue?.matchState = "NONE"
//                }
//
//                if(homeViewModel.thisUsersFam.userValue?.matchState == "ENDED") {
//                    userValue.isInGame= false
//                    homeViewModel.thisUsersFam.userValue?.playersInGame = homeViewModel.thisUsersFam.userValue?.playersInGame!! - 1
//                }

                println("dovrebbe " + (userValue.isInGame))
                if(userValue.isInGame) {    //then match must be "STARTED" or "ACTIVE"
                    homeViewModel.thisUsersFam.observe(viewLifecycleOwner,
                            { famValue ->
                                println("tizios fatti  " + famValue)
                                when (famValue.matchState) {
                                    "STARTED" -> {
                                        gameBoard.alpha= 1F
                                        joinMatch.visibility= View.GONE
                                        waitForPlayers.visibility= View.GONE
                                    }
                                    "ACTIVE" -> {
                                        gameBoard.alpha= 0.5F
                                        joinMatch.visibility= View.GONE
                                        waitForPlayers.visibility= View.VISIBLE
                                        txt_waitForPlayers.text= "Aspetta che gli altri giocatori della famiglia si uniscano, la partita inizierà quando ci sarete tutti. \n" +
                                                        "Se sei solo invita altri ad unirsi alla tua famiglia facedoli accedere con questo codice: " + famValue.code + "\n" +
                                                        "Se credi che tutti si siano già uniti prova a resettare l'app."
                                    }
                                }
                            }
                    )
                }
                else {  //player is not in game
                    //activate dialog box
                    gameBoard.alpha= 0.5F
                    joinMatch.visibility= View.VISIBLE
                    waitForPlayers.visibility= View.GONE

                    homeViewModel.thisUsersFam.observe(viewLifecycleOwner,
                        { famValue ->
                            println("tizios fatti  " + famValue)
                            when(famValue.matchState) {
                                "NONE" -> { //player can create first match
                                    txt_title.text= "Avvia una nuova partita"   //title

                                    but_obj.visibility = View.VISIBLE           //onj button

                                    if (userValue.objective == null) {              //obj text; end button
                                        txt_obj.text =
                                                "Devi impostare un obiettivo prima di poter iniziare la partita. Dopo averla avviata non potrai più modificarlo prima di 30 giorni"
                                        setStartButtonDisabled()
                                    } else {
                                        txt_obj.text =
                                                "Adesso il tuo obiettivo di peso è di ${userValue.objective} kg. Una volta che avvierai la partita non potrai più modificarlo fino alla fine del mese. Vuoi cambiarlo ora?"
                                        setStartButtonEnabled()
                                    }

                                }
                                "ACTIVE" -> {   //player can join
                                    txt_title.text= "Unisciti alla partita!"    //title

                                    but_obj.visibility = View.VISIBLE           //obj button

                                    if (userValue.objective == null) {              //obj text; end button
                                        txt_obj.text =
                                                "Devi impostare un obiettivo prima di poterti unire alla partita. Ricorda che dopo esserti unito non potrai più modificarlo prima di 30 giorni"
                                        setJoinButtonDisabled()
                                    } else {
                                        txt_obj.text =
                                                "Adesso il tuo obiettivo di peso è di ${userValue.objective} kg. Una volta che sarai in partita non potrai più modificarlo prima di 30 giorni. Vuoi cambiarlo ora?"
                                        setJoinButtonEnabled()
                                    }
                                }
                                "STARTED" -> {  //player can't join
                                    txt_title.text = "La partita è gia iniziata."   //title

                                    val matchEndCal = Calendar.getInstance()        //obj text
                                    matchEndCal.timeInMillis = famValue.lastMatchMillis!!
                                    matchEndCal.add(Calendar.DAY_OF_MONTH, 30)
                                    txt_obj.text = "Aspetta che finisca per unirti alla prossima. " +
                                            "\n Finirà il ${matchEndCal.get(Calendar.DAY_OF_MONTH)}/${matchEndCal.get(Calendar.MONTH)+1}"

                                    but_obj.visibility = View.GONE                  //obj button

                                    setJoinButtonDisabled()                         //end button

                                }
                                "ENDED" -> {    //player can restart match only if every player has seen victory screen -> homeViewModel.thisUsersFam.userValue?.playersInGame == 0
                                    var winner : String? = famValue.components?.find { user -> user.mail == famValue.lastWinnerMail }?.username
                                    txt_title.text = "Congratulazioni ${winner}!!!"     //title

                                    txt_obj.text = "La partita si è conclusa. Complimenti al vincitore ma anche a tutti voi! \n "   //obj text part1

                                    if(famValue.playersInGame == 0) {      //obj text part2; obj button; end button
                                        txt_obj.text = txt_obj.text.toString()  + "Sei già pronto a rigiocare? Se vuoi cambia ora il tuo obiettivo e poi avvia la partita"
                                        but_obj.visibility = View.VISIBLE
                                        setStartButtonEnabled()
                                    } else {
                                        txt_obj.text = txt_obj.text.toString()  + "Gli altri giocatori non sono ancora pronti. Digli di aprire l'app così che tutti vedano chi ha vinto"
                                        but_obj.visibility = View.GONE
                                        setStartButtonDisabled()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        )

        but_obj.setOnClickListener {

        }

        but_join.setOnClickListener {
            if(datacontroller==true) {      //non ti sei pesato oggi
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Pesati per unirti alla partita")
                        .setPositiveButton("OK") { _, _ ->
                            weighting("JOIN")
                        }
                        .setNegativeButton(R.string.cancel) { _,_ -> }
                        .show()
            }
            else {
                joinMatch()
            }
        }

        but_start.setOnClickListener {
            if(datacontroller==true) {      //non ti sei pesato oggi
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Pesati per avviare la partita")
                        .setPositiveButton("OK") { _, _ ->
                            weighting("START")
                        }
                        .setNegativeButton(R.string.cancel) { _,_ -> }
                        .show()
            }
            else {
                startMatch()
            }
        }



        homeViewModel.thisUsersFam.observe(viewLifecycleOwner,
            { value ->
                println("tizioscricca: " + value)

                val endTimeCal = Calendar.getInstance()
                if (value.lastMatchMillis != null) {
                    endTimeCal.timeInMillis = value.lastMatchMillis!!
                    endTimeCal.add(Calendar.DAY_OF_MONTH,30)
                    if (today.after(endTimeCal) && value.matchState == "STARTED") {
                        value.matchState = "ENDED"
                        value.lastWinnerMail = value.components?.sortedBy { user -> user.position }?.last()?.mail
                        homeViewModel.updateMatchState(requireContext())
                    }                                 //end match if is been more than 30 days from match start
                    endTimeCal.add(Calendar.DAY_OF_MONTH, 7)
                    if( ( (today.after(endTimeCal) && value.playersInGame <= 0) || value.playersInGame == -10) && value.matchState == "ENDED") {
                        value.matchState = "NONE"
                        homeViewModel.updateMatchState(requireContext())
                    }                                                                                       //remove victory screen if it's been a week since match ended or if victory screen has been shown
                                                                                                            //at least 1 time by all players and then 10 times more
                }

                if(value.matchState == "ENDED") { println("ma manco qua? " + homeViewModel.thisUser.value?.isInGame)
                    if(homeViewModel.thisUser.value?.isInGame == true || value.playersInGame<=0) {
                        value.playersInGame = value.playersInGame - 1
                        homeViewModel.updateMatchState(requireContext())
                    }
                    homeViewModel.thisUser.value?.isInGame= false
                    println("ohmachee: " + homeViewModel.thisUser.value?.isInGame)
                    homeViewModel.updatePlayerState(requireContext())
                }

                if (!value.components.isNullOrEmpty())
                    value.components?.let {
                        adapter.setData(it)
                        adapter.sortItems()
                        setPositions(it)
                    }

                println("2COPIARE: " + layout.gameBoard.width + "/" + layout.gameBoard.height)// = ViewGroup.LayoutParams()

                rvhome.layoutManager = LinearLayoutManager(requireContext())
                rvhome.adapter = adapter


                familyName.text = value.name

                //inserimento dati utente nell'header
                val header = requireActivity().findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
                header.findViewById<TextView>(R.id.navHeadNickname)?.text = homeViewModel.thisUser.value?.username
                header.findViewById<TextView>(R.id.navHeadFamilyName)?.text = homeViewModel.thisUsersFam.value?.name
                header.findViewById<ImageView>(R.id.navHeadProfileImg)?.let { Picasso.get().load(homeViewModel.thisUser.value?.profileImg).fit().into(it) }
            }
        )


        homeViewModel.weights.observe(viewLifecycleOwner, Observer { weights ->
            println("WEIGHTS: $weights")
            println("nWeight: ${weights.size}")
            listapesate.clear()


            weights.forEach {
                listapesate.add(it)
            }

            val dataultimapesata: Calendar = Calendar.getInstance()
            dataultimapesata.timeInMillis = listapesate[listapesate.size - 1].data!!
            dataultima = dataultimapesata

            if (dataultima.get(java.util.Calendar.DAY_OF_MONTH) == today.get(java.util.Calendar.DAY_OF_MONTH)
                    && dataultima.get(java.util.Calendar.MONTH) == today.get(java.util.Calendar.MONTH)
                    && dataultima.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR)) {
                datacontroller = false
            }
            println("data ultima pesata: ${dataultima.timeInMillis}")
            println("data oggi: ${today.timeInMillis}")
            println("Se falso oggi=dataultimapesata: $datacontroller")

        })

        /*homeViewModel.thisUsersFam.observe(viewLifecycleOwner, { fam ->
            familyName.text = fam.name

            //inserimento dati utente nell'header
            val header = requireActivity().findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
            header.findViewById<TextView>(R.id.navHeadNickname)?.text = homeViewModel.thisUser.value?.username
            header.findViewById<TextView>(R.id.navHeadFamilyName)?.text = homeViewModel.thisUsersFam.value?.name
            header.findViewById<ImageView>(R.id.navHeadProfileImg)?.let { Picasso.get().load(homeViewModel.thisUser.value?.profileImg).into(it) }
        })*/

        //Carousel

        var carouselView: CarouselView? = null
        carouselView = requireActivity().findViewById(R.id.carouselView)
        carouselView.setImageListener(imageListener)
        carouselView.pageCount = sampleImages.size


        regolamento.setOnClickListener {
            carousel.isVisible = true
            button.setOnClickListener {
                carousel.isVisible = false
            }
        }



        //Add weight

        homeAddWeight.setOnClickListener {
            if (datacontroller == false) {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Ehi attento")
                        .setMessage("Ti sei già pesato oggi, torna domani!")
                        .setPositiveButton("OK") { _, _ ->
                        }
                        .show()
            } else {
                weighting()
            }
        }
    }

    private fun weighting(fromJoinOrStartMatch : String = "none") {
        val messaggiosalvato: String = getString(R.string.question_message_weight)
        val message: String = messaggiosalvato

        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.question_title_weight)
                .setMessage(message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    //Wifi
                    val manager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val wifi = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager?
                    var wifiacceso = false
                    if (wifi != null) {
                        if (wifi.isWifiEnabled){
                            wifiacceso = true
                        }
                    }
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
                        println("dentro try")
                        var retedisponibile = false

                        manager.requestNetwork(builder.build(), object : ConnectivityManager.NetworkCallback() {
                            override fun onUnavailable() {
                                super.onUnavailable()
                                println("OnAnavailable")
                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Non sei connesso alla bilancia")
                                        .setMessage("Connettiti al wifi KGame e riprova!")
                                        .setPositiveButton(R.string.ok) { _, _ ->

                                        }
                                        .show()

                            }

                            override fun onAvailable(network: Network) {
                                println("dentro onAvailable")
                                manager.bindProcessToNetwork(network)
                                Log.d("esp", "network connected")
                                retedisponibile = true
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val str = URL("http://192.168.4.1/").readText(Charset.forName("UTF-8"))
                                    withContext(Dispatchers.Main) {

                                        val messaggiosalvato2: String = getString(R.string.question_message_obj_ok)
                                        val kg: String = getString(R.string.kg)
                                        val peso: String = str
                                        val message2: String = "$messaggiosalvato2 $peso $kg"

                                        if (homeViewModel.thisUser.value?.objective != null && homeViewModel.weights.value?.size!! > 0) {
                                            homeViewModel.changePosition(requireContext(), peso.toFloat())
                                        }

                                        DbManager.createWeight(requireContext(), peso, System.currentTimeMillis())

                                        MaterialAlertDialogBuilder(requireContext())
                                                .setTitle(R.string.question_title_weight_ok)
                                                .setMessage(message2)
                                                .setPositiveButton(R.string.ok) { _, _ ->
                                                    if(fromJoinOrStartMatch == "JOIN") joinMatch()
                                                    if(fromJoinOrStartMatch == "START") startMatch()
                                                }
                                                .show()

                                    }
                                }


                            }


                            override fun onLost(network: Network) {
                                super.onLost(network)
                                println("onLost")
                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Ti sei disconnesso dalla bilancia")
                                        .setMessage("Connettiti di nuovo al wifi KGame e riprova!")
                                        .setPositiveButton(R.string.ok) { _, _ ->

                                        }
                                        .show()
                            }


                        })

                        if(wifiacceso == false){
                            println("non connesso")
                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Non sei connesso alla bilancia")
                                    .setMessage("Connettiti al wifi KGame e riprova!")
                                    .setPositiveButton(R.string.ok) { _, _ ->

                                    }
                                    .show()

                        }
                    } catch (e: SecurityException) {
                        Log.d("Non ha trovato wifi", e.message!!)
                        println("dentro catch")
                    }


                }
                .setNegativeButton(R.string.no) { _, _ -> noClicked() }
                .show()
    }

    private fun joinMatch() {
        homeViewModel.thisUser.value?.isInGame = true
        homeViewModel.updatePlayerState(requireContext())
        homeViewModel.thisUsersFam.value?.playersInGame = homeViewModel.thisUsersFam.value?.playersInGame!! + 1
        if (homeViewModel.thisUsersFam.value?.playersInGame == homeViewModel.thisUsersFam.value?.components?.size) {
            homeViewModel.thisUsersFam.value?.matchState = "STARTED"
            homeViewModel.thisUsersFam.value?.lastMatchMillis = System.currentTimeMillis()
        }

        homeViewModel.updateMatchState(requireContext())
    }

    private fun startMatch() {
        homeViewModel.thisUser.value?.isInGame= true
        homeViewModel.updatePlayerState(requireContext())
        homeViewModel.thisUsersFam.value?.playersInGame = homeViewModel.thisUsersFam.value?.playersInGame!! + 1
        homeViewModel.thisUsersFam.value?.matchState = "ACTIVE"
        homeViewModel.updateMatchState(requireContext())
    }

    private fun setPositions(list: List<User>) {
        val layout = requireActivity().findViewById<ConstraintLayout>(R.id.homeLayout)
        println(" layout " + layout)
        println(" gameBoard " + layout.gameBoard)

        val map = mutableMapOf<Int, Int>()
        list.forEach { u -> val num = list.count { x -> x.position == u.position }
            map[u.position!!] = num
        }

        list.forEach { u ->
            val player = ImageView(requireContext()).apply {
                setImageResource(Pedina.pedina(u.pawnCode))
                adjustViewBounds = true
                layoutParams = ViewGroup.LayoutParams(
                        pawnWidth!!,
                        pawnHeight!!)
                println("1pos della pedina: w - " + x + "; h - " + y)

            }
            println("2pos della pedina: w - " + player.x + "; h - " + player.y)
            layout.apply {
                addView(player)
            }
            layout.gameBoard.viewTreeObserver.addOnGlobalLayoutListener {
                if(u.position==0)  { player.visibility = View.INVISIBLE }

                when (map[u.position!!]) {
                    1 -> {
                        player.translationX =
                                layout.gameBoard.left + (layout.gameBoard.width.toFloat()) * getXYmodsFromPosition(
                                        u.position!!
                                ).first - pawnWidth!!.toFloat() / 2
                        player.translationY =
                                layout.gameBoard.top + (layout.gameBoard.height.toFloat()) * getXYmodsFromPosition(
                                        u.position!!
                                ).second - pawnHeight!!.toFloat() / 2
                        println("SIAMO IN 1")
                        println("31pos della pedina: w - " + player.x + "; h - " + player.y)
                        println("megaprintone: \n" +
                                "layout.gameBoard.left: " + layout.gameBoard.left + ";  \n" +
                                "layout.gameBoard.width.toFloat(): " + layout.gameBoard.width.toFloat() + "; \n" +
                                "getXYmodsFromPosition(u.position!!).first: " + getXYmodsFromPosition(u.position!!).first)

                    }
                    2 -> {
                        player.translationX =
                                layout.gameBoard.left + (layout.gameBoard.width.toFloat()) * getXYmodsFromPosition(
                                        u.position!!
                                ).first - pawnWidth!!.toFloat() * 3 / 4
                        player.translationY =
                                layout.gameBoard.top + (layout.gameBoard.height.toFloat()) * getXYmodsFromPosition(
                                        u.position!!
                                ).second - pawnHeight!!.toFloat() * 3 / 4

                        map[u.position!!] = 22
                        println("SIAMO IN 2")
                    }
                    22 -> {
                        player.translationX =
                                layout.gameBoard.left + (layout.gameBoard.width.toFloat()) * getXYmodsFromPosition(
                                        u.position!!
                                ).first - pawnWidth!!.toFloat() / 4
                        player.translationY =
                                layout.gameBoard.top + (layout.gameBoard.height.toFloat()) * getXYmodsFromPosition(
                                        u.position!!
                                ).second - pawnHeight!!.toFloat() / 4
                        println("SIAMO IN 22")
                    }
                    3 -> {
                        //AGGIUNGERE POP UP
                        println("SIAMO IN 3")
                    }
                }
            }
        }
    }

    //Prende in input il numero della casella e ritorna i moltiplicatori x ed y relativi a quella casella
    fun getXYmodsFromPosition(position: Int): Pair<Float, Float> {
        var (x, y) = Pair(1F, 1F)
        when (position) {
            1 -> {
                println("position == 1")
                x = 75 / 100F
                y = 6 / 100F
            }
            2 -> {
                println("position == 2")
                x = 62 / 100F
                y = 5 / 100F
            }
            3 -> {
                println("position == 3")
                x = 51 / 100F
                y = 5 / 100F
            }
            4 -> {
                println("position == 4")
                x = 41 / 100F
                y = 5 / 100F
            }
            5 -> {
                println("position == 5")
                x = 30 / 100F
                y = 7 / 100F
            }
            6 -> {
                println("position == 6")
                x = 22 / 100F
                y = 11 / 100F
            }
            7 -> {
                println("position == 7")
                x = 22 / 100F
                y = 17 / 100F
            }
            8 -> {
                println("position == 8")
                x = 27 / 100F
                y = 22 / 100F
            }
            9 -> {
                println("position == 9")
                x = 36 / 100F
                y = 22 / 100F
            }
            10 -> {
                println("position == 10")
                x = 46 / 100F
                y = 20 / 100F
            }
            11 -> {
                println("position == 11")
                x = 56 / 100F
                y = 21 / 100F
            }
            12 -> {
                println("position == 12")
                x = 65 / 100F
                y = 22 / 100F
            }
            13 -> {
                println("position == 13")
                x = 75 / 100F
                y = 25 / 100F
            }
            14 -> {
                println("position == 14")
                x = 79 / 100F
                y = 35 / 100F
            }
            15 -> {
                println("position == 15")
                x = 70 / 100F
                y = 40 / 100F
            }
            16 -> {
                println("position == 16")
                x = 59 / 100F
                y = 40 / 100F
            }
            17 -> {
                println("position == 17")
                x = 49 / 100F
                y = 40 / 100F
            }
            18 -> {
                println("position == 18")
                x = 39 / 100F
                y = 39 / 100F
            }
            19 -> {
                println("position == 19")
                x = 29 / 100F
                y = 38 / 100F
            }
            20 -> {
                println("position == 20")
                x = 23 / 100F
                y = 44 / 100F
            }
            21 -> {
                println("position == 21")
                x = 22 / 100F
                y = 52 / 100F
            }
            22 -> {
                println("position == 22")
                x = 26 / 100F
                y = 59 / 100F
            }
            23 -> {
                println("position == 23")
                x = 34 / 100F
                y = 62 / 100F
            }
            24 -> {
                println("position == 24")
                x = 44 / 100F
                y = 63 / 100F
            }
            25 -> {
                println("position == 25")
                x = 54 / 100F
                y = 63 / 100F
            }
            26 -> {
                println("position == 26")
                x = 64 / 100F
                y = 64 / 100F
            }
            27 -> {
                println("position == 27")
                x = 72 / 100F
                y = 68 / 100F
            }
            28 -> {
                println("position == 28")
                x = 79 / 100F
                y = 75 / 100F
            }
            29 -> {
                println("position == 29")
                x = 81 / 100F
                y = 84 / 100F
            }
            30 -> {
                println("position == 30")
                x = 78 / 100F
                y = 93 / 100F
            }
            31 -> {
                println("position == 31")
                x = 68 / 100F
                y = 95 / 100F
            }
            32 -> {
                println("position == 32")
                x = 56 / 100F
                y = 95 / 100F
            }
            33 -> {
                println("position == 33")
                x = 46 / 100F
                y = 95 / 100F
            }
            34 -> {
                println("position == 34")
                x = 35 / 100F
                y = 95 / 100F
            }
            35 -> {
                println("position == 35")
                x = 24 / 100F
                y = 95 / 100F
            }

            else -> { // Note the block
                print("position is not in range 1 - 35")
            }
        }
        return Pair(x, y)
    }

    var imageListener = ImageListener { position, imageView -> imageView.setImageResource(sampleImages.get(position)) }

    fun setStartButtonEnabled() {
        but_join.visibility = View.GONE
        but_start.visibility=View.VISIBLE
        but_start.isEnabled = true
    }

    fun setStartButtonDisabled() {
        but_join.visibility = View.GONE
        but_start.visibility=View.VISIBLE
        but_start.isEnabled = false
    }

    fun setJoinButtonEnabled() {
        but_start.visibility = View.GONE
        but_join.visibility=View.VISIBLE
        but_join.isEnabled = true
    }

    fun setJoinButtonDisabled() {
        but_start.visibility = View.GONE
        but_join.visibility=View.VISIBLE
        but_join.isEnabled = false
    }
}