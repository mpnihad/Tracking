package com.androiddevs.runningappyt.ui.presenter

import android.Manifest
import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.adapter.RunAdapter
import com.androiddevs.runningappyt.other.Constant
import com.androiddevs.runningappyt.other.TrackingUtility
import com.androiddevs.runningappyt.other.TrackingUtility.hasLocationPermission
import com.androiddevs.runningappyt.ui.viewmodels.MainViewModel
import com.androiddevs.runningappyt.ui.viewmodels.SORT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {


    private val viewModel: MainViewModel by viewModels()

    private lateinit var  runAdapter: RunAdapter

    @Inject
    lateinit var name:String

    companion object {
        var perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(ACCESS_FINE_LOCATION)
        }
    }

    private fun setUpRecyclerView() = rvRuns.apply{
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())


    }

    private fun setUpTitle() {


        requireActivity().tvToolbarTitle.text = "Lets go, $name!"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermission()
        setUpRecyclerView()

        setUpObservers()


        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
        when(viewModel.sort){
            SORT.DATE -> spFilter.setSelection(0)
            SORT.SPEED -> spFilter.setSelection(3)
            SORT.TIME ->spFilter.setSelection(1)
            SORT.DISTANCE ->spFilter.setSelection(2)
            SORT.CALORIES ->spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> viewModel.sortBy(SORT.DATE)
                    1 -> viewModel.sortBy(SORT.TIME)
                    2 -> viewModel.sortBy(SORT.DISTANCE)
                    3 -> viewModel.sortBy(SORT.SPEED)
                    4 -> viewModel.sortBy(SORT.CALORIES)

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }



    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
//        setUpTitle()
    }

    private fun setUpObservers() {
        viewModel.finalList.observe(viewLifecycleOwner){

            runAdapter .submitList(it)

        }
    }

    private fun requestPermission() {

        if (TrackingUtility.hasLocationPermission(requireContext())) {
            return

        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            EasyPermissions.requestPermissions(
                this,
                "You need to accept the location permission to use the app",
                Constant.REQUEST_CODE_LOCATION_PERMISSION,
                ACCESS_FINE_LOCATION,
//                ACCESS_COARSE_LOCATION

            )

        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept the location permission to use the app",
                Constant.REQUEST_CODE_LOCATION_PERMISSION,
                ACCESS_FINE_LOCATION,
//                ACCESS_COARSE_LOCATION
            )
//
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {



            // only after having foreground permission we can ask for background permission
            if (!hasLocationPermission(requireContext())) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), Constant.REQUEST_CODE_LOCATION_PERMISSION)

            }
        }

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()


        } else {

//            EasyPermissions.requestPermissions(
//                this,
//                "You need to accept the location permission to use the app",
//                Constant.REQUEST_CODE_LOCATION_PERMISSION,
////                ACCESS_FINE_LOCATION,
////                ACCESS_COARSE_LOCATION,
//                ACCESS_BACKGROUND_LOCATION
//
//            )

            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}