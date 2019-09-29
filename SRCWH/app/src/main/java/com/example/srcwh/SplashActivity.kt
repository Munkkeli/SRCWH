package com.example.srcwh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlin.reflect.KClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = getUserData()

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
}