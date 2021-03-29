package it.polito.kgame.ui.account


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
//import com.google.firebase.storage.StorageReference
//import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso
import it.polito.kgame.DbManager
import it.polito.kgame.Pedina

import it.polito.kgame.R
import it.polito.kgame.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.fragment_account.*


class AccountFragment : Fragment(R.layout.fragment_account) {
    private val adapter = ItemAdapterFamily()
    private val viewModel by activityViewModels<AccountViewModel>()
    //questo serve per la gallery
    private val REQUEST_CODE = 100

    //    private var mStorageRef: StorageReference? = null
//    private var mUploadTask: StorageTask<*>? = null
    private var db: FirebaseFirestore? = null
    private var pawnCode: Int = 0
    private var switch: Int? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        db = viewModel.db

        //observe aspetta che il dato sia pronto per poi gestirlo
        viewModel.thisUser.observe(viewLifecycleOwner, Observer { user ->
            println("QUIIII: $user")

            //read profile pic
            Picasso.get().load(user.profileImg).into(imageView)

            //read pawnCode if exists or 0 and set pawn image
            if (user.pawnCode != null) {
                pawnCode = user.pawnCode!!
            }
            changePawnView()

            //read nickname
            edit_nickname.setText(user.username)

        })


        //change pawn image
        sx.setOnClickListener {
            if (pawnCode <= 0) {
                pawnCode = 2
            } else pawnCode--
            changePawnView()
            if (switch == 0) switch = 2
            activateUpdateButton(switch ?: 1, null)
        }

        dx.setOnClickListener {
            if (pawnCode >= 2) {
                pawnCode = 0
            } else pawnCode++
            changePawnView()
            println("switch1: $switch")

            if (switch == 0) switch = 2
            println("switch2: $switch")

            activateUpdateButton(switch ?: 1, null)
            println("switch3: $switch")

        }


        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_account)

        //i frammenti non sono lifecycleowner
        viewModel.data.observe(viewLifecycleOwner, Observer { data -> adapter.setData(data) })
        val tView: View = requireActivity().toolbar
        val nView: View = requireActivity().nav_view


        //keyboard management
        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
        view.setOnClickListener { view.hideKeyboard() }
        tView.setOnClickListener { view.hideKeyboard() }
        nView.setOnClickListener { view.hideKeyboard() }

        //profile picture management
        but_cambiaFoto.setOnClickListener {
            if (askForPermissions()) {
                // Permissions are already granted, do your stuff
                openGalleryForImage()
            }
        }

        //nickname edit text management
        edit_nickname.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
//                DbManager.updateNickname(requireContext(), edit_nickname.text.toString())
                viewModel.changeUsername(edit_nickname.text.toString())
                if (switch == 0) switch = 2
                activateUpdateButton(switch ?: 1, null)
            }
            false
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
            imageView.setImageURI(uri) // handle chosen image
            //send acquired image to db
