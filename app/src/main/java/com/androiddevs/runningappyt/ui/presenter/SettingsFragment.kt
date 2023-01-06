package com.androiddevs.runningappyt.ui.presenter

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.other.Constant.KEY_NAME
import com.androiddevs.runningappyt.other.Constant.KEY_WEIGHT
import com.androiddevs.runningappyt.repository.MainRepository
import com.androiddevs.runningappyt.ui.viewmodels.MainViewModel
import com.androiddevs.runningappyt.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.android.synthetic.main.fragment_setup.etName
import kotlinx.android.synthetic.main.fragment_setup.etWeight
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private  val viewModel : SettingsViewModel by viewModels()

    @Inject
    lateinit var sharedPref:SharedPreferences

    @Inject
    lateinit var name:String

    @set:Inject
     var weight : Float =80.0f


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
loadFieldFromSharedPref()
        btnApplyChanges.setOnClickListener{
            var nameText = etName.text.toString()
            var weightText = etWeight.text.toString()

            val success = viewModel.writePersonalDataToSharePref(nameText,weightText)

            if(success){



                    requireActivity().tvToolbarTitle.text = "Lets go, ${etName.text}!"


                Toast.makeText(requireContext(), "Save Successful", Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(requireContext(), "Please enter all the value", Toast.LENGTH_SHORT).show()

            }
        }
    }

    fun loadFieldFromSharedPref(){
        etName.setText( name)
        etWeight.setText( weight.toString())
    }

    private fun applyChnagesTpSharedPref(): Boolean{

        var nameText = etName.text.toString()
        var weightText = etWeight.text.toString()

        viewModel.writePersonalDataToSharePref(name,weightText)

        if(nameText.isEmpty() || weightText.isEmpty()){
            return false
        }
        else
        {
            sharedPref.edit().putString(KEY_NAME,nameText)
                .putFloat(KEY_WEIGHT,weight)
                .apply()
        }
        return  true


    }


}