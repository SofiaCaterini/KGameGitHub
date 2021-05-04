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
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.app_bar_main.*


class LogInActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    var isRemembered = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        isRemembered = sharedPreferences.getBoolean("CHECKBOX", false)

        if(isRemembered){
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val view: View = findViewById(R.id.sfondologin)
        view.setOnClickListener { hideKeyboard(this@LogInActivity) }

        tv_register.setOnClickListener{
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
                    editor.putBoolean("CHECKBOX",checked)
                    editor.apply()

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                                if (task.isSuccessful) {

                                    Toast.makeText(
                                            this@LogInActivity,
                                            R.string.succ_login,
                                            Toast.LENGTH_SHORT
                                    ).show()

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
                                } else {
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

        if(context.currentFocus!=null) {

            inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }

}
