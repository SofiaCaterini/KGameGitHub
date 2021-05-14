package it.polito.kgame

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        val sessionId = intent.getStringExtra("EXTRA_SESSION_ID")
        if (sessionId == "true"){
            carousel.isVisible = true
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_account, R.id.nav_calendar, R.id.nav_grow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    //Chiude la tastiera che rimane aperta dalla pagina del profilo
    fun hideKeyboard(pView: View?, pActivity: Activity) {
        var pView: View? = pView

        if (pView == null) {
            pView = pActivity.window.currentFocus
        }
        if (pView != null) {
            val imm = pActivity
                    .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(pView.getWindowToken(), 0)


        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity, menu)

        return true
    }

    fun LogOut(item: MenuItem?) {
        println("Hai cliccato")
        MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Sei sicuro di voler effettuare il logout?")
                .setPositiveButton("Sì") { _, _ ->
                    val editor: SharedPreferences.Editor = preferences.edit()
                    editor.clear()
                    editor.apply()

                    val intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                    finish()

                }
                .setNegativeButton("No"){ _, _ ->

                }
                .show()

    }

    fun DeleteAccount(item: MenuItem?){
        MaterialAlertDialogBuilder(this)
                .setTitle("Cancella account")
                .setMessage("Sei sicuro di voler cancellare il tuo account?")
                .setPositiveButton("Sì") { _, _ ->
                    //db operations
                    DbManager.deleteUser(this)

                    val editor: SharedPreferences.Editor = preferences.edit()
                    editor.clear()
                    editor.apply()

                    val intent= Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                    finish()

                }
                .setNegativeButton("No"){ _, _ ->

                }
                .show()
    }

    fun FamilyOut(item: MenuItem?){
        MaterialAlertDialogBuilder(this)
                .setTitle("Esci dalla famiglia")
                .setMessage("Sei sicuro di voler uscire dalla tua famiglia?")
                .setPositiveButton("Sì") { _, _ ->
                    //db operations
                    DbManager.deleteProfileInFamily(this)

                    val editor: SharedPreferences.Editor = preferences.edit()
                    editor.clear()
                    editor.apply()
                    val intent = Intent(this, SetUpProfileActivity::class.java)
                    startActivity(intent)
                    finish()

                }
                .setNegativeButton("No"){ _, _ ->

                }
                .show()

    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard(nav_view, this)

        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}