package com.androiddevs.runningappyt.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.db.RunDao
import com.androiddevs.runningappyt.other.Constant
import com.androiddevs.runningappyt.other.GoogleWalletDetails
import com.androiddevs.runningappyt.other.GoogleWalletDetails.ADD_WALLET_REQUEST_CODE
import com.androiddevs.runningappyt.ui.presenter.AddWalletDialog
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var runDao: RunDao


    @Inject
    lateinit var name: String

    @set:Inject
    var isFirstTime: Boolean = true

    private lateinit var walletClient: PayClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToTrackingFragmentIfNeeded(intent)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        //prevent the bottom navigation button from loading the app again
        bottomNavigationView.setOnItemReselectedListener(::setUpFirebase)


        if (!isFirstTime) {
            setUpTitle()
        }

        walletClient = Pay.getClient(this)

        setUpGooglePayWallet()

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {

                    R.id.settingsFragment, R.id.runFragment, R.id.trackingFragment, R.id.statisticFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE

                }


            }
    }

    private fun setUpGooglePayWallet() {
        walletClient
            .getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
            .addOnSuccessListener { status ->
                if (status == PayApiAvailabilityStatus.AVAILABLE)
                    showAddWallet()
            }
            .addOnFailureListener {
                // Hide the button and optionally show an error message
                Toast.makeText(this, "Not available : google wallet", Toast.LENGTH_SHORT).show()

            }

    }

    private fun showAddWallet() {

        AddWalletDialog().apply {
            setYesListener {

                sendOfferToWallet()
            }
        }.show(supportFragmentManager, Constant.ADD_WALLET_DIALOG)


    }

    private fun sendOfferToWallet() {

        walletClient.savePasses(GoogleWalletDetails.newObjectJson, this, ADD_WALLET_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == ADD_WALLET_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    //TODO
                }
                // Pass saved successfully. Consider informing the user.
                RESULT_CANCELED -> {
                    // Save canceled
                }

                PayClient.SavePassesResult.SAVE_ERROR -> data?.let { intentData ->
                    val errorMessage = intentData.getStringExtra(PayClient.EXTRA_API_ERROR_MESSAGE)
                    // Handle error. Consider informing the user.
                    Log.d("TAG", "onActivityResult: ${errorMessage} ")
                }

                else -> {
                    // Handle unexpected (non-API) exception
                }
            }
        }
    }

    private fun setUpFirebase(menuItem: MenuItem) {
        var remote = FirebaseRemoteConfig.getInstance()
        val config = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remote.setConfigSettingsAsync(config)
        remote.setDefaultsAsync(R.xml.remote_config_defaults)
        remote.fetchAndActivate().addOnCompleteListener {
            Log.d(
                "TAG",
                "provideFirebaseRemoteConfig: ${remote.getBoolean(Constant.FIREBASE_REMOTE_CONFIG_SHOW_LINE_GRAPH)}"
            )
            remote.getBoolean(Constant.FIREBASE_REMOTE_CONFIG_SHOW_LINE_GRAPH)
            Log.d("TAG", "setUpFirebase: ${it.result}")
        }
            .addOnFailureListener {
                Log.d("TAG", "setUpFirebase: ${it.localizedMessage}")
            }


//        return remote
    }

    private fun setUpTitle() {


        tvToolbarTitle.text = "Lets go, $name!"
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == Constant.ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }
}
