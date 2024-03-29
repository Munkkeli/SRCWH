package com.example.srcwh

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.PendingIntent
import android.content.Context
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout
import java.time.ZonedDateTime
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import android.os.Build
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var schedule: List<ClientSchedule>
    private lateinit var networkHandler: NetworkHandler
    private lateinit var attendHandler: AttendHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Setup prominent toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val collapsingToolbar =
            findViewById<SubtitleCollapsingToolbarLayout>(R.id.collapsing_toolbar)
        toolbar.title = "Schedule"
        collapsingToolbar.title = "Schedule"
        collapsingToolbar.subtitle = ""
        setSupportActionBar(toolbar)

        // first thing, we need to establish the database connection, and check if current userdata exists
        // getUserData() both initiates the database connection, and returns an user -object IF one exists.
        // if the user object is null, then there was no data. (usually meaning first time user)
        if (!DatabaseObj.isConnected) DatabaseObj.initDatabaseConnection(this)
        DatabaseObj.user = DatabaseObj.getUserData()!!

        // Load schedule from DB cache
        schedule = DatabaseObj.getSchedule() ?: listOf()
        generateView(schedule)

        // Set toolbar subtitle
        collapsingToolbar.subtitle = getSubtitleText(schedule)

        networkHandler = NetworkHandler()

        // Check if the login token is still valid
        networkHandler.getTokenValid { valid ->
            if (!valid) {
                DatabaseObj.clearAllData()

                // Navigate to login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }

        // setup the notification channel
        createNotificationChannel()

        // setup the nfc reader
        setupNfc()

        // Setup attend handler
        attendHandler = AttendHandler(this, supportFragmentManager)

        // if the application was opened via nfc reader, this gets called
        if (intent != null) {
            if (intent.extras != null && intent.extras!!.containsKey("nfc")) {
                processIncomingIntent(intent.extras!!["nfc"] as Intent)
            } else {
                processIncomingIntent(intent)
            }
        }

        // QR scan FAB
        scan_qr_code_button.setOnClickListener {
            // Navigate to scan
            val intent = Intent(this, QRActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK
            startActivityForResult(intent, QR_ACTIVITY_REQUEST_CODE)
            // finish()
        }
    }

    private fun getSubtitleText(schedule: List<ClientSchedule>?): String {
        if (schedule == null || schedule.isEmpty()) {
            return getString(R.string.toolbar_subtitle_none)
        }

        val now = Controller.time
        val scheduleLeft = schedule.filter { lesson -> now.isBefore(lesson.start) }

        if (scheduleLeft.isEmpty()) {
            return getString(R.string.toolbar_subtitle_done)
        }

        var subtitle = if (scheduleLeft.size == schedule.size) {
            getString(R.string.toolbar_subtitle_start)
        } else {
            getString(R.string.toolbar_subtitle_start_left)
        }

        subtitle += " ${scheduleLeft.size} "

        subtitle += if (scheduleLeft.size > 1) {
            getString(R.string.toolbar_subtitle_end_multiple)
        } else {
            getString(R.string.toolbar_subtitle_end)
        }

        return subtitle
    }

    fun updateSchedule() {
        networkHandler.getSchedule {
            schedule = DatabaseObj.getSchedule() ?: listOf()
            (recyclerview_main.adapter as MainAdapter).schedule = schedule
            recyclerview_main.adapter?.notifyDataSetChanged()

            Log.d("SCHEDULE", "Schedule updated from API")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Pass on to attend handler
        attendHandler.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == QR_ACTIVITY_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                val qr = data?.extras?.get("qr")
                Log.d("QR", "Returned to main activity with $qr")

                val slabId = qr.toString().replace("$BASE_URL/qr/", "")

                attendHandler.attend(slabId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // reload the nfc reader to make sure it's up and running
        setupNfc()
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)

        // also as a potato solution we have to get the new schedule, since the group might have been changed
        updateSchedule()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)

        // start the worker for background schedule fetching
        if(DatabaseObj.getSettingsData().allowNotifications){
            startWork()
        }

    }


    // this is here to catch some situations of reading nfc
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            processIncomingIntent(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Change the menu item icon color to white, tried to do this with themes, could not get it to work...
        val settings = menu.findItem(R.id.action_settings)
        settings.icon.setTint(getColor(R.color.colorTaskBarIcon))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            openSettings()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    // setupNfc fetches the default NFC -adapter.
    private fun setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // if (!nfcAdapter.isEnabled) showSnackbar(R.string.snackbar_no_nfc)

        // pendingIntent is constantly scanning for nfc tags while on the main page
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
    }

    private fun generateView(schedule: List<ClientSchedule>) {
        val layoutManager = LinearLayoutManager(this)
        recyclerview_main.layoutManager = layoutManager
        recyclerview_main.addOnScrollListener(scrollListener)
        recyclerview_main.adapter = MainAdapter(this, schedule)

        // Create custom divider for recycler view
        val dividerItemDecoration = object : DividerItemDecoration(
            recyclerview_main.context,
            layoutManager.orientation
        ) {
            override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                val dividerLeft = parent.paddingLeft
                val dividerRight = parent.width - parent.paddingRight

                val childCount = parent.childCount
                for (i in 0..childCount - 2) {
                    val child = parent.getChildAt(i)

                    val params = child.layoutParams as RecyclerView.LayoutParams

                    val dividerTop = child.bottom + params.bottomMargin
                    val dividerBottom = dividerTop + this.drawable!!.intrinsicHeight

                    this.drawable!!.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                    this.drawable!!.draw(canvas)
                }
            }
        }

        dividerItemDecoration.setDrawable(getDrawable(R.drawable.item_lesson_divider)!!)
        recyclerview_main.addItemDecoration(dividerItemDecoration)
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

            if (!attendHandler.isOpen) attendHandler.attend(nfc_id)
        } else if (intent.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            val qr = data.toString()

            if (QR_REGEX.toRegex().matches(qr)) {
                val slabId = qr.replace("$BASE_URL/qr/", "")
                attendHandler.attend(slabId)
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Unknown tag ID",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } else {
            // for some reason the incoming intent.action is not the one we want
            return
        }
    }

    private var scrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val n = recyclerView.layoutManager?.childCount
                Log.d("SCROLLED", "coordx : $dx coordy $dy and children: $n")
                when (dy < 0) {
                    true -> {
                        // scrolling upwards, fade in text
                        Log.d("SCROLL", "scrolling up?")
                        // user is scrolling up, which makes the toolbar appear
                        // now would be a good time to play an animation
                    }
                    false -> {
                        Log.d("SCROLL", "scrolling down?")
                        // user is scrolling down, play animation is necessary
                    }
                }

            }
        }

    private fun openSettings() {
        val activityIntent = Intent(this, SettingsActivity::class.java)
        startActivity(activityIntent)
    }

    private fun startWork(){
        val work = createWorkRequest(Data.EMPTY)
        WorkManager.getInstance().enqueueUniquePeriodicWork("Schedule Update Work", ExistingPeriodicWorkPolicy.REPLACE, work)
    }

    private fun createConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    private fun createWorkRequest(data: Data)= PeriodicWorkRequestBuilder<BackgroundWorker>(15, TimeUnit.MINUTES)
        .setInputData(data)
        .setConstraints(createConstraints())
        .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
        .build()



    private fun showSnackbar(stringInt: Int) {
        // val coordinator = this.findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        // Snackbar.make(coordinator, stringInt, Snackbar.LENGTH_LONG).show()
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                NOTIF_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificaitonManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificaitonManager.createNotificationChannel(channel)
        }
    }
}
