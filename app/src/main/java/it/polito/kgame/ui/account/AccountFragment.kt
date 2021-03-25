package it.polito.kgame.ui.account


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.View
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso

import it.polito.kgame.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_account.*


class AccountFragment : Fragment(R.layout.fragment_account) {
    val adapter = ItemAdapterFamily()
    val viewModel by activityViewModels<AccountViewModel>()
    val REQUEST_CODE = 100
    private var mImageUri: Uri? = null
    private var mStorageRef: StorageReference? = null
    private var mUploadTask: StorageTask<*>? = null
    private var db : FirebaseFirestore? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        db = viewModel.db

        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_account)
        //i frammenti non sono lifecycleowner
        viewModel.data.observe(viewLifecycleOwner, Observer { data -> adapter.setData(data) })
        rv.layoutManager= LinearLayoutManager(requireContext())
        rv.adapter = adapter
        val tView: View = requireActivity().toolbar
        val nView: View = requireActivity().nav_view

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
       

        //keyboard management
        fun View.hideKeyboard(){
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
        view.setOnClickListener { view.hideKeyboard() }
        tView.setOnClickListener { view.hideKeyboard()  }
        nView.setOnClickListener { view.hideKeyboard()  }

        readImgProfilo()
        //profile picture management
        but_cambiaFoto.setOnClickListener {
            if (askForPermissions()) {
                // Permissions are already granted, do your stuff
                openGalleryForImage()
            }
        }


        readNickname()
        edit_nickname.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                updateNickname()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            imageView.setImageURI(data?.data) // handle chosen image
            mImageUri = data?.data
            uploadImgProfilo()
        }

    }

    //permission

    private fun isPermissionsAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun askForPermissions(): Boolean {
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
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    //  askForPermissions()
                }
                return
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.permesso_negato)
                .setMessage(R.string.impostazioni_app)
                .setPositiveButton(R.string.butt_imp_app,

                    DialogInterface.OnClickListener { dialogInterface, i ->
                        // send to app settings if permission is denied permanently
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            requireActivity().getPackageName(),
                            null
                        )
                        intent.data = uri
                        startActivity(intent)
                    })
                .setNegativeButton(R.string.annulla, null)
                .show()
    }


    fun updateNickname() {

        val nickname : String = "NICKNAME"
        var data : MutableMap<String,String> = mutableMapOf()
        data.put(nickname, edit_nickname.text.toString())


        db?.collection("Accounts")
                ?.document("pippo@sowlo.it")
                ?.update(data as Map<String, Any>)
                ?.addOnSuccessListener {
                    Toast.makeText(
                            requireContext(),
                            R.string.succ_newNN,
                            Toast.LENGTH_SHORT
                    ).show();
                    println("update Nickname success")
                }
                ?.addOnFailureListener {
                    Toast.makeText(
                            requireContext(),
                            R.string.fail_newNN,
                            Toast.LENGTH_SHORT
                    ).show();
                    println("update Nickname epic fail")
                }
    }

    fun readNickname() {
        val nickname: String = "NICKNAME"

        db?.collection("Accounts")
                ?.document("pippo@sowlo.it")
                ?.get()
                ?.addOnSuccessListener {
                    if (it.exists()) {
                        edit_nickname.setText(it.getString(nickname))
                    } else {
                        Toast.makeText(
                                requireContext(),
                                R.string.req_doc,
                                Toast.LENGTH_SHORT
                        ).show()

                    }
                }
                ?.addOnFailureListener {

                }
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR: ContentResolver = requireContext().contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadImgProfilo() {
        if (mImageUri != null) {
            val fileReference = mStorageRef!!.child(
                System.currentTimeMillis()
                    .toString() + "." + getFileExtension(mImageUri!!)
            )
            mUploadTask = fileReference.putFile(mImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_LONG).show()

                    var data : MutableMap<String,String> = mutableMapOf()
                    val img : String = "IMGPROFILO"
                    fileReference.downloadUrl.addOnCompleteListener () { taskSnapshot ->
                        var url = taskSnapshot.result
                        println ("url =" + url.toString())
                        data.put(img, url.toString())
                        db?.collection("Accounts")
                                ?.document("pippo@sowlo.it")
                                ?.update(data as Map<String, Any>)
                                ?.addOnSuccessListener {
                                    Toast.makeText(
                                            requireContext(),
                                            R.string.ok,
                                            Toast.LENGTH_SHORT
                                    )
                                }

                    }


                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

        } else {
            print("errore")
            Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
        }

    }

    fun readImgProfilo() {
        val img: String = "IMGPROFILO"

        db?.collection("Accounts")
                ?.document("pippo@sowlo.it")
                ?.get()
                ?.addOnSuccessListener {
                    if (it.exists()) {
                        Picasso.get().load(it.getString(img)).into(imageView)
                    } else {
                        Toast.makeText(
                                requireContext(),
                                R.string.req_doc,
                                Toast.LENGTH_SHORT
                        ).show()

                    }
                }
                ?.addOnFailureListener {

                }
    }

}