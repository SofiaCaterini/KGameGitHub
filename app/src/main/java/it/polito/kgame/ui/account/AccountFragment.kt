package it.polito.kgame.ui.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.kgame.R
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


    }
}