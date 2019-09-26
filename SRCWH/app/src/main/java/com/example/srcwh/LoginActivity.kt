package com.example.srcwh

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d("LOGIN", "Activity created!")

        changeFragment(LoginFragment { token, user ->  handleLogin(token, user) }, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    private fun handleLogin(token: String, user: LoginUser) {
        var dbUser = User(user.id.toLong(), user.firstName, user.lastName, user.groupList.joinToString(","), "", user.hash, token)

        // Check if result has more than one group, if it does, we have to ask the user which one they want to use
        if (user.groupList.size > 1) {
            changeFragment(GroupFragment(token, user) { group ->
                Log.d("LOGIN", "Group saved!")

                dbUser.currentGroup = group
                saveUserToDB(dbUser)
            }, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        } else {
            dbUser.currentGroup = user.groupList[0]
            saveUserToDB(dbUser)
        }
    }

    private fun saveUserToDB(user: User) {
        DatabaseObj.initDatabaseConnection(this)
        DatabaseObj.addUserToDatabase(user)

        backToMain()
    }

    private fun backToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun changeFragment(fragment: Fragment, inAnim: Int = 0, outAnim: Int = 0){
        val transaction  = supportFragmentManager.beginTransaction()

        Log.d("LOGIN", "Replacing fragment")

        if (supportFragmentManager.fragments.count() == 0) {
            transaction.add(R.id.login_fragment_container, fragment)
        } else {
            transaction.setCustomAnimations(inAnim, outAnim)
            transaction.replace(R.id.login_fragment_container, fragment)
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }
}
