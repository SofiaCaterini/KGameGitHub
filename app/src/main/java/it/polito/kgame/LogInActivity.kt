package it.polito.kgame

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.autofill.AutofillManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LogInActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var sharedPreferences: SharedPreferences
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val fbUser: FirebaseUser? = auth.currentUser
    var isRemembered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        isRemembered = sharedPreferences.getBoolean("CHECKBOX", false)

        if (isRemembered) {
            println("Intent isRemembered")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val view: View = findViewById(R.id.sfondologin)
        view.setOnClickListener { hideKeyboard(this@LogInActivity) }

        tv_register.setOnClickListener {
            startActivity(Intent(this@LogInActivity, RegisterActivity::class.java))
        }

        btn_login.setOnClickListener {
            when {
                TextUtils.isEmpty(et_login_email.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                            this@LogInActivity,
                            R.string.req_mail,
                            Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_login_password.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                            this@LogInActivity,
                            R.string.req_pw,
                            Toast.LENGTH_SHORT
                    ).show()


                }
                else -> {
                    val email: String = et_login_email.text.toString().trim { it <= ' ' }
                    val password: String = et_login_password.text.toString().trim { it <= ' ' }
                    val checked: Boolean = checkBox.isChecked

                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("EMAIL", email)
                    editor.putString("PASSWORD", password)
                    editor.putBoolean("CHECKBOX", checked)
                    editor.apply()

                    println("dentro else")
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->

                                println("Dentro sign in")
                                if (task.isSuccessful) {
                                    println("task successful")
                                    val user = auth.currentUser
                                    updateUI(user, email)
                                } else {
                                    updateUI(null, null)
                                    Toast.makeText(
                                            this@LogInActivity,
                                            task.exception!!.message.toString(),
                                            Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                }
            }


        }

    }


    private fun hideKeyboard(context: Activity) {
        val inputMethodManager =
                context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if (context.currentFocus != null) {

            inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }

    /*public override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser
        updateUI(currentUser, null)
    }*/

    private fun updateUI(currentUser: FirebaseUser?, email:String?) {
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {

                Toast.makeText(
                        this@LogInActivity,
                        R.string.succ_login,
                        Toast.LENGTH_SHORT
                ).show()

                GlobalScope.launch {
                    val user = DbManager.getUser(email)
                    println("userfamcode: $user")
                    print(user?.familyCode)

                    //se hai già codfam     thisUser.value?.familiyCode != null
                    if (user?.familyCode != null) {

                        println("ha cod")
                        val intent =
                                Intent(this@LogInActivity, MainActivity::class.java)
                        intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(
                                "user_id",
                                FirebaseAuth.getInstance().currentUser!!.uid
                        )

                    intent.putExtra("email_id", email)
                    startActivity(intent)
                    finish()
                }


                    //se non ce l'hai
                    if (user?.familyCode == null) {
                        /*if (isRemembered) {
                            println("Intent isRemembered")
                            val intent = Intent(this@LogInActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }*/
                        println("non ha cod")
                        val intent =
                                Intent(this@LogInActivity, SetUpProfileActivity::class.java)
                        intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(
                                "user_id",
                                FirebaseAuth.getInstance().currentUser!!.uid
                        )
                        intent.putExtra("email_id", email)
                        startActivity(intent)
                        finish()
                    }
                }
            }else {
                Toast.makeText(baseContext,"Conferma la tua mail", Toast.LENGTH_SHORT).show()
        }
        }
    }


    }