//            DbManager.uploadImgProfilo(requireContext(), uri)
            viewModel.changeImg(uri.toString())
            println("switch1: $switch")

            if (switch == 1) switch = 2
            println("switch2: $switch")

            activateUpdateButton(switch ?: 0, uri)
            println("switch3: $switch")

        }
    }

    //permission
    private fun askForPermissions(): Boolean {

        fun isPermissionsAllowed(): Boolean {
            return ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun showPermissionDeniedDialog() {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.permession_denied)
                    .setMessage(R.string.impostazioni_app)
                    .setPositiveButton(R.string.butt_imp_app,

                            DialogInterface.OnClickListener { _, _ ->
                                // send to app settings if permission is denied permanently
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts(
                                        "package",
                                        requireActivity().packageName,
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
                            requireActivity() as Activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    )) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                        requireActivity() as Activity,
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

//    private fun showPermissionDeniedDialog() {
//        AlertDialog.Builder(requireContext())
//                .setTitle(R.string.permesso_negato)
//                .setMessage(R.string.impostazioni_app)
//                .setPositiveButton(R.string.butt_imp_app,
//
//                    DialogInterface.OnClickListener { dialogInterface, i ->
//                        // send to app settings if permission is denied permanently
//                        val intent = Intent()
//                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        val uri = Uri.fromParts(
//                            "package",
//                            requireActivity().getPackageName(),
//                            null
//                        )
//                        intent.data = uri
//                        startActivity(intent)
//                    })
//                .setNegativeButton(R.string.annulla, null)
//                .show()
//    }


//    fun updateNickname() {
//
//        var data : MutableMap<String,String> = mutableMapOf()
//        data.put(DbManager.NICKNAME, edit_nickname.text.toString())
//
//
//        db?.collection(DbManager.ACCOUNTS)
//                ?.document(viewModel.thisUser.mail)
//                ?.update(data as Map<String, Any>)
//                ?.addOnSuccessListener {
//                    Toast.makeText(
//                            requireContext(),
//                            R.string.succ_newNN,
//                            Toast.LENGTH_SHORT
//                    ).show();
//                    println("update Nickname success")
//                }
//                ?.addOnFailureListener {
//                    Toast.makeText(
//                            requireContext(),
//                            R.string.fail_newNN,
//                            Toast.LENGTH_SHORT
//                    ).show();
//                    println("update Nickname epic fail")
//                }
//    }

//    fun readNickname() {
//
//        db?.collection(DbManager.ACCOUNTS)
//                ?.document(viewModel.thisUser.mail)
//                ?.get()
//                ?.addOnSuccessListener {
//                    if (it.exists()) {
//                        edit_nickname.setText(it.getString(DbManager.NICKNAME))
//                    } else {
//                        Toast.makeText(
//                                requireContext(),
//                                R.string.req_doc,
//                                Toast.LENGTH_SHORT
//                        ).show()
//
//                    }
//                }
//                ?.addOnFailureListener {
//
//                }
//    }

//    private fun getFileExtension(uri: Uri): String? {
//        val cR: ContentResolver = requireContext().contentResolver
//        val mime = MimeTypeMap.getSingleton()
//        return mime.getExtensionFromMimeType(cR.getType(uri))
//    }
//
//    private fun uploadImgProfilo() {
//        if (mImageUri != null) {
//            val fileReference = mStorageRef!!.child(
//                System.currentTimeMillis()
//                    .toString() + "." + getFileExtension(mImageUri!!)
//            )
//            mUploadTask = fileReference.putFile(mImageUri!!)
//                .addOnSuccessListener { taskSnapshot ->
//                    Toast.makeText(
//                            requireContext(),
//                            "Upload successful",
//                            Toast.LENGTH_LONG
//                    ).show()
//
//                    var data : MutableMap<String,String> = mutableMapOf()
//
//                    fileReference.downloadUrl.addOnCompleteListener () { taskSnapshot ->
//                        var url = taskSnapshot.result
//                        println ("url =" + url.toString())
//                        data.put(DbManager.PROF_PIC, url.toString())
//                        viewModel.fbUser.value?.let {
//                            db?.collection(DbManager.ACCOUNTS)
//                                    ?.document(it.email)
//                                    ?.update(data as Map<String, Any>)
//                                    ?.addOnSuccessListener {
//                                        Toast.makeText(
//                                                requireContext(),
//                                                R.string.ok,
//                                                Toast.LENGTH_SHORT
//                                        )
//                                    }
//                        }
//
//                    }
//
//
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(
//                        context,
//                        e.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//        } else {
//            print("errore")
//            Toast.makeText(
//                    context,
//                    "No file selected",
//                    Toast.LENGTH_SHORT
//            ).show()
//        }
//
//    }

//    fun readImgProfilo() {
//
//        db?.collection(DbManager.ACCOUNTS)
//                ?.document(viewModel.thisUser.mail)
//                ?.get()
//                ?.addOnSuccessListener {
//                    if (it.exists()) {
//                        Picasso.get().load(it.getString(DbManager.PROF_PIC)).into(imageView)
//                    } else {
//                        Toast.makeText(
//                                requireContext(),
//                                R.string.req_doc,
//                                Toast.LENGTH_SHORT
//                        ).show()
//
//                    }
//                }
//                ?.addOnFailureListener {
//
//                }
//    }

    private fun changePawnView() {
        imageViewpedina.setImageResource(pedina(pawnCode))
        viewModel.changePawn(pawnCode)
//        DbManager.updatePawnCode(requireContext(),pawnCode)     //in questo modo carichi il dato sul db ogni volta che lo cambi. È meglio aggiungere un pulsante per salvare tutte le modifiche
    }

    var uriBuffer : Uri? = null
    private fun activateUpdateButton(case: Int, uri: Uri?) {
        requireActivity().saveUpdates.visibility = View.VISIBLE
        when (case) {
            0 -> {  //case where only profile image is getting uploaded
                switch = 0
                uriBuffer= uri
                requireActivity().saveUpdates.setOnClickListener {
                    DbManager.uploadProfileImg(requireContext(), uri)
                    Toast.makeText(requireContext(), "Stai salvando la nuova immagine profilo", Toast.LENGTH_SHORT).show()
                    requireActivity().saveUpdates.visibility = View.GONE
                    switch=null
                }
            }
            1 -> {  //case where only pawn/nickname is getting uploaded
                    switch=1
                    requireActivity().saveUpdates.setOnClickListener {
                        viewModel.saveUpdates(requireContext())
                        Toast.makeText(requireContext(), "Stai salvando le nuove impostazioni", Toast.LENGTH_SHORT).show()
                        requireActivity().saveUpdates.visibility = View.GONE
                        switch=null
                    }
            }
            2 -> {  //case where both profile image and pawn/nickname are getting uploaded
                requireActivity().saveUpdates.setOnClickListener {
                    DbManager.uploadProfileImg(requireContext(), uri?: uriBuffer)
                    viewModel.saveUpdates(requireContext())
                    Toast.makeText(requireContext(), "Stai salvando le nuove impostazioni!", Toast.LENGTH_SHORT).show()
                    requireActivity().saveUpdates.visibility = View.GONE
                    switch=null
                }
            }
        }
    }


    private fun pedina(id : Int?) : Int {
        when (id) {
            0 -> {
                return R.drawable.dog //imageviewpedina.setImageResource(idpedina)
            }
            1 -> {
                return R.drawable.lion
            }
            2 -> {
                return R.drawable.owl
            }
            R.drawable.dog -> {
                return 0
            }
            R.drawable.lion -> {
                return 1
            }
            R.drawable.owl -> {
                return 2
            }
            null -> {
                return R.drawable.dog
            }
        }
        return 0
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().saveUpdates.visibility = View.GONE
    }
}