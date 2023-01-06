package com.androiddevs.runningappyt.ui.presenter

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.db.Run
import com.androiddevs.runningappyt.other.Constant.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.other.Constant.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.other.Constant.ACTION_STOP_SERVICE
import com.androiddevs.runningappyt.other.Constant.CANCEL_TRACKING_DIALOG
import com.androiddevs.runningappyt.other.Constant.FIREBASE_ANALYTICS_CALORIES
import com.androiddevs.runningappyt.other.Constant.FIREBASE_ANALYTICS_CANCEL_RUN
import com.androiddevs.runningappyt.other.Constant.FIREBASE_ANALYTICS_DATE
import com.androiddevs.runningappyt.other.Constant.FIREBASE_ANALYTICS_DISTANCE
import com.androiddevs.runningappyt.other.Constant.FIREBASE_ANALYTICS_SPEED
import com.androiddevs.runningappyt.other.Constant.MAP_ZOOM
import com.androiddevs.runningappyt.other.Constant.POLYLINE_COLOR
import com.androiddevs.runningappyt.other.Constant.POLYLINE_WIDTH
import com.androiddevs.runningappyt.other.TrackingUtility
import com.androiddevs.runningappyt.service.Polyline
import com.androiddevs.runningappyt.service.TrackingService
import com.androiddevs.runningappyt.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null

    private var menu: Menu? = null


    private var isTracking = false
    private var pathPoint = mutableListOf<Polyline>()

    @set:Inject
    var weight = 80.0f

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }

        setOnClickListener()
        askForNotificationPermission()
        subscribeToObservers()

        setUpMenuItems()

        if(savedInstanceState!=null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {

                sendDataToFirebaseAnalytics()
                stopRun()
            }
        }

    }

    private fun sendDataToFirebaseAnalytics() {
        var cancelledRun = getRun(null)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = cancelledRun.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = dateFormat.format(calendar.time)


        val bundle = Bundle().apply {
            putFloat(FIREBASE_ANALYTICS_SPEED, cancelledRun.avgSpeedInKMH)
            putInt(FIREBASE_ANALYTICS_CALORIES, cancelledRun.caloriesBurned)
            putInt(FIREBASE_ANALYTICS_DISTANCE, cancelledRun.distanceInMeters)
            putString(FIREBASE_ANALYTICS_DATE, date)
        }

        firebaseAnalytics.logEvent(FIREBASE_ANALYTICS_CANCEL_RUN, bundle)
    }


    fun addAllPolyLines() {
        for (polyline in pathPoint) {
            var polyLinesOption = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polyLinesOption)

        }


    }


    private fun setUpMenuItems() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

                menuInflater.inflate(R.menu.toolbar_tracking_menu, menu)
                this@TrackingFragment.menu = menu
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return when (menuItem.itemId) {
                    R.id.miCancelTracking -> {
                        showCancelTrackingDialog()
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {

                super.onPrepareMenu(menu)

                if (curTimeOnMillis > 0L) {
                    menu.getItem(0)?.isVisible = true
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


    }


    private fun addLatestPolyLines() {
        if (pathPoint.isNotEmpty() && pathPoint.last().size > 1) {
            val preLastLating = pathPoint.last()[pathPoint.last().size - 2]
            var lastLatLng = pathPoint.last().last()
            var polyLinesOption = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLating)
                .add(lastLatLng)

            map?.addPolyline(polyLinesOption)
//            moveCameraToUser()


        }

    }

    private fun moveCameraToUser() {
        if (pathPoint.isNotEmpty() && pathPoint.last().isNotEmpty()) {

            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoint.last().last(),
                    MAP_ZOOM

                )
            )

        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeOnMillis > 0L) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE


        } else if(isTracking) {
            menu?.getItem(0)?.isVisible = true
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE

        }
    }


    private fun showCancelTrackingDialog() {

        CancelTrackingDialog().apply {
            setYesListener{

                sendDataToFirebaseAnalytics()
                stopRun()
            }
        }.show(parentFragmentManager,CANCEL_TRACKING_DIALOG)

    }

    private fun stopRun() {
        tvTimer.text ="00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private var curTimeOnMillis = 0L

    private fun askForNotificationPermission() {

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                if (!shouldShowRequestPermissionRationale("You need this permission for using tracker feature")) {
                    showSettingDialog()
                }

            } else {
                viewModel.isNotificationEnabled = isGranted ?: false
            }


        }


        if (Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        }
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
            .setNegativeButton(
                "Cancel"
            ) { _, _ ->

                Toast.makeText(requireContext(), "Permission needed", Toast.LENGTH_SHORT).show()
                navHostFragment.findNavController().popBackStack()
            }
            .show()
    }


    private fun setOnClickListener() {
        btnToggleRun.setOnClickListener {
            toggleRun()
//            sendCommandToService(Constant.ACTION_START_OR_RESUME_SERVICE)
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDB()
        }


    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)

            menu?.getItem(0)?.isVisible = true
        } else {

            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }


    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoint = it
            addLatestPolyLines()
            moveCameraToUser()
        }

        TrackingService.timRunInMillis.observe(viewLifecycleOwner) {
            curTimeOnMillis = it
            var formattedTime = TrackingUtility.getFormattedStopwatchTime(curTimeOnMillis, true)
            tvTimer.text = formattedTime
        }
    }

    fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun zoomToSeeWholeTrack() {

        val bounce = LatLngBounds.builder()
        pathPoint.forEach { polyline ->
            polyline.forEach { position ->
                bounce.include(position)
            }

        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounce.build(),
                mapView.width, mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }


    private  fun endRunAndSaveToDB(){

    map?.let {
        it.snapshot { btm->

            var run = getRun(btm)
            viewModel.insertRun(run)

            Snackbar.make(requireActivity().findViewById(R.id.rootView),
            "Run Saved Successfully",Snackbar.LENGTH_LONG).show()
            stopRun()
        }
    }
    }

    private fun getRun(btm: Bitmap?): Run {
        var distanceInMeter = 0
        for (polyline in pathPoint) {
            distanceInMeter += TrackingUtility.calculatePolylineLength(polyline).toInt()

        }

        val avgSpeed =
            round((distanceInMeter / 1000f) / (curTimeOnMillis / 1000f / 60 / 60) * 10) / 10f
        val dateTimeStamp = Calendar.getInstance().timeInMillis
        val caloriesBurned = ((distanceInMeter / 1000f) * weight).toInt()

        return Run(btm, dateTimeStamp, avgSpeed, distanceInMeter, curTimeOnMillis, caloriesBurned)

    }


    override fun onResume() {
        super.onResume()

        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.let {
            it.onDestroy()
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.let{
            it.onSaveInstanceState(outState)
        }
    }
}