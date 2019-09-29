package com.example.srcwh

import android.Manifest
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import android.app.PendingIntent
import androidx.fragment.app.Fragment
import android.content.pm.PackageManager
import android.location.Location
import android.provider.ContactsContract
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var schedule: List<ClientSchedule>

    private var locationRequestCallback: ((granted: Boolean, explain: Boolean?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DatabaseObj.user = DatabaseObj.getUserData()!!
        val networkHandler = NetworkHandler()
        networkHandler.getSchedule{generateView()}
        // first thing, we need to establish the database connection, and check if current userdata exists
        // getUserData() both initiates the database connection, and returns an user -object IF one exists.
        // if the user object is null, then there was no data. (usually meaning first time user)

        // setup the nfc reader
        setupNfc()

        // if the application was opened via nfc reader, this gets called
        if(intent != null){ processIncomingIntent(intent)}

        // fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // showAlertDialog()
    }

    private fun showAlertDialog() {
        val dialogHandler = DialogHandler(this)
        dialogHandler.open()

        getLocationCoordinates { location, explain ->
            when {
                location != null -> {
                    val networkHandler = NetworkHandler()
                    /*
                    networkHandler.postCheckIn() { error ->

                    }
                    */
                }
                explain == true -> {
                    dialogHandler.setErrorLocation()
                }
                else -> {

                }
            }
        }
    }

    private fun getLocationCoordinates(callback: (location: Pair<Double, Double>?, explain: Boolean?) -> Unit) {
        checkLocationPermission { granted, explain ->
            when {
                granted -> fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location == null) {
                            callback(null, null)
                        } else {
                            callback(Pair(location?.latitude, location?.longitude), null)
                        }
                    }
                explain == true -> callback(null, true)
                else -> callback(null, null)
            }
        }
    }

    private fun checkLocationPermission(callback: (granted: Boolean, explain: Boolean?) -> Unit) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO show WE NEED PERMISSION dialog
                callback(false, true)
            } else {
                requestLocationPermission(callback)
            }
        } else {
            callback(true, false)
        }
    }

    private fun requestLocationPermission(callback: (granted: Boolean, explain: Boolean?) -> Unit) {
        locationRequestCallback = callback
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                val granted = (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                if (locationRequestCallback != null) {
                    locationRequestCallback?.invoke(granted, false)
                    locationRequestCallback = null
                }
            }
            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        setupNfc()
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }


    // this is here to catch some situations of reading nfc
    override fun onNewIntent(intent: Intent?) {
        println("KIKKEL  on new intent called")
        super.onNewIntent(intent)
        if(intent != null){ processIncomingIntent(intent)}
    }

    // setupNfc fetches the default NFC -adapter.
    private fun setupNfc(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if(!nfcAdapter.isEnabled) showSnackbar(R.string.snackbar_no_nfc)

        // pendingIntent is constantly scanning for nfc tags while on the main page
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
    }

    private fun generateView(){
        recyclerview_main.layoutManager = LinearLayoutManager(this)
        recyclerview_main.adapter = MainAdapter(DatabaseObj.getSchedule())
    }

    // in processIncomingIntent we check the message initiated by the nfc -reading
    private fun processIncomingIntent(intent: Intent){
        // so again, just to check that the nfc tag has some ndef data
        // because the ndef holds multiple points of data, we tell here that this one bytestream is the id data.
        // it's easy to configure when writing the nfc slab. (make the "app opening" to be first datapoint, and the id the second)
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ) {
            val msg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0] as NdefMessage
            val nfc_id = String(msg.records[1].payload.drop(3).toByteArray())
            println("KIKKEL " + nfc_id)

            showAlertDialog()
        } else {
            // for some reason the incomint intent.action is not the one we want
                return
        }
    }


    private fun showSnackbar(stringInt: Int){
        val coordinator = this.findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        Snackbar.make(coordinator, stringInt, Snackbar.LENGTH_LONG).show()
    }

}
