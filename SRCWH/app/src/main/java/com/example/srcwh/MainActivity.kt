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
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import com.example.srcwh.dialog.DialogAction
import com.example.srcwh.dialog.DialogHandler
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var schedule: List<ClientSchedule>
    private lateinit var networkHandler: NetworkHandler
    private lateinit var dialogHandler: DialogHandler

    private var locationRequestCallback: ((granted: Boolean, explain: Boolean?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DatabaseObj.initDatabaseConnection(this)
        DatabaseObj.user = DatabaseObj.getUserData()!!
        networkHandler = NetworkHandler()
        networkHandler.getSchedule{generateView()}
        // first thing, we need to establish the database connection, and check if current userdata exists
        // getUserData() both initiates the database connection, and returns an user -object IF one exists.
        // if the user object is null, then there was no data. (usually meaning first time user)

        // setup the nfc reader
        setupNfc()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        dialogHandler = DialogHandler(this, supportFragmentManager)

        settings_button.setOnClickListener { view -> openSettings()}

        // if the application was opened via nfc reader, this gets called
        if(intent != null){
            if (intent.extras != null && intent.extras!!.containsKey("nfc")) {
                processIncomingIntent(intent.extras!!["nfc"] as Intent)
            } else {
                processIncomingIntent(intent)
            }
        }
    }

    private fun showAlertDialog(slabId: String, comingFromExplain: Boolean = false) {
        val user = DatabaseObj.getUserData()

        getLocationCoordinates(comingFromExplain) { location, explain ->
            Log.d("LOCATION", "Test done ${location} ${explain}")

            when {
                location != null -> {
                    dialogHandler.open()

                    val networkHandler = NetworkHandler()
                    networkHandler.postCheckIn(user!!.token!!, slabId, location, false) { error ->
                        when (error) {
                            AttendError.LOCATION -> dialogHandler.setErrorLocation()
                            // TODO: add all other possibilities
                        }
                        Log.d("CHECKIN", "Doned ${error}")
                    }
                }
                (explain == true) -> {
                    dialogHandler.open("location_permission") { action ->
                        dialogHandler.close()

                        Log.d("DIALOG", "action $action")

                        if (action == DialogAction.PRIMARY) {
                            showAlertDialog(slabId, true)
                        }
                    }
                }
                else -> {
                    dialogHandler.open("location_permission", true)
                }
            }
        }
    }

    private fun getLocationCoordinates(comingFromExplain: Boolean, callback: (location: Pair<Double, Double>?, explain: Boolean?) -> Unit) {
        Log.d("LOCATION", "getLocationCoordinates")

        checkLocationPermission(comingFromExplain) { granted, explain ->
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

    private fun checkLocationPermission(comingFromExplain: Boolean, callback: (granted: Boolean, explain: Boolean?) -> Unit) {
        Log.d("LOCATION", "checkLocationPermission")

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (!comingFromExplain) {
                    Log.d("LOCATION", "how WE NEED PERMISSION dialog")
                    callback(false, true)
                } else {
                    requestLocationPermission(callback)
                }
            } else {
                requestLocationPermission(callback)
            }
        } else {
            callback(true, false)
        }
    }

    private fun requestLocationPermission(callback: (granted: Boolean, explain: Boolean?) -> Unit) {
        Log.d("LOCATION", "requestLocationPermission")

        locationRequestCallback = callback
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("LOCATION", "onRequestPermissionsResult ${requestCode}")

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
        // reload the nfc reader to make sure it's up and running
        setupNfc()
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)

        // also as a potato solution we have to get the new schedule, since the group might have been changed
        networkHandler.getSchedule{generateView()}
        recyclerview_main.adapter?.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }


    // this is here to catch some situations of reading nfc
    override fun onNewIntent(intent: Intent?) {
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

            if (!dialogHandler.isOpen) showAlertDialog(nfc_id)
        } else {
            // for some reason the incomint intent.action is not the one we want
                return
        }
    }

    private fun openSettings(){
        val activityIntent = Intent(this, SettingsActivity::class.java)
        startActivity(activityIntent)
    }


    private fun showSnackbar(stringInt: Int){
        val coordinator = this.findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        Snackbar.make(coordinator, stringInt, Snackbar.LENGTH_LONG).show()
    }

}
