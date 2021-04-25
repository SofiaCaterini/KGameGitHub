package it.polito.kgame.ui.account


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.navigation.NavigationView


import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.StorageReference
//import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso
import it.polito.kgame.DbManager
import it.polito.kgame.Pedina

import it.polito.kgame.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.fragment_account.*


class AccountFragment : Fragment(R.layout.fragment_account) {
    private val adapter = ItemAdapterFamily()
    private val viewModel by activityViewModels<AccountViewModel>()
    //questo serve per la gallery
    private val REQUEST_CODE = 100

    private var pawnCode: Int = 0
    private var switch: Int? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

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

            //inserimento dati utente nell'header, nel caso venissero aggiornati
            val header = requireActivity().findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
            header.findViewById<TextView>(R.id.navHeadNickname)?.text = viewModel.thisUser.value?.username
            header.findViewById<ImageView>(R.id.navHeadProfileImg)?.let { Picasso.get().load(viewModel.thisUser.value?.profileImg).into(it)  }

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
            if (switch == 0) switch = 2
            activateUpdateButton(switch ?: 1, null)

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
            if (requireActivity().currentFocus!=null){
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
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
        edit_nickname.addTextChangedListener{
            val imm = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            if(imm.isAcceptingText) {
                viewModel.changeUsername(edit_nickname.text.toString())
                if (switch == 0) switch = 2
                activateUpdateButton(switch ?: 1, null)
            }
        }


        requireActivity().discardUpdates.setOnClickListener {
            requireActivity().recreate()
            viewModel.discardUpdates()
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
            //send acquired image to saveUpdates method
            viewModel.changeImg(uri.toString())
            if (switch == 1) switch = 2
            activateUpdateButton(switch ?: 0, uri)
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


    private fun changePawnView() {
        imageViewpedina.setImageResource(Pedina.pedina(pawnCode))
        viewModel.changePawn(pawnCode)
    }

    var uriBuffer : Uri? = null
    private fun activateUpdateButton(case: Int, uri: Uri?) {
        requireActivity().saveUpdates.visibility = View.VISIBLE
        requireActivity().discardUpdates.visibility = View.VISIBLE
        when (case) {
            0 -> {  //case where only profile image is getting uploaded
                switch = 0
                uriBuffer= uri
                requireActivity().saveUpdates.setOnClickListener {
                    DbManager.uploadProfileImg(requireContext(), uri)
                    Toast.makeText(requireContext(), "Stai salvando la nuova immagine profilo", Toast.LENGTH_SHORT).show()
                    requireActivity().saveUpdates.visibility = View.GONE
                    requireActivity().discardUpdates.visibility = View.GONE
                    switch=null
                }
            }
            1 -> {  //case where only pawn/nickname is getting uploaded
                    switch=1
                    requireActivity().saveUpdates.setOnClickListener {
                        viewModel.saveUpdates(requireContext())
                        Toast.makeText(requireContext(), "Stai salvando le nuove impostazioni", Toast.LENGTH_SHORT).show()
                        requireActivity().saveUpdates.visibility = View.GONE
                        requireActivity().discardUpdates.visibility = View.GONE
                        switch=null
                    }
            }
            2 -> {  //case where both profile image and pawn/nickname are getting uploaded
                requireActivity().saveUpdates.setOnClickListener {
                    DbManager.uploadProfileImg(requireContext(), uri?: uriBuffer)
                    viewModel.saveUpdates(requireContext())
                    Toast.makeText(requireContext(), "Stai salvando le nuove impostazioni!", Toast.LENGTH_SHORT).show()
                    requireActivity().saveUpdates.visibility = View.GONE
                    requireActivity().discardUpdates.visibility = View.GONE
                    switch=null
                }
            }
        }
    }


//    private fun pedina(id : Int?) : Int {
//        when (id) {
//            0 -> {
//                return R.drawable.dog //imageviewpedina.setImageResource(idpedina)
//            }
//            1 -> {
//                return R.drawable.lion
//            }
//            2 -> {
//                return R.drawable.owl
//            }
//            R.drawable.dog -> {
//                return 0
//            }
//            R.drawable.lion -> {
//                return 1
//            }
//            R.drawable.owl -> {
//                return 2
//            }
//            null -> {
//                return R.drawable.dog
//            }
//        }
//        return 0
//    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().saveUpdates.visibility = View.GONE
        requireActivity().discardUpdates.visibility = View.GONE

    }
}