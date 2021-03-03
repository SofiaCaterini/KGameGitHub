package it.polito.kgame.ui.account

<<<<<<< Updated upstream
=======
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
>>>>>>> Stashed changes
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import it.polito.kgame.R
<<<<<<< Updated upstream
=======
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_account.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
>>>>>>> Stashed changes

class AccountFragment : Fragment() {

<<<<<<< Updated upstream
    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        accountViewModel =
                ViewModelProvider(this).get(AccountViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_account, container, false)
        val textView: TextView = root.findViewById(R.id.text_account)
        accountViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
=======
class AccountFragment : Fragment(R.layout.fragment_account) {
    val adapter = ItemAdapterFamily()
    val viewModel by activityViewModels<AccountViewModel>()
    val REQUEST_CODE = 100

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_account)
        //i frammenti non sono lifecycleowner
        viewModel.data.observe(viewLifecycleOwner, Observer { data -> adapter.setData(data) })
        rv.layoutManager= LinearLayoutManager(requireContext())
        rv.adapter = adapter
        val tView: View = requireActivity().toolbar
        val nView: View = requireActivity().nav_view
        fun View.hideKeyboard(){
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
        view.setOnClickListener { view.hideKeyboard() }
        tView.setOnClickListener { view.hideKeyboard()  }
        nView.setOnClickListener { view.hideKeyboard()  }

        but_cambiaFoto.setOnClickListener {
            if (askForPermissions()) {
                // Permissions are already granted, do your stuff
                openGalleryForImage()
            }
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
        }
    }

    //permission

    fun isPermissionsAllowed(): Boolean {
        return if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            false
        } else true
    }

    fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity() as Activity,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(requireActivity() as Activity,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_CODE)
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>,grantResults: IntArray) {
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
                            val uri = Uri.fromParts("package", requireActivity().getPackageName(), null)
                            intent.data = uri
                            startActivity(intent)
                        })
                .setNegativeButton(R.string.annulla,null)
                .show()
    }

>>>>>>> Stashed changes
}