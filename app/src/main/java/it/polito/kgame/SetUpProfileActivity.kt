package it.polito.kgame

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager

class SetUpProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up_profile)
        val view: View = findViewById(R.id.sfondoset)
        view.setOnClickListener { hideKeyboard(this@SetUpProfileActivity) }
    }
    private fun hideKeyboard(context: Activity) {
        val inputMethodManager =
            context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if(context.currentFocus!=null){
            inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }
}