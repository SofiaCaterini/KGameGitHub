package it.polito.kgame

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.autofill.AutofillManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_account.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        val view: View = findViewById(R.id.sfondoreg)

        view.setOnClickListener { hideKeyboard(this@RegisterActivity) }


        tv_login.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,LogInActivity::class.java))
        }

        btn_register.setOnClickListener {
            when {

                //errori vari  !!!AGGIUNGERE CONTROLLO CONFERMA PASSWORD UGUALE ALLA PW
                TextUtils.isEmpty(et_register_email.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                            this@RegisterActivity,
                            R.string.req_mail,
                            Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_register_password.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                            this@RegisterActivity,
                            R.string.req_pw,
                            Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_register_password2.text.toString().trim { it <= ' ' }) -> {

                    Toast.makeText(
                            this@RegisterActivity,
                            R.string.req_pw2,
                            Toast.LENGTH_SHORT
                    ).show()
                }

                !(et_register_password.text.toString().equals(et_register_password2.text.toString())) -> {
                    Toast.makeText(
                            this@RegisterActivity,
                            "Inserisci password uguali",
                            Toast.LENGTH_SHORT
                    ).show()

                }

                //dati ok
                else -> {
                    val email: String = et_register_email.text.toString().trim { it <= ' ' }
                    val password: String = et_register_password.text.toString().trim { it <= ' ' }

                    //crei utente
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->

                                if (task.isSuccessful) {
                                    //logIn

                                    auth.signInWithEmailAndPassword(email, password)
                                    //registra utente in db: FirebaseFirestore
                                    DbManager.registerUser(et_nickame.text.toString(), et_register_email.text.toString() )

                                    Toast.makeText(
                                            this@RegisterActivity,
                                            R.string.succ_signin,
                                            Toast.LENGTH_SHORT
                                    ).show()

                                    val user = auth.currentUser

                                    user?.sendEmailVerification()
                                            ?.addOnCompleteListener { task->

                                                if(task.isSuccessful) {
                                                    startActivity(Intent(this, LogInActivity::class.java))
                                                    finish()
                                                }
                                            }

                                } else {
                                    Toast.makeText(
                                            this@RegisterActivity,
                                            task.exception!!.message.toString(),
                                            Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                }
            }
        }
    }


    fun hideKeyboard(context: Activity) {
        val inputMethodManager =
                context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if(context.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }



}