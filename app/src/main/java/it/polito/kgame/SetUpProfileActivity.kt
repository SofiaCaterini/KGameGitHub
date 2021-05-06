package it.polito.kgame

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_set_up_profile.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SetUpProfileActivity : AppCompatActivity() {

    //questo serve per la gallery
    private val REQUEST_CODE = 100
    private var profilePic : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up_profile)

        val view: View = findViewById(R.id.sfondoset)

        view.setOnClickListener { hideKeyboard(this@SetUpProfileActivity) }

        edit_family_setup.hint = "Nome famiglia"
        et_familyCode.hint = "Inserisci codice"
        profilePicBox.setOnClickListener {
            openGalleryForImage()
        }

        but_createFamily.setOnClickListener {
            if (edit_family_setup.text.isNullOrBlank()){
                edit_family_setup.error = "Inserisci nome famiglia!"
            } else{
                DbManager.setUpUserProfile(profilePic, null, edit_family_setup.text.toString(), this@SetUpProfileActivity)
                Toast.makeText(
                        this, "Codice famiglia creato con successo",
                        Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        but_joinFamily.setOnClickListener {

            if(!et_familyCode.text.isNullOrBlank()) {
                GlobalScope.launch {
                    if (DbManager.getFamilyComps(et_familyCode.text.toString()).isNullOrEmpty()){
                        //et_familyCode.error = "Inserisci un codice valido!"
                        println("Aggiungi un codice valido")
                    }else{
                        DbManager.setUpUserProfile(profilePic, et_familyCode.text.toString(),null,this@SetUpProfileActivity)
                        /*Toast.makeText(
                                this@SetUpProfileActivity, "Ti sei unito correttamente alla famiglia",
                                Toast.LENGTH_SHORT
                        ).show()*/
                        val intent = Intent(this@SetUpProfileActivity, MainActivity::class.java)
                        intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }


            } else {
                //ADD ERROR NOTE!!!
                et_familyCode.error = "Inserisci un codice valido!"
                println("Aggiungi un codice valido")
            }
        }

    }


    // access to gallery
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    // result of gallery call
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri: Uri? = data?.data
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            imgView_profilePic.setImageURI(uri) // handle chosen image

            //store acquired image
            profilePic = uri
        }
    }

    //permission
    private fun askForPermissions(): Boolean {

        fun isPermissionsAllowed(): Boolean {
            return ContextCompat.checkSelfPermission(
                this@SetUpProfileActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun showPermissionDeniedDialog() {
            AlertDialog.Builder(this@SetUpProfileActivity)
                .setTitle(R.string.permession_denied)
                .setMessage(R.string.impostazioni_app)
                .setPositiveButton(R.string.butt_imp_app,

                    DialogInterface.OnClickListener { _, _ ->
                        // send to app settings if permission is denied permanently
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            this@SetUpProfileActivity.packageName,
                            null
                        )
                        intent.data = uri
                        startActivity(intent)
                    })
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SetUpProfileActivity as Activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this@SetUpProfileActivity as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                    openGalleryForImage()
                } else {
                    // permission is denied, permission is asked again
                    askForPermissions()
                }
                return
            }
        }
    }

    private fun hideKeyboard(context: Activity) {
        val inputMethodManager =
            context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if(context.currentFocus!=null){
            inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }
}