package com.example.srcwh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlin.reflect.KClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = getUserData()
        val settings = getCurrentSettings()

        if (settings == null) {
            // no settings implemented yet, insert basic settings
            Log.d("SPLASH", "settings null")
            DatabaseObj.initDefaultSettings()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            // settings were found, change the app theme
            Log.d("SPLASH", "settings found! darkmode is: ${settings.darkMode}")
            when(settings.darkMode){
                1 ->   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                0 ->  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        Log.d("SPLASH", "Went to splash screen")
        Log.d("SPLASH", intent.action)

        if (user == null) {
            navigate(LoginActivity::class as KClass<Any>, intent)
        } else {
            navigate(MainActivity::class as KClass<Any>, intent)
        }
    }

    private fun navigate(activity: KClass<Any>, nfc: Intent?) {
        val intent = Intent(this, activity.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("nfc", nfc)
        startActivity(intent)
        finish()
    }

    private fun getUserData() : ClientUser?{
        DatabaseObj.initDatabaseConnection(this)
        return DatabaseObj.getUserData()
    }

    private fun getCurrentSettings(): AppSettings?{
        return DatabaseObj.getSettingsData()
    }
}