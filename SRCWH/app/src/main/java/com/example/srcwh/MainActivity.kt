package com.example.srcwh

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import android.app.PendingIntent
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.nfc.tech.Ndef
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_login.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log


class MainActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private var user: User? = null
    private val networkHandler = NetworkHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // first thing, we need to establish the database connection, and check if current userdata exists
        // getUserData() both initiates the database connection, and returns an user -object IF one exists.
        // if the user object is null, then there was no data. (usually meaning first time user)
        user = getUserData()

        if (user == null) {
            Log.d("MAIN", "Opening login")

            // TODO app should decide on startup if login activity should be run
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK  and Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
           // changeFragment(LoginFragment { error, result -> })
        }

        changeFragment(ScanFragment())

        // setup the nfc reader
        setupNfc()

        // if the application was opened via nfc reader, this gets called
        if(intent != null){ processIncomingIntent(intent)}


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

    // in processIncomingIntent we check the message initiated by the nfc -reading
    private fun processIncomingIntent(intent: Intent){
        // so again, just to check that the nfc tag has some ndef data
        // because the ndef holds multiple points of data, we tell here that this one bytestream is the id data.
        // it's easy to configure when writing the nfc slab. (make the "app opening" to be first datapoint, and the id the second)
        if(intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ){
            val msg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0] as NdefMessage
            val nfc_id = String(msg.records[1].payload.drop(3).toByteArray())
            println("KIKKEL " + nfc_id)

        }else{
            // for some reason the incomint intent.action is not the one we want
                return
        }
    }

    private fun getUserData() : User?{
        DatabaseObj.initDatabaseConnection(this)
        return DatabaseObj.getUserData()
    }

    private fun showSnackbar(stringInt: Int){
        val coordinator = this.findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        Snackbar.make(coordinator, stringInt, Snackbar.LENGTH_LONG).show()
    }


    private fun changeFragment(fragment: Fragment, inAnim: Int = 0, outAnim: Int = 0){
        val fManager = supportFragmentManager
        val fTransaction = fManager.beginTransaction()


        // first we need to check if a fragment exists, meaning should we replace or add
        // we also set the custom animation only when there already exists one fragment
        if(fManager.fragments.count() == 0)fTransaction.add(R.id.main_fragment_container, fragment)
        else{
            fTransaction.setCustomAnimations(inAnim, outAnim)
            fTransaction.replace(R.id.main_fragment_container, fragment)
        }

        fTransaction.commit()
    }


}
