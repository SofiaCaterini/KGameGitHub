package it.polito.kgame.ui.account

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import it.polito.kgame.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : Fragment(R.layout.fragment_account) {
    val adapter = ItemAdapterFamily()
    val viewModel by activityViewModels<AccountViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toolbar
        requireActivity().toolbar.setBackgroundResource(R.color.toolbar_account)
        //i frammenti non sono lifecycleowner
        viewModel.data.observe(viewLifecycleOwner, Observer { data-> adapter.setData(data) })
        rv.layoutManager= LinearLayoutManager(requireContext())
        rv.adapter = adapter
        val tView: View = requireActivity().toolbar
        val nView: View = requireActivity().nav_view
        fun View.hideKeyboard(){
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE ) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
        view.setOnClickListener { view.hideKeyboard() }
        tView.setOnClickListener { view.hideKeyboard()  }
        nView.setOnClickListener { view.hideKeyboard()  }
    }


}