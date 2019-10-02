package com.example.srcwh

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import android.app.PendingIntent
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import com.example.srcwh.dialog.DialogAction
import com.example.srcwh.dialog.DialogHandler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.srcwh.dialog.DialogInitialState
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var schedule: List<ClientSchedule>
    private lateinit var networkHandler: NetworkHandler
    private lateinit var dialogHandler: DialogHandler

    private lateinit var locationHandler: LocationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Make sure DB is connected
        if (!DatabaseObj.isConnected) DatabaseObj.initDatabaseConnection(this)

        DatabaseObj.user = DatabaseObj.getUserData()!!
        networkHandler = NetworkHandler()
        networkHandler.getSchedule { generateView() }
        // first thing, we need to establish the database connection, and check if current userdata exists
        // getUserData() both initiates the database connection, and returns an user -object IF one exists.
        // if the user object is null, then there was no data. (usually meaning first time user)

        // Check if the login token is still valid
        networkHandler.getTokenValid { valid ->
            if (!valid) {
                DatabaseObj.clearAllData() // TODO: Only clear login...

                // Navigate to login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }

        // setup the nfc reader
        setupNfc()

        // Setup location handler
        locationHandler = LocationHandler(this)

        dialogHandler = DialogHandler(this, supportFragmentManager)

        settings_button.setOnClickListener { view -> openSettings()}

        // if the application was opened via nfc reader, this gets called
        if (intent != null) {
            if (intent.extras != null && intent.extras!!.containsKey("nfc")) {
                processIncomingIntent(intent.extras!!["nfc"] as Intent)
            } else {
                processIncomingIntent(intent)
            }
        }
    }

    private fun showAlertDialog(
        slabId: String,
        confirmUpdate: Boolean = false,
        confirmOverride: Boolean = false,
        comingFromExplain: Boolean = false
    ) {
        val user = DatabaseObj.getUserData()

        locationHandler.getCoordinates(comingFromExplain) { error, coordinates ->
            Log.d("LOCATION", "Test done ${error} ${coordinates}")

            if (error != null || coordinates == null) {
                // Did not get the location
                when (error) {
                    LocationError.DENIED -> dialogHandler.open(DialogInitialState.POSITION_ERROR)
                    LocationError.BLOCKED -> dialogHandler.open(DialogInitialState.POSITION_BLOCK_ERROR)
                    LocationError.EXPLAIN -> dialogHandler.open(DialogInitialState.POSITION_ERROR) { action ->
                        dialogHandler.close()
                        if (action == DialogAction.PRIMARY) {
                            showAlertDialog(slabId, confirmUpdate, confirmOverride, true)
                        } else {
                            dialogHandler.close()
                        }
                    }
                    else -> dialogHandler.open(DialogInitialState.ERROR) // TODO: Maybe error page is required?
                }
            } else {
                // Did get a location
                dialogHandler.open()

                val networkHandler = NetworkHandler()
                networkHandler.postAttend(
                    user!!.token!!,
                    slabId,
                    coordinates,
                    confirmUpdate,
                    confirmOverride
                ) { error, location, lesson ->
                    Log.d("CHECKIN", "Doned $error")

                    if (error != null) {
                        when (error) {
                            AttendError.LESSON -> dialogHandler.setError()
                            AttendError.LOCATION -> dialogHandler.setConfirm(
                                location,
                                lesson
                            ) { action ->
                                if (action == DialogAction.PRIMARY) {
                                    showAlertDialog(
                                        slabId,
                                        confirmUpdate = confirmUpdate,
                                        confirmOverride = true
                                    )
                                } else {
                                    dialogHandler.close()
                                }
                            }
                            AttendError.POSITION -> dialogHandler.setErrorPosition(lesson)
                            AttendError.UPDATE -> dialogHandler.setOverride(
                                location,
                                lesson
                            ) { action ->
                                if (action == DialogAction.PRIMARY) {
                                    showAlertDialog(
                                        slabId,
                                        confirmUpdate = true,
                                        confirmOverride = confirmOverride
                                    )
                                } else {
                                    dialogHandler.close()
                                }
                            }
                            else -> dialogHandler.setError()
                        }
                    } else {
                        dialogHandler.setAttended(location, lesson)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check for location results
        locationHandler.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
        if (intent != null) {
            processIncomingIntent(intent)
        }
    }

    // setupNfc fetches the default NFC -adapter.
    private fun setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (!nfcAdapter.isEnabled) showSnackbar(R.string.snackbar_no_nfc)

        // pendingIntent is constantly scanning for nfc tags while on the main page
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
    }

    private fun generateView() {
        recyclerview_main.layoutManager = LinearLayoutManager(this)
        recyclerview_main.addOnScrollListener(scrollListener)
        recyclerview_main.adapter = MainAdapter(DatabaseObj.getSchedule())
    }

    // in processIncomingIntent we check the message initiated by the nfc -reading
    private fun processIncomingIntent(intent: Intent) {
        // so again, just to check that the nfc tag has some ndef data
        // because the ndef holds multiple points of data, we tell here that this one bytestream is the id data.
        // it's easy to configure when writing the nfc slab. (make the "app opening" to be first datapoint, and the id the second)
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val msg =
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0] as NdefMessage
            val nfc_id = String(msg.records[1].payload.drop(3).toByteArray())
            println("KIKKEL " + nfc_id)

            if (!dialogHandler.isOpen) showAlertDialog(nfc_id)
        } else {
            // for some reason the incomint intent.action is not the one we want
            return
        }
    }

    private var scrollListener:RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val n = recyclerView.layoutManager?.childCount
            Log.d("SCROLLED", "coordx : $dx coordy $dy and children: $n")
            when(dy<0){
                 true-> {
                     // scrolling upwards, fade in text
                     Log.d("SCROLL", "scrolling up?")
                      // user is scrolling up, which makes the toolbar appear
                     // now would be a good time to play an animation
                 }
                false ->{
                    Log.d("SCROLL", "scrolling down?")
                    // user is scrolling down, play animation is necessary
                }
            }

        }
    }

    private fun openSettings(){
        val activityIntent = Intent(this, SettingsActivity::class.java)
        startActivity(activityIntent)
    }


    private fun showSnackbar(stringInt: Int) {
        val coordinator = this.findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        Snackbar.make(coordinator, stringInt, Snackbar.LENGTH_LONG).show()
    }

}
