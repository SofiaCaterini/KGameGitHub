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

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val view: View = findViewById(R.id.sfondoreg)
        view.setOnClickListener { hideKeyboard(this@RegisterActivity) }

        val db : FirebaseFirestore

        val autofillManager = getSystemService(AutofillManager::class.java)
        autofillManager.disableAutofillServices()
        et_register_email.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        et_register_password.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO

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

                //dati ok
                else -> {
                    val email: String = et_register_email.text.toString().trim { it <= ' ' }
                    val password: String = et_register_password.text.toString().trim { it <= ' ' }

                    //crei utente
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->

                                if (task.isSuccessful) {
                                    val firebaseUser: FirebaseUser = task.result!!.user!!

                                    Toast.makeText(
                                        this@RegisterActivity,
                                        R.string.succ_signin,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent =
                                        Intent(this@RegisterActivity, setUpProfileActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                   /* intent.putExtra("user_id", firebaseUser.uid)
                                    intent.putExtra("email_id", email)*/
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })

                    //registra utente in db (FirebaseFirestore)



                }
            }

        }
    }
    fun hideKeyboard(context: Activity) {
        val inputMethodManager =
            context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
    }
}