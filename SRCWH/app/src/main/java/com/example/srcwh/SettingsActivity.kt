package com.example.srcwh

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.configuration
import androidx.core.app.NavUtils
import android.view.MenuItem


// purpose of this activity is to handle all the actions and logic of the settings page


class SettingsActivity : AppCompatActivity(), View.OnClickListener,  AdapterView.OnItemSelectedListener {

    private lateinit var networkHandler: NetworkHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        networkHandler = NetworkHandler()

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.settings_toolbar)
        toolbar.title = "Settings"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        dark_mode_switch.setOnClickListener(this)
        notifications_enabled_switch.setOnClickListener(this)
        location_permission_switch.setOnClickListener(this)
        logout_button.setOnClickListener(this)
        group_select_spinner.onItemSelectedListener = this

        setupFunction()
    }

    override fun onResume() {
        super.onResume()
        // user might have changed the settings while away, setup the buttons again
        setupFunction()
    }

    override fun onClick(view: View?) {
       when(view){
           dark_mode_switch-> darkModeSwitch()
           notifications_enabled_switch -> notificationsSwitch()
           location_permission_switch -> enableLocationPermission()
           logout_button -> logout()
       }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // item was selected from the spinner, set the new group and save it to database
        onGroupSelected(parent.getItemAtPosition(pos).toString())
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
        println("KIKKEL nothing interesting happens")
    }

    // Enable back button functionality
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun enableLocationPermission(){
        // user wants to enable the location permissions, guide them to the Android settings -page
        if(location_permission_switch.isChecked){
            val intent = Intent()
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.setData(uri)
            startActivity(intent)
        }

    }

    private fun darkModeSwitch(){
        // the switch was interacted with, check if on/off and act accordingly
        val settings = DatabaseObj.settings
        if(dark_mode_switch.isChecked){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            settings.darkMode = 1
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            settings.darkMode = 0
        }

        DatabaseObj.updateSettingsData(settings)
      //  recreate()
    }

    private fun notificationsSwitch(){
        val settings = DatabaseObj.settings
        settings.allowNotifications = notifications_enabled_switch.isChecked
        DatabaseObj.updateSettingsData(settings)
    }

    private fun logout(){
        // delete the local sqlite database -data and open up the login page
        DatabaseObj.clearAllData()
        startActivity(Intent().setClass(this, LoginActivity::class.java))
    }

    // this function set's the buttons to correct positions -- i.e if location is enabled, the button is already checked etc.
    private fun setupFunction(){
        // switches to correct position
        location_permission_switch.isChecked = switchSetup(location_permission_switch)
        dark_mode_switch.isChecked = switchSetup(dark_mode_switch)
        notifications_enabled_switch.isChecked = switchSetup(notifications_enabled_switch)

        // populate the spinner and select the correct group as a chosen group
        val adapterGroups = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, DatabaseObj.user.groupList.toList())
        adapterGroups.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        group_select_spinner.adapter = adapterGroups
        val group = DatabaseObj.user.groupList.toList().indexOf(DatabaseObj.user.currentGroup)
        group_select_spinner.setSelection(group)


        // deactivate location enabling switch and text if location is already permitted
        if(location_permission_switch.isChecked)disableLocationPermissionSwitchAndText()
    }

    private fun switchSetup(view: View): Boolean{
        return when(view){
            location_permission_switch -> ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
            dark_mode_switch -> return when(configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }
            notifications_enabled_switch -> return DatabaseObj.getSettingsData()!!.allowNotifications
            else -> false
        }
    }

    private fun onGroupSelected(group: String){
        // first we check if the selected group was different than before a.k.a is it a new group
        if(DatabaseObj.user.currentGroup != group){
            println("KIKKEL  clearing the schedule ")
            // was a new group, first make the users current group the new group
            DatabaseObj.user.currentGroup = group
            // let's update the new user info into the database
            DatabaseObj.updateUserdata()
            // then we have to clear the database of the current schedule, since we are not using that anymore
            DatabaseObj.clearSchedule()

            // as a test, let's see how many objects the table still has
            Log.d("SCHEDULE", DatabaseObj.getSchedule()?.count().toString())

            // and finally update the backend
            networkHandler.postGroupUpdate(DatabaseObj.user.token!!, group){ error ->
                if (error != null) {
                    Toast.makeText(baseContext, error, Toast.LENGTH_SHORT).show()
                }}
        }
    }

    private fun disableLocationPermissionSwitchAndText(){
        location_perm_textview.isEnabled = false
        location_permission_switch.isEnabled = false
    }
}


