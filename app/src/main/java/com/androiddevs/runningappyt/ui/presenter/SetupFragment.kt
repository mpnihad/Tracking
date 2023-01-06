package com.androiddevs.runningappyt.ui.presenter

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.other.Constant.KEY_FIRST_TIME_TOGGLED
import com.androiddevs.runningappyt.other.Constant.KEY_NAME
import com.androiddevs.runningappyt.other.Constant.KEY_WEIGHT
import com.androiddevs.runningappyt.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {


    private  val viewModel : SettingsViewModel by viewModels()

    @set:Inject
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isFirstAppOpen) {
            var navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()


            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )

        }
        tvContinue.setOnClickListener {
            val name = etName.text.toString()
            val weight = etWeight.text.toString()

            val success = viewModel.writePersonalDataToSharePref(name,weight)
            if (success) {

                val toolbarText = "Lets go, ${name}!"
                requireActivity().tvToolbarTitle.text = toolbarText


                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }
            else {
                Toast.makeText(requireContext(), "Please enter all the fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }



}